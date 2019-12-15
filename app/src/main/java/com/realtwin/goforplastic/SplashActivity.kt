package com.realtwin.goforplastic;

import android.app.ActivityOptions
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.*
import android.view.animation.Animation.RELATIVE_TO_PARENT
import kotlinx.android.synthetic.main.activity_splash.*
import android.content.Intent
//import android.support.v7.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity() {

    private val fadeInAnimation = AlphaAnimation(0f,1f)
    private val animationDuration = 500L
    private val animationStartDelay = 300L
    private lateinit var handler: Handler

    private val animationListener = object : Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) = Unit
        override fun onAnimationEnd(animation: Animation?) {
            // Dummy test to show 2nd phase of app flow
            handler.postDelayed({onSplashEnded()}, 500L)
        }
        override fun onAnimationStart(animation: Animation?) {
            splash_banner.visibility = View.VISIBLE
        }
    }

    // banner slide-up
    private val buttonSlideUpAnimation = TranslateAnimation(
        RELATIVE_TO_PARENT, 0f, RELATIVE_TO_PARENT, 0f,
        RELATIVE_TO_PARENT, 1f, RELATIVE_TO_PARENT, 0f
    )

    // Constructor
    init {
        fadeInAnimation.duration = animationDuration
        fadeInAnimation.interpolator = AccelerateDecelerateInterpolator()
        fadeInAnimation.setAnimationListener(animationListener)
        fadeInAnimation.startOffset = animationStartDelay

        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        splash_banner.startAnimation(fadeInAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // Transition to Log in screen
    private fun onSplashEnded() {
        val intent = Intent(this, StartupActivity::class.java)

        val p1 = android.util.Pair(splash_banner as View, "appBanner")
        val p2 = android.util.Pair(splash_background as View, "appBackground")

        val options = ActivityOptions.makeSceneTransitionAnimation(this, p1, p2)
        startActivity(intent, options.toBundle())
        this.finish()
    }

}