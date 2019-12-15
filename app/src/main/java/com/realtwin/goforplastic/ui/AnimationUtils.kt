package com.realtwin.goforplastic;

import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation

internal object AnimationUtils {

    val DEFAULT_ANIMATION_DURATION = 500L

    // Creates a slide-up animation with optional arguments duration and start-delay
    fun createSlideUpAnimation(btn : View, dur: Long = DEFAULT_ANIMATION_DURATION, delay: Long = 0L): Animation{

        // button slide-up
        val slideUpAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f,
            Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT, 0f)

        slideUpAnimation.duration = dur
        slideUpAnimation.interpolator = DecelerateInterpolator()
        slideUpAnimation.startOffset = delay
        slideUpAnimation.setAnimationListener(
            AnimationUtils.VisibilityAnimationListener(btn)
        )

        return slideUpAnimation
    }

    // Creates a slide-up animation with optional arguments duration and start-delay
    fun slideUpButton(btn : View, dur: Long = DEFAULT_ANIMATION_DURATION, delay: Long = 0L) {

        // button slide-up
        val slideUpAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f,
            Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT, 0f)

        slideUpAnimation.duration = dur
        slideUpAnimation.interpolator = DecelerateInterpolator()
        slideUpAnimation.startOffset = delay
        slideUpAnimation.setAnimationListener(
            AnimationUtils.VisibilityAnimationListener(btn)
        )

        btn.startAnimation(slideUpAnimation)
    }

    internal class VisibilityAnimationListener(val view: View): Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) = Unit
        override fun onAnimationEnd(animation: Animation?) = Unit
        override fun onAnimationStart(animation: Animation?) {
            view.visibility = View.VISIBLE
        }
    }

    // NOTE: Might help adding also a listener to set
    // view to INVISIBLE/VISIBLE onEnd/onStart respectively
    fun crossFadeViews(v1: View?, v2: View?) {
        v1?.animate()?.alpha(0f)?.duration = 2000
        v2?.animate()?.alpha(1f)?.duration = 2000
        v2?.bringToFront()
    }

    fun scaleView(v: View?, startScale: Float, endScale: Float, offSet: Long = 0) {
        val anim = ScaleAnimation(
            startScale, endScale, // Start and end values for the X axis scaling
            startScale, endScale, // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f
        ) // Pivot point of Y scaling
        anim.fillAfter = true // Needed to keep the result of the animation
        anim.duration = 1000
        anim.startOffset = offSet
        v?.startAnimation(anim)
    }

}