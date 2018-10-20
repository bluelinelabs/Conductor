package com.bluelinelabs.conductor.demo.kotlin.changehandler

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.Explode
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup

import com.bluelinelabs.conductor.changehandler.SharedElementTransitionChangeHandler

import java.util.ArrayList

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CityGridSharedElementTransitionChangeHandler(waitForTransitionNames: List<String>) : SharedElementTransitionChangeHandler() {

    private val names: ArrayList<String> = ArrayList(waitForTransitionNames)

    override fun saveToBundle(bundle: Bundle) {
        bundle.putStringArrayList(KEY_WAIT_FOR_TRANSITION_NAMES, names)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        val savedNames = bundle.getStringArrayList(KEY_WAIT_FOR_TRANSITION_NAMES)
        if (savedNames != null) {
            names.addAll(savedNames)
        }
    }

    override fun getExitTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean): Transition? {
        return if (isPush) {
            Explode()
        } else {
            Slide(Gravity.BOTTOM)
        }
    }

    override fun getSharedElementTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean): Transition? {
        return TransitionSet().addTransition(ChangeBounds()).addTransition(ChangeClipBounds()).addTransition(ChangeTransform())
    }

    override fun getEnterTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean): Transition? {
        return if (isPush) {
            Slide(Gravity.BOTTOM)
        } else {
            Explode()
        }
    }

    override fun configureSharedElements(container: ViewGroup, from: View?, to: View?, isPush: Boolean) {
        for (name in names) {
            addSharedElement(name)
            waitOnSharedElementNamed(name)
        }
    }

    companion object {

        private val KEY_WAIT_FOR_TRANSITION_NAMES = "CityGridSharedElementTransitionChangeHandler.names"
    }

}
