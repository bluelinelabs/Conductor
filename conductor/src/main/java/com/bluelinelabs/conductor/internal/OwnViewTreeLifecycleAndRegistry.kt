package com.bluelinelabs.conductor.internal

import android.content.Context
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
  private val parentChangeListeners = mutableMapOf<String, Controller.LifecycleListener>()

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
          controller === changeController &&
          changeType.isEnter &&
          changeHandler.removesFromViewOnPush() &&
          changeController.view?.windowToken != null &&
          lifecycleRegistry.currentState == Lifecycle.State.STARTED
        ) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
      }

      override fun onChangeStart(
        changeController: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType,
      ) {
        pauseOnChangeStart(
          targetController = controller,
          changeController = changeController,
          changeHandler = changeHandler,
          changeType = changeType,
        )
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

      override fun postContextAvailable(controller: Controller, context: Context) {
        listenForParentChangeStart(controller)
      }

      override fun preContextUnavailable(controller: Controller, context: Context) {
        stopListeningForParentChangeStart(controller)
      }
    })
  }

  override fun getLifecycle() = lifecycleRegistry

  override fun getSavedStateRegistry() = savedStateRegistryController.savedStateRegistry

  private fun listenForParentChangeStart(controller: Controller) {
    controller.parentController?.let { parent ->
      val changeListener = object : Controller.LifecycleListener() {
        override fun onChangeStart(
          controller: Controller,
          changeHandler: ControllerChangeHandler,
          changeType: ControllerChangeType
        ) {
          pauseOnChangeStart(
            targetController = parent,
            changeController = controller,
            changeHandler = changeHandler,
            changeType = changeType,
          )
        }
      }

      parent.addLifecycleListener(changeListener)
      parentChangeListeners[controller.instanceId] = changeListener

      listenForParentChangeStart(parent)
    }
  }

  private fun stopListeningForParentChangeStart(controller: Controller) {
    controller.parentController?.let { parent ->
      parentChangeListeners.remove(parent.instanceId)?.let { listener ->
        parent.removeLifecycleListener(listener)
      }
    }
  }

  // AbstractComposeView adds its own OnAttachStateChangeListener by default. Since it
  // does this on init, its detach callbacks get called before ours, which prevents us
  // from saving state in onDetach. The if statement in here should detect upcoming
  // detachment.
  private fun pauseOnChangeStart(
    targetController: Controller,
    changeController: Controller,
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType,
  ) {
    if (
      targetController === changeController &&
      !changeType.isEnter &&
      changeHandler.removesFromViewOnPush() &&
      changeController.view != null &&
      lifecycleRegistry.currentState == Lifecycle.State.RESUMED
    ) {
      lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

      savedRegistryState = Bundle()
      savedStateRegistryController.performSave(savedRegistryState)

      hasSavedState = true
    }
  }

  companion object {
    private const val KEY_SAVED_STATE = "Registry.savedState"

    fun own(target: Controller): OwnViewTreeLifecycleAndRegistry {
      return OwnViewTreeLifecycleAndRegistry(target)
    }
  }
}
