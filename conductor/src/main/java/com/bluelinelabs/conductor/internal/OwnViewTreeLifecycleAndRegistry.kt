package com.bluelinelabs.conductor.internal

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.bluelinelabs.conductor.Controller

internal class OwnViewTreeLifecycleAndRegistry
private constructor(controller: Controller) : LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry

    private lateinit var savedStateRegistryController: SavedStateRegistryController

    private var handlingDestroyViaHostDetach = false

    private var savedRegistryState = Bundle.EMPTY

    private fun initLifecycle() {
        lifecycleRegistry = LifecycleRegistry(this)
        savedStateRegistryController = SavedStateRegistryController.create(this)
    }

    init {
        controller.addLifecycleListener(object : Controller.LifecycleListener() {

            override fun postContextAvailable(controller: Controller, context: Context) {
                initLifecycle()
                savedStateRegistryController.performRestore(savedRegistryState)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }

            override fun postCreateView(controller: Controller, view: View) {
                ViewTreeLifecycleOwner.set(view, this@OwnViewTreeLifecycleAndRegistry)
                ViewTreeSavedStateRegistryOwner.set(
                    view,
                    this@OwnViewTreeLifecycleAndRegistry
                )
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }

            override fun postAttach(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            override fun preDetach(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }

            override fun onSaveInstanceState(controller: Controller, outState: Bundle) {
                savedStateRegistryController.performSave(outState)
            }

            override fun onSaveViewState(controller: Controller, outState: Bundle) {
                savedRegistryState = Bundle()
                savedStateRegistryController.performSave(savedRegistryState)
            }

            override fun onRestoreInstanceState(
                controller: Controller,
                savedInstanceState: Bundle
            ) {
                savedStateRegistryController.performRestore(savedInstanceState)
            }

            override fun preDestroyView(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                if (controller.isBeingDestroyed && controller.router.backstackSize == 0) {
                    val parent = view.parent as? View
                    handlingDestroyViaHostDetach = parent != null
                    parent?.addOnAttachStateChangeListener(object :
                        View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View?) = Unit
                        override fun onViewDetachedFromWindow(v: View?) {
                            parent.removeOnAttachStateChangeListener(this)
                            onDestroy()
                        }
                    })
                }
            }

            override fun preDestroy(controller: Controller) {
                if (!handlingDestroyViaHostDetach) {
                    onDestroy()
                }
            }

            private fun onDestroy() {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        })
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    override fun getSavedStateRegistry(): SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    companion object {
        fun own(target: Controller) {
            OwnViewTreeLifecycleAndRegistry(target)
        }
    }
}
