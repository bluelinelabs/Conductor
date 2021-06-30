package com.bluelinelabs.conductor.internal

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.R

/**
 * This class sets the [ViewTreeLifecycleOwner] and [ViewTreeSavedStateRegistryOwner] which is
 * necessary for Jetpack Compose. By setting these, the view state restoration and compose lifecycle
 * play together with the lifecycle of the [Controller].
 */
internal class OwnViewTreeLifecycleAndRegistry private constructor(
  controller: Controller
) : LifecycleOwner, SavedStateRegistryOwner {

  private lateinit var lifecycleRegistry: LifecycleRegistry
  private lateinit var savedStateRegistryController: SavedStateRegistryController

  private var hasSavedState = false
  private var savedRegistryState = Bundle.EMPTY

  init {
    controller.addLifecycleListener(object : Controller.LifecycleListener() {
      override fun preCreateView(controller: Controller) {
        hasSavedState = false

        lifecycleRegistry = LifecycleRegistry(this@OwnViewTreeLifecycleAndRegistry)
        savedStateRegistryController = SavedStateRegistryController.create(
          this@OwnViewTreeLifecycleAndRegistry
        )
        savedStateRegistryController.performRestore(savedRegistryState)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
      }

      override fun postCreateView(controller: Controller, view: View) {
        /**
         * If the consumer of the library already has its own [ViewTreeLifecycleOwner] or
         * [ViewTreeSavedStateRegistryOwner] set, don't overwrite it but assume that they're doing
         * it on purpose.
         */
        if (
          view.getTag(R.id.view_tree_lifecycle_owner) == null &&
          view.getTag(R.id.view_tree_saved_state_registry_owner) == null
        ) {
          ViewTreeLifecycleOwner.set(view, this@OwnViewTreeLifecycleAndRegistry)
          ViewTreeSavedStateRegistryOwner.set(
            view,
            this@OwnViewTreeLifecycleAndRegistry
          )
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
      }

      override fun postAttach(controller: Controller, view: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
      }

      override fun onChangeEnd(
        changeController: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        // Should only happen if pushing another controller over this one was aborted
        if (
          controller == changeController &&
          changeType.isEnter &&
          changeHandler.removesFromViewOnPush() &&
          changeController.view?.windowToken != null &&
          lifecycleRegistry.currentState == Lifecycle.State.STARTED
        ) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
      }

      // AbstractComposeView adds its own OnAttachStateChangeListener by default. Since it
      // does this on init, its detach callbacks get called before ours, which prevents us
      // from saving state in onDetach. The if statement in here should detect upcoming
      // detachment.
      override fun onChangeStart(
        changeController: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        if (
          controller == changeController &&
          !changeType.isEnter &&
          changeHandler.removesFromViewOnPush() &&
          lifecycleRegistry.currentState == Lifecycle.State.RESUMED
        ) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

          savedRegistryState = Bundle()
          savedStateRegistryController.performSave(savedRegistryState)

          hasSavedState = true
        }
      }

      override fun preDetach(controller: Controller, view: View) {
        // Should only happen if pushing this controller was aborted
        if (lifecycleRegistry.currentState == Lifecycle.State.RESUMED) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
      }

      override fun onSaveInstanceState(controller: Controller, outState: Bundle) {
        outState.putBundle(KEY_SAVED_STATE, savedRegistryState)
      }

      override fun onSaveViewState(controller: Controller, outState: Bundle) {
        if (!hasSavedState) {
          savedRegistryState = Bundle()
          savedStateRegistryController.performSave(savedRegistryState)
        }
      }

      override fun onRestoreInstanceState(controller: Controller, savedInstanceState: Bundle) {
        savedRegistryState = savedInstanceState.getBundle(KEY_SAVED_STATE)
      }

      override fun preDestroyView(controller: Controller, view: View) {
        if (controller.isBeingDestroyed && controller.router.backstackSize == 0) {
          val parent = view.parent as? View
          parent?.addOnAttachStateChangeListener(object :
            View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) = Unit
            override fun onViewDetachedFromWindow(v: View?) {
              parent.removeOnAttachStateChangeListener(this)
              lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
          })
        } else {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
      }
    })
  }

  override fun getLifecycle() = lifecycleRegistry

  override fun getSavedStateRegistry() = savedStateRegistryController.savedStateRegistry

  companion object {
    private const val KEY_SAVED_STATE = "Registry.savedState"

    fun own(target: Controller) {
      OwnViewTreeLifecycleAndRegistry(target)
    }
  }
}
