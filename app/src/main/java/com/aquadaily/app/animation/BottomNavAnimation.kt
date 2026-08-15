package com.aquadaily.app.animation

import android.view.View
import android.view.animation.OvershootInterpolator

object BottomNavAnimation {
    fun animateClick(view: View) {
        view.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(150L)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150L)
                    .start()
            }
            .start()
    }

    fun animateNavigationItem(view: View) {
        view.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .translationY(-4f)
            .setDuration(180L)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    fun resetNavigationItem(view: View) {
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(180L)
            .start()
    }
}