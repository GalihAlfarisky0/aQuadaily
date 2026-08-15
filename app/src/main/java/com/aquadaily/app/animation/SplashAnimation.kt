package com.aquadaily.app.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.aquadaily.app.databinding.ActivitySplashBinding

object SplashAnimation {

    fun play(
        binding: ActivitySplashBinding,
        onFinished: () -> Unit
    ) {
        val logo = binding.ivSplashLogo
        val title = binding.tvSplashTitle
        val subtitle = binding.tvSplashSubtitle
        val waterDrop = binding.ivWaterDrop
        val ripple1 = binding.viewRipple1
        val ripple2 = binding.viewRipple2

        // Initial States
        logo.alpha = 0f
        logo.scaleX = 0.85f
        logo.scaleY = 0.85f
        
        title.alpha = 0f
        title.translationY = 20f
        
        subtitle.alpha = 0f
        subtitle.translationY = 20f

        waterDrop.alpha = 0f
        ripple1.alpha = 0f
        ripple2.alpha = 0f

        // 1. Logo Animation (Fade In + Scale)
        val logoAlpha = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f)
        val logoScaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.85f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.85f, 1f)
        
        val logoAnimation = AnimatorSet().apply {
            playTogether(logoAlpha, logoScaleX, logoScaleY)
            duration = 800L
            interpolator = FastOutSlowInInterpolator()
        }

        // 2. Title & Subtitle Animation
        val titleAlpha = ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f)
        val titleTranslation = ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, 20f, 0f)
        val titleAnimation = AnimatorSet().apply {
            playTogether(titleAlpha, titleTranslation)
            duration = 600L
            interpolator = DecelerateInterpolator()
        }

        val subAlpha = ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 0.85f)
        val subTranslation = ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, 20f, 0f)
        val subAnimation = AnimatorSet().apply {
            playTogether(subAlpha, subTranslation)
            duration = 500L
            interpolator = DecelerateInterpolator()
        }

        // 3. Water Drop Animation
        fun createWaterDropAnimator(): AnimatorSet {
            waterDrop.translationY = -400f
            waterDrop.alpha = 0f
            
            val dropFadeIn = ObjectAnimator.ofFloat(waterDrop, View.ALPHA, 0f, 1f).apply { duration = 200 }
            val dropFall = ObjectAnimator.ofFloat(waterDrop, View.TRANSLATION_Y, -400f, 0f).apply {
                duration = 600
                interpolator = AccelerateInterpolator(1.5f)
            }
            val dropFadeOut = ObjectAnimator.ofFloat(waterDrop, View.ALPHA, 1f, 0f).apply { duration = 50 }
            
            val rippleAnim1 = createRippleAnimator(ripple1, 0)
            val rippleAnim2 = createRippleAnimator(ripple2, 200)

            return AnimatorSet().apply {
                play(dropFadeIn).with(dropFall)
                play(dropFadeOut).after(dropFall)
                play(rippleAnim1).after(dropFall)
                play(rippleAnim2).after(dropFall)
            }
        }

        // Main Sequence
        val mainSequence = AnimatorSet()
        mainSequence.playSequentially(logoAnimation, titleAnimation, subAnimation)
        
        mainSequence.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Start looping water drop
                startLoopingWaterDrop(binding)
                
                // Final delay before navigation
                logo.postDelayed({
                    onFinished()
                }, 2500)
            }
        })
        
        mainSequence.start()
    }

    private fun createRippleAnimator(view: View, startDelay: Long): AnimatorSet {
        view.scaleX = 0.3f
        view.scaleY = 0.3f
        view.alpha = 0f
        
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.3f, 2.0f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.3f, 2.0f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0.8f, 0f)
        
        return AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 800
            this.startDelay = startDelay
            interpolator = DecelerateInterpolator()
        }
    }

    private var waterDropAnimator: AnimatorSet? = null

    private fun startLoopingWaterDrop(binding: ActivitySplashBinding) {
        val waterDrop = binding.ivWaterDrop
        val ripple1 = binding.viewRipple1
        val ripple2 = binding.viewRipple2

        fun runAnimation() {
            waterDrop.translationY = -600f
            waterDrop.alpha = 0f
            
            val dropFadeIn = ObjectAnimator.ofFloat(waterDrop, View.ALPHA, 0f, 1f).apply { duration = 150 }
            val dropFall = ObjectAnimator.ofFloat(waterDrop, View.TRANSLATION_Y, -600f, 0f).apply {
                duration = 700
                interpolator = AccelerateInterpolator(2f)
            }
            val dropFadeOut = ObjectAnimator.ofFloat(waterDrop, View.ALPHA, 1f, 0f).apply { duration = 50 }
            
            val rippleAnim1 = createRippleAnimator(ripple1, 0)
            val rippleAnim2 = createRippleAnimator(ripple2, 300)

            waterDropAnimator = AnimatorSet().apply {
                play(dropFadeIn).with(dropFall)
                play(dropFadeOut).after(dropFall)
                playTogether(rippleAnim1, rippleAnim2)
                
                addListener(object : AnimatorListenerAdapter() {
                    private var isCancelled = false
                    override fun onAnimationCancel(animation: Animator) {
                        isCancelled = true
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        if (!isCancelled) {
                            waterDrop.postDelayed({ runAnimation() }, 500)
                        }
                    }
                })
                start()
            }
        }
        
        runAnimation()
    }

    fun stop() {
        waterDropAnimator?.cancel()
        waterDropAnimator = null
    }
}