package com.realtwin.goforplastic

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.Camera
import com.google.ar.core.ImageFormat
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.realtwin.goforplastic.detection.DetectorActivity
import com.realtwin.goforplastic.detection.customview.OverlayView
import com.realtwin.goforplastic.detection.env.BorderedText
import com.realtwin.goforplastic.detection.env.ImageUtils
import com.realtwin.goforplastic.detection.tflite.Classifier
import com.realtwin.goforplastic.detection.tflite.Classifier.Recognition
import com.realtwin.goforplastic.detection.tflite.TFLiteObjectDetectionAPIModel
import com.realtwin.goforplastic.detection.tracking.MultiBoxTracker
import kotlinx.android.synthetic.main.activity_ar.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext


class PlasticDetectionFragment : ArFragment(), CoroutineScope {


    companion object {
        private const val TAG = "PlasticDetectionFragment"

    }

    //Detector Activity
// Which detection model to use: by default uses Tensorflow Object Detection API frozen
// checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val TF_OD_API_INPUT_SIZE: Int = 300

    private val TF_OD_API_DETECTION_AREA_THRESHOLD: Int = (TF_OD_API_INPUT_SIZE * TF_OD_API_INPUT_SIZE * 0.5f).toInt()
    private val TF_OD_API_IS_QUANTIZED = true
    private val TF_OD_API_MODEL_FILE = "detect.tflite"
    private val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
    private val MODE = DetectorMode.TF_OD_API
    // Minimum detection confidence to track a detection.
    private val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f
    private val SAVE_PREVIEW_BITMAP = false
    var trackingOverlay: OverlayView? = null

    private var detector: Classifier? = null

    private var lastProcessingTimeMs: Long = 0
    private var rgbFrameBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var cropCopyBitmap: Bitmap? = null

    private var computingDetection = false

    private var timestamp: Long = 0

    private var frameToCropTransform: Matrix? = null
    private val cropToFrameTransform: Matrix? = null

    private val tracker: MultiBoxTracker? = null

    private val borderedText: BorderedText? = null

    // AR Token
    private val animHandler: Handler = Handler(Looper.getMainLooper())
    private var shouldAugment: Boolean = false
    private var shouldDetect: Boolean = true

    private var correctResultCounter = 0

    private var correctResult: Recognition? = null

    private var tokenRenderable: ModelRenderable? = null
    private var tokenNode: Node? = null
    private var tokenAnchorNode: AnchorNode? = null
    private var tokenLocation = RectF()
    private var resizeScale = 1f

    // Camera activity staff
    private var isProcessingFrame = false
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var rgbBytes: IntArray? = null
    private var yRowStride = 0
    private var postInferenceCallback: Runnable? = null
    private var imageConverter: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                    activity?.assets,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED)
                    TF_OD_API_INPUT_SIZE
        } catch (e: IOException) {
            e.printStackTrace()
                Log.e(e.message, "Exception initializing classifier!")
            val toast = Toast.makeText(
                    activity?.applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT)
            toast.show()
            activity?.finish()
        }

        // AR test, augment 5 seconds after
        ModelRenderable.builder()
                .setSource(context, R.raw.bottle_token)
                .build()
                .thenAccept{ renderable ->
                    tokenRenderable = renderable
                }
    }

    /** depr */
    init {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Hide plane discovery
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        arSceneView.isLightEstimationEnabled = false

        // Init sessions
        initializeSession()

        return view
    }

    // Note: Might be useful if we want to add image tracking
    override fun getSessionConfiguration(session: Session): Config {

        fun loadAugmentedImageBitmap(imageName: String): Bitmap =
                requireContext().assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

        fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
            try {
                config.augmentedImageDatabase = AugmentedImageDatabase(session).also { db ->
                    //db.addImage(TEST_IMAGE_1, loadAugmentedImageBitmap(TEST_IMAGE_1))
                    // Note: Here add rest of demo
                }
                return true
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Could not add bitmap to augmented image database", e)
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image bitmap.", e)
            }
            return false
        }

        return super.getSessionConfiguration(session).also {
            it.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            it.focusMode = Config.FocusMode.AUTO
        }
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame
        if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            return
        }

        // detect only while
        if(shouldDetect)
            detectPlastic(frame, frame.camera)

        // AR
        if(shouldAugment){
            // call activity
            (activity as ARActivity).onTrashDetected(correctResult)

            shouldAugment = false
            // Coordinates of Bounding box
            val cropToFrame = Matrix()
            frameToCropTransform!!.invert(cropToFrame)
            val tokenPos = FloatArray(2)
            val cropFramePos = floatArrayOf(tokenLocation.centerX()+ tokenLocation.left, tokenLocation.centerY() + tokenLocation.top)
            cropToFrame.mapPoints(tokenPos, cropFramePos)
            tokenPos[0] = resizeScale * tokenPos[0]
            tokenPos[1] = resizeScale * tokenPos[1]
            //Log.d(TAG, "${tokenPos[0]}, ${tokenPos[1]}")
            //createToken(tokenPos[0], tokenPos[1])
            createToken(500f, 600f)
        }
    }

    private fun createToken(objX: Float, objY: Float) {
        // create Pose
        val ray = arSceneView.scene.camera.screenPointToRay(objX, objY)

        val pos = Vector3.add(ray.origin, ray.direction.scaled(0.5f)) // 50cm away
        val tokenPose = Pose.makeTranslation(pos.x, pos.y, pos.z)

        // create anchorNode
        val anchor = arSceneView.session?.createAnchor(tokenPose)
        tokenAnchorNode = AnchorNode(anchor!!).also {
            arSceneView.scene.addChild(it)
        }
        tokenNode = Node().also {
            it.renderable = tokenRenderable
            it.setParent(tokenAnchorNode)
            it.localScale = Vector3(0f,0f,0f)
        }
        // scale-up
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator(1.5f)
            addUpdateListener { v ->
                val value = v.animatedValue as Float;

                tokenNode!!.localScale = Vector3(value, value, value)
            }
            start()
        }
        // and shoot-up 5 meters up
        ValueAnimator.ofFloat(0f, 3f).apply {
            startDelay = 500 // after 1st animation
            duration = 3000
            interpolator = AccelerateInterpolator(1.7f) // more ease-in
            addUpdateListener { v ->
                val value = v.animatedValue as Float;

                // 10 widths away
                val posDiff = Vector3(0f, value, 0f)
                val rotDiff = Quaternion.axisAngle(Vector3(0.24f,0.8f,0.44f), 500*value)

                tokenNode?.localPosition = posDiff
                tokenNode?.localRotation = rotDiff
            }
            addListener(object: Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationCancel(animation: Animator?) = Unit
                override fun onAnimationStart(animation: Animator?) = Unit
                override fun onAnimationEnd(animation: Animator?) {
                    // on end, remove token
                    arSceneView.scene.removeChild(tokenAnchorNode)

                    // test, add again
                    //shouldAugment = true
                }
            })
            start()
        }

    }

    private fun detectPlastic(frame: Frame, camera: Camera) {
        try {
            frame.acquireCameraImage().use { image ->
                require(image.getFormat() == ImageFormat.YUV_420_888) { "Expected image in YUV_420_888 format, got format " + image.getFormat() }

                HandleImage(image)

            }
        }
        catch ( e: NotYetAvailableException){
            Log.e(TAG, "Exception during image processing", e)
        }
        catch (e: Exception) {
            Log.e(TAG, "Exception during image processing", e)
        }
    }

    private fun HandleImage(image: Image) {
        try {
            if (isProcessingFrame) {
                image.close()
                return
            }
            isProcessingFrame = true
            Trace.beginSection("imageAvailable")
            val planes: Array<Image.Plane> = image.getPlanes()
            fillBytes(planes, yuvBytes)
            yRowStride = planes[0].rowStride
            val uvRowStride = planes[1].rowStride
            val uvPixelStride = planes[1].pixelStride
            rgbBytes = IntArray(image.width * image.height * 3)
            imageConverter = Runnable {
                ImageUtils.convertYUV420ToARGB8888(
                        yuvBytes[0],
                        yuvBytes[1],
                        yuvBytes[2],
                        image.width,
                        image.height,
                        yRowStride,
                        uvRowStride,
                        uvPixelStride,
                        rgbBytes)
            }

            postInferenceCallback = Runnable {
                image.close()
                isProcessingFrame = false
            }
            rgbFrameBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            croppedBitmap = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE,TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888)
            resizeScale = image.width.toFloat() / TF_OD_API_INPUT_SIZE
            processImage(image.width, image.height)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            Trace.endSection()
            return
        }
        Trace.endSection()
    }

    fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) { // Because of the variable row stride it's not possible to know in
// advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer[yuvBytes[i]]
        }
    }
    fun processImage(width: Int, height: Int) {
        ++timestamp
        val currTimestamp: Long = timestamp
        trackingOverlay?.postInvalidate()
        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage()
            return
        }
        computingDetection = true
        Log.i("PlasticDetector", "Preparing image $currTimestamp for detection in bg thread.")
        rgbFrameBitmap!!.setPixels(getRgbBytes(), 0, width, 0, 0, width, height)
        readyForNextImage()
        val canvas = Canvas(croppedBitmap)
        frameToCropTransform = ImageUtils.getTransformationMatrix(
                width, height,
                TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                0, false)
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null)
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap)
        }
        launch(Dispatchers.Main) {
            BackgroundImageProcess(currTimestamp)
        }
    }

    private fun BackgroundImageProcess(currTimestamp: Long){
        Log.i(TAG,"Running detection on image $currTimestamp")
        val startTime = SystemClock.uptimeMillis()
        val results: List<Recognition> = detector!!.recognizeImage(croppedBitmap)
        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap)
        val canvas = Canvas(cropCopyBitmap)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.0f
        var minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
        minimumConfidence = when (MODE) {
            DetectorMode.TF_OD_API -> MINIMUM_CONFIDENCE_TF_OD_API
        }
        val mappedRecognitions: MutableList<Recognition> = LinkedList()
        for (result in results) {
            Log.d(TAG, "${result.confidence}")

            if(result.title == "bottle" || result.title == "wine glass" ||
                    result.title == "fork" || result.title == "knife" ||
                    result.title == "spoon"|| result.title == "bowl" ||
                    result.title == "cup" &&
                    getArea(result.location) > TF_OD_API_DETECTION_AREA_THRESHOLD){
                if(correctResultCounter > 5){
                    shouldAugment = true
                    shouldDetect = false

                    // TODO: This needs scaling to screen size
                    tokenLocation = result.location
                    correctResult = result

                    break
                }
                else{
                    correctResultCounter++
                    break
                }

            }

//            val location = result.location
//            if (location != null && result.confidence >= minimumConfidence) {
//                canvas.drawRect(location, paint)
//                cropToFrameTransform?.mapRect(location)
//                result.location = location
//                mappedRecognitions.add(result)
//            }
        }
        tracker?.trackResults(mappedRecognitions, currTimestamp)
        trackingOverlay?.postInvalidate()
        computingDetection = false
//                    activity?.runOnUiThread(
//                            Runnable {
//                                showFrameInfo(previewWidth.toString() + "x" + previewHeight)
//                                showCropInfo(cropCopyBitmap.getWidth().toString() + "x" + cropCopyBitmap.getHeight())
//                                showInference(lastProcessingTimeMs.toString() + "ms")
//                            })
    }

    fun getRgbBytes(): IntArray? {
        imageConverter!!.run()
        return rgbBytes
    }

    fun readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback!!.run()
        }
    }

    fun getArea(rect: RectF): Float{
        return rect.width()* rect.height()
    }
}