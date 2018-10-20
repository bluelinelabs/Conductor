package com.bluelinelabs.conductor.demo.kotlin.changehandler

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.view.View
import android.view.ViewGroup

class CircularRevealChangeHandlerCompat(fromView: View, containerView: View) : CircularRevealChangeHandler(fromView, containerView) {

    override fun getAnimator(container: ViewGroup, from: View?, to: View?, isPush: Boolean, toAddedToContainer: Boolean): Animator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.getAnimator(container, from, to, isPush, toAddedToContainer)
        } else {
            val animator = AnimatorSet()
            if (to != null) {
                val start = if (toAddedToContainer) 0F else to.alpha
                animator.play(ObjectAnimator.ofFloat<View>(to, View.ALPHA, start, 1f))
            }

            if (from != null) {
                animator.play(ObjectAnimator.ofFloat<View>(from, View.ALPHA, 0f))
            }

            animator
        }
    }
}
