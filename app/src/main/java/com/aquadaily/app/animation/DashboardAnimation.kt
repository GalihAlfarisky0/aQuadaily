package com.aquadaily.app.animation

import android.view.View
import android.view.animation.DecelerateInterpolator

object DashboardAnimation {

    fun animateCard(
        view: View,
        delay: Long = 0L
    ) {
        view.alpha = 0f
        view.translationY = 80f
        view.scaleX = 0.95f
        view.scaleY = 0.95f

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(delay)
            .setDuration(600L)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()
    }

    fun animateRecyclerViewItem(view: View, position: Int) {
        view.alpha = 0f
        view.translationX = 100f

        view.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(400L)
            .setStartDelay(position * 50L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}