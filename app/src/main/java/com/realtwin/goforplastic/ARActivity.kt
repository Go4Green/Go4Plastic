package com.realtwin.goforplastic

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.realtwin.goforplastic.detection.tflite.Classifier

class ARActivity : AppCompatActivity() {

    companion object {
        private val TAG = ARActivity::class.java.simpleName
        public val DEBUG_MODE = false
    }

    private val openGlVersion by lazy {
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
    }

    private var mArScene: PlasticDetectionFragment? = null // Camera output

    // Hold Detector reference here (DNN for testing)
    // TODO: Hold this for Recyclable detector
    //private var trashDetector: DNNFieldDetector? = null

    private lateinit var instructionsText: TextView
    private var isTextShowing = false
    private var isTextScanning = false

    private val animHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        val frameLayout = findViewById<FrameLayout>(R.id.root_container)
        mArScene = supportFragmentManager.findFragmentById(R.id.arcoreView) as PlasticDetectionFragment

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        instructionsText = findViewById<TextView>(R.id.txt_instructions)
        instructionsText.visibility = View.GONE

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private val PERMISSION_REQUEST_CODE = 101

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        animHandler.postDelayed({runOnUiThread{animateTextIn(instructionsText)}},1000)
    }

    ////////////////////////////////////////////////////////////////
    // PERMISSIONS
    private fun checkWritePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@ARActivity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return (result == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestWritePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@ARActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(this@ARActivity, "Need write permissions.", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(
                this@ARActivity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun animateTextIn(instructionsText: TextView) {
        val introAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        introAnim.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) = Unit


            override fun onAnimationEnd(animation: Animation?) {
                isTextShowing = true
            }

            override fun onAnimationStart(animation: Animation?) {

            }

        })
        introAnim.interpolator = OvershootInterpolator()
        introAnim.duration = 500
        instructionsText.visibility = View.VISIBLE
        instructionsText.startAnimation(introAnim)
    }

    /** could be passing a few arguments on type */
    fun onTrashDetected(result: Classifier.Recognition?){
        result?.let{
            Toast.makeText(this, "Great job! Here is a token!",//\n${result.title} : ${result.location.width() * result.location.height()}",
                    Toast.LENGTH_LONG).show()
            animateTextOut(instructionsText)
            Handler().postDelayed(this::tokenAcquired, 2000)
        }
    }

    private fun tokenAcquired(){
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun animateTextOut(instructionsText: TextView) {
        val introAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
        introAnim.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) = Unit


            override fun onAnimationEnd(animation: Animation?) {
                instructionsText.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                isTextShowing = false
            }

        })
        introAnim.interpolator = AccelerateInterpolator()
        introAnim.duration = 500
        instructionsText.startAnimation(introAnim)
    }

    public override fun onPause() {
        super.onPause()

    }

}

