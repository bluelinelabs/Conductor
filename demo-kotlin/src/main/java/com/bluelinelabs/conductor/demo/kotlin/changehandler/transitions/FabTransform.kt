/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Example from https://github.com/nickbutcher/plaid
 */
package com.bluelinelabs.conductor.demo.kotlin.changehandler.transitions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver.OnPreDrawListener
import com.bluelinelabs.conductor.demo.kotlin.util.AnimUtils
import java.util.*

/**
 * A transition between a FAB & another surface using a circular reveal moving along an arc.
 *
 *
 * See: https://www.google.com/design/spec/motion/transforming-material.html#transforming-material-radial-transformation
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FabTransform(@param:ColorInt private val color: Int, @param:DrawableRes private val icon: Int) : Transition() {

    init {
        pathMotion = GravityArcMotion()
        duration = DEFAULT_DURATION
    }

    override fun getTransitionProperties(): Array<String> {
        return TRANSITION_PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun createAnimator(sceneRoot: ViewGroup,
                                startValues: TransitionValues?,
                                endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) return null

        val startBounds = startValues.values[PROP_BOUNDS] as Rect
        val endBounds = endValues.values[PROP_BOUNDS] as Rect

        val fromFab = endBounds.width() > startBounds.width()
        val view = endValues.view
        val dialogBounds = if (fromFab) endBounds else startBounds
        val fastOutSlowInInterpolator = AnimUtils.fastOutSlowInInterpolator
        val duration = duration
        val halfDuration = duration / 2
        val twoThirdsDuration = duration * 2 / 3

        if (!fromFab) {
            // Force measure / layout the dialog back to it's original bounds
            view.measure(
                    makeMeasureSpec(startBounds.width(), View.MeasureSpec.EXACTLY),
                    makeMeasureSpec(startBounds.height(), View.MeasureSpec.EXACTLY))
            view.layout(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom)
        }

        val translationX = startBounds.centerX() - endBounds.centerX()
        val translationY = startBounds.centerY() - endBounds.centerY()
        if (fromFab) {
            view.translationX = translationX.toFloat()
            view.translationY = translationY.toFloat()
        }

        // Add a color overlay to fake appearance of the FAB
        val fabColor = ColorDrawable(color)
        fabColor.setBounds(0, 0, dialogBounds.width(), dialogBounds.height())
        if (!fromFab) fabColor.alpha = 0
        view.overlay.add(fabColor)

        // Add an icon overlay again to fake the appearance of the FAB
        val fabIcon = ContextCompat.getDrawable(sceneRoot.context, icon)!!.mutate()
        val iconLeft = (dialogBounds.width() - fabIcon.intrinsicWidth) / 2
        val iconTop = (dialogBounds.height() - fabIcon.intrinsicHeight) / 2
        fabIcon.setBounds(iconLeft, iconTop,
                iconLeft + fabIcon.intrinsicWidth,
                iconTop + fabIcon.intrinsicHeight)
        if (!fromFab) fabIcon.alpha = 0
        view.overlay.add(fabIcon)

        // Since the view that's being transition to always seems to be on the top (z-order), we have
        // to make a copy of the "from" view and put it in the "to" view's overlay, then fade it out.
        // There has to be another way to do this, right?
        var dialogView: Drawable? = null
        if (!fromFab) {
            startValues.view.isDrawingCacheEnabled = true
            startValues.view.buildDrawingCache()
            val viewBitmap = startValues.view.drawingCache
            dialogView = BitmapDrawable(view.resources, viewBitmap)
            dialogView.setBounds(0, 0, dialogBounds.width(), dialogBounds.height())
            view.overlay.add(dialogView)
        }

        // Circular clip from/to the FAB size
        val circularReveal: Animator
        if (fromFab) {
            circularReveal = ViewAnimationUtils.createCircularReveal(view,
                    view.width / 2,
                    view.height / 2,
                    (startBounds.width() / 2).toFloat(),
                    Math.hypot((endBounds.width() / 2).toDouble(), (endBounds.height() / 2).toDouble()).toFloat())
            circularReveal.interpolator = AnimUtils.fastOutLinearInInterpolator
        } else {
            circularReveal = ViewAnimationUtils.createCircularReveal(view,
                    view.width / 2,
                    view.height / 2,
                    Math.hypot((startBounds.width() / 2).toDouble(), (startBounds.height() / 2).toDouble()).toFloat(),
                    (endBounds.width() / 2).toFloat())
            circularReveal.interpolator = AnimUtils.linearOutSlowInInterpolator

            // Persist the end clip i.e. stay at FAB size after the reveal has run
            circularReveal.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val fabOutlineProvider = view.outlineProvider

                    view.outlineProvider = object : ViewOutlineProvider() {
                        internal var hasRun = false

                        override fun getOutline(view: View, outline: Outline) {
                            val left = (view.width - endBounds.width()) / 2
                            val top = (view.height - endBounds.height()) / 2

                            outline.setOval(
                                    left, top, left + endBounds.width(), top + endBounds.height())

                            if (!hasRun) {
                                hasRun = true
                                view.clipToOutline = true

                                // We have to remove this as soon as it's laid out so we can get the shadow back
                                view.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
                                    override fun onPreDraw(): Boolean {
                                        if (view.width == endBounds.width() && view.height == endBounds.height()) {
                                            view.outlineProvider = fabOutlineProvider
                                            view.clipToOutline = false
                                            view.viewTreeObserver.removeOnPreDrawListener(this)
                                            return true
                                        }

                                        return true
                                    }
                                })
                            }
                        }
                    }
                }
            })
        }
        circularReveal.duration = duration

        // Translate to end position along an arc
        val translate = ObjectAnimator.ofFloat(
                view,
                View.TRANSLATION_X,
                View.TRANSLATION_Y,
                if (fromFab)
                    pathMotion.getPath(translationX.toFloat(), translationY.toFloat(), 0f, 0f)
                else
                    pathMotion.getPath(0f, 0f, (-translationX).toFloat(), (-translationY).toFloat()))
        translate.duration = duration
        translate.interpolator = fastOutSlowInInterpolator

        // Fade contents of non-FAB view in/out
        var fadeContents: MutableList<Animator>? = null
        if (view is ViewGroup) {
            fadeContents = ArrayList(view.childCount)
            for (i in view.childCount - 1 downTo 0) {
                val child = view.getChildAt(i)
                val fade = ObjectAnimator.ofFloat(child, View.ALPHA, if (fromFab) 1f else 0f)
                if (fromFab) {
                    child.alpha = 0f
                }
                fade.duration = twoThirdsDuration
                fade.interpolator = fastOutSlowInInterpolator
                fadeContents.add(fade)
            }
        }

        // Fade in/out the fab color & icon overlays
        val colorFade = ObjectAnimator.ofInt(fabColor, "alpha", if (fromFab) 0 else 255)
        val iconFade = ObjectAnimator.ofInt(fabIcon, "alpha", if (fromFab) 0 else 255)
        if (!fromFab) {
            colorFade.startDelay = halfDuration
            iconFade.startDelay = halfDuration
        }
        colorFade.duration = halfDuration
        iconFade.duration = halfDuration
        colorFade.interpolator = fastOutSlowInInterpolator
        iconFade.interpolator = fastOutSlowInInterpolator

        // Run all animations together
        val transition = AnimatorSet()
        transition.playTogether(circularReveal, translate, colorFade, iconFade)
        transition.playTogether(fadeContents)
        if (dialogView != null) {
            val dialogViewFade = ObjectAnimator.ofInt(dialogView, "alpha", 0).setDuration(twoThirdsDuration)
            dialogViewFade.interpolator = fastOutSlowInInterpolator
            transition.playTogether(dialogViewFade)
        }
        transition.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Clean up
                view.overlay.clear()

                if (!fromFab) {
                    view.translationX = 0f
                    view.translationY = 0f
                    view.translationZ = 0f

                    view.measure(
                            makeMeasureSpec(endBounds.width(), View.MeasureSpec.EXACTLY),
                            makeMeasureSpec(endBounds.height(), View.MeasureSpec.EXACTLY))
                    view.layout(endBounds.left, endBounds.top, endBounds.right, endBounds.bottom)
                }

            }
        })
        return AnimUtils.NoPauseAnimator(transition)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        if (view == null || view.width <= 0 || view.height <= 0) return

        transitionValues.values[PROP_BOUNDS] = Rect(view.left, view.top,
                view.right, view.bottom)
    }

    companion object {

        private val DEFAULT_DURATION = 240L
        private val PROP_BOUNDS = "plaid:fabTransform:bounds"
        private val TRANSITION_PROPERTIES = arrayOf(PROP_BOUNDS)
    }
}
