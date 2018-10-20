/*
 * Copyright 2015 Google Inc.
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

package com.bluelinelabs.conductor.demo.kotlin.util

import android.animation.Animator
import android.animation.TimeInterpolator
import android.annotation.TargetApi
import android.os.Build
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.transition.Transition
import android.util.ArrayMap
import android.util.FloatProperty
import android.util.IntProperty
import android.util.Property
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import java.util.*

/**
 * Utility methods for working with animations.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
object AnimUtils {

    private var fastOutSlowIn: Interpolator? = null
    private var fastOutLinearIn: Interpolator? = null
    private var linearOutSlowIn: Interpolator? = null
    private var linear: Interpolator? = null

    val fastOutSlowInInterpolator: Interpolator
        get() {
            if (fastOutSlowIn == null) {
                fastOutSlowIn = FastOutSlowInInterpolator()
            }
            return fastOutSlowIn as Interpolator
        }

    val fastOutLinearInInterpolator: Interpolator
        get() {
            if (fastOutLinearIn == null) {
                fastOutLinearIn = FastOutLinearInInterpolator()
            }
            return fastOutLinearIn as Interpolator
        }

    val linearOutSlowInInterpolator: Interpolator
        get() {
            if (linearOutSlowIn == null) {
                linearOutSlowIn = LinearOutSlowInInterpolator()
            }
            return linearOutSlowIn as Interpolator
        }

    val linearInterpolator: Interpolator
        get() {
            if (linear == null) {
                linear = LinearInterpolator()
            }
            return linear as Interpolator
        }

    /**
     * Linear interpolate between a and b with parameter t.
     */
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    /**
     * A delegate for creating a [Property] of `int` type.
     */
    abstract class IntProp<T>(val name: String) {

        abstract operator fun set(`object`: T, value: Int)
        abstract operator fun get(`object`: T): Int
    }

    /**
     * The animation framework has an optimization for `Properties` of type
     * `int` but it was only made public in API24, so wrap the impl in our own type
     * and conditionally create the appropriate type, delegating the implementation.
     */
    fun <T> createIntProperty(impl: IntProp<T>): Property<T, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            object : IntProperty<T>(impl.name) {
                override fun get(`object`: T): Int? {
                    return impl[`object`]
                }

                override fun setValue(`object`: T, value: Int) {
                    impl[`object`] = value
                }
            }
        } else {
            object : Property<T, Int>(Int::class.java, impl.name) {
                override fun get(`object`: T): Int? {
                    return impl[`object`]
                }

                override fun set(`object`: T, value: Int?) {
                    impl[`object`] = value!!
                }
            }
        }
    }

    /**
     * A delegate for creating a [Property] of `float` type.
     */
    abstract class FloatProp<T> protected constructor(val name: String) {

        abstract operator fun set(`object`: T, value: Float)
        abstract operator fun get(`object`: T): Float
    }

    /**
     * The animation framework has an optimization for `Properties` of type
     * `float` but it was only made public in API24, so wrap the impl in our own type
     * and conditionally create the appropriate type, delegating the implementation.
     */
    fun <T> createFloatProperty(impl: FloatProp<T>): Property<T, Float> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            object : FloatProperty<T>(impl.name) {
                override fun get(`object`: T): Float? {
                    return impl[`object`]
                }

                override fun setValue(`object`: T, value: Float) {
                    impl[`object`] = value
                }
            }
        } else {
            object : Property<T, Float>(Float::class.java, impl.name) {
                override fun get(`object`: T): Float? {
                    return impl[`object`]
                }

                override fun set(`object`: T, value: Float?) {
                    impl[`object`] = value!!
                }
            }
        }
    }

    /**
     * https://halfthought.wordpress.com/2014/11/07/reveal-transition/
     *
     *
     * Interrupting Activity transitions can yield an OperationNotSupportedException when the
     * transition tries to pause the animator. Yikes! We can fix this by wrapping the Animator:
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    class NoPauseAnimator(private val mAnimator: Animator) : Animator() {
        private val mListeners = ArrayMap<Animator.AnimatorListener, Animator.AnimatorListener>()

        override fun addListener(listener: Animator.AnimatorListener) {
            val wrapper = AnimatorListenerWrapper(this, listener)
            if (!mListeners.containsKey(listener)) {
                mListeners[listener] = wrapper
                mAnimator.addListener(wrapper)
            }
        }

        override fun cancel() {
            mAnimator.cancel()
        }

        override fun end() {
            mAnimator.end()
        }

        override fun getDuration(): Long {
            return mAnimator.duration
        }

        override fun getInterpolator(): TimeInterpolator {
            return mAnimator.interpolator
        }

        override fun setInterpolator(timeInterpolator: TimeInterpolator) {
            mAnimator.interpolator = timeInterpolator
        }

        override fun getListeners(): ArrayList<Animator.AnimatorListener> {
            return ArrayList(mListeners.keys)
        }

        override fun getStartDelay(): Long {
            return mAnimator.startDelay
        }

        override fun setStartDelay(delayMS: Long) {
            mAnimator.startDelay = delayMS
        }

        override fun isPaused(): Boolean {
            return mAnimator.isPaused
        }

        override fun isRunning(): Boolean {
            return mAnimator.isRunning
        }

        override fun isStarted(): Boolean {
            return mAnimator.isStarted
        }

        /* We don't want to override pause or resume methods because we don't want them
         * to affect mAnimator.
        public void pause();

        public void resume();

        public void addPauseListener(AnimatorPauseListener listener);

        public void removePauseListener(AnimatorPauseListener listener);
        */

        override fun removeAllListeners() {
            mListeners.clear()
            mAnimator.removeAllListeners()
        }

        override fun removeListener(listener: Animator.AnimatorListener) {
            val wrapper = mListeners[listener]
            if (wrapper != null) {
                mListeners.remove(listener)
                mAnimator.removeListener(wrapper)
            }
        }

        override fun setDuration(durationMS: Long): Animator {
            mAnimator.duration = durationMS
            return this
        }

        override fun setTarget(target: Any?) {
            mAnimator.setTarget(target)
        }

        override fun setupEndValues() {
            mAnimator.setupEndValues()
        }

        override fun setupStartValues() {
            mAnimator.setupStartValues()
        }

        override fun start() {
            mAnimator.start()
        }
    }

    class AnimatorListenerWrapper internal constructor(private val mAnimator: Animator, private val mListener: Animator.AnimatorListener) : Animator.AnimatorListener {

        override fun onAnimationStart(animator: Animator) {
            mListener.onAnimationStart(mAnimator)
        }

        override fun onAnimationEnd(animator: Animator) {
            mListener.onAnimationEnd(mAnimator)
        }

        override fun onAnimationCancel(animator: Animator) {
            mListener.onAnimationCancel(mAnimator)
        }

        override fun onAnimationRepeat(animator: Animator) {
            mListener.onAnimationRepeat(mAnimator)
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    abstract class TransitionEndListener : Transition.TransitionListener {
        abstract fun onTransitionCompleted(transition: Transition?)

        override fun onTransitionStart(transition: Transition?) {

        }

        override fun onTransitionEnd(transition: Transition?) {
            onTransitionCompleted(transition)
        }

        override fun onTransitionCancel(transition: Transition?) {
            onTransitionCompleted(transition)
        }

        override fun onTransitionPause(transition: Transition?) {

        }

        override fun onTransitionResume(transition: Transition?) {

        }
    }

}
