package com.bluelinelabs.conductor

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import com.bluelinelabs.conductor.internal.ClassUtils

/**
 * ControllerChangeHandlers are responsible for swapping the View for one Controller to the View
 * of another. They can be useful for performing animations and transitions between Controllers. Several
 * default ControllerChangeHandlers are included.
 */
abstract class ControllerChangeHandler {
  private var forceRemoveViewOnPush = false

  /**
   * Returns whether or not this is a reusable ControllerChangeHandler. Defaults to false and should
   * ONLY be overridden if there are absolutely no side effects to using this handler more than once.
   * In the case that a handler is not reusable, it will be copied using the [.copy] method
   * prior to use.
   */
  open val isReusable: Boolean = false

  /**
   * Returns whether or not this handler removes the `from` view from the container when performing a push.
   *
   * If this is true:
   * - This handler's implementation of [performChange] should remove `from` from `container`
   *   before calling `changeListener.onChangeCompleted()`
   * - When a controller is pushed, the previous controller will be detached and its view will be destroyed
   *
   * If this is false:
   * - This handler's implementation of [performChange] should only remove `from` from `container`
   *   when `isPush` is false
   * - When a controller is pushed, the previous controller will stay attached and its view will remain created
   * - When a view is recreated (e.g. after a configuration change), any controllers underneath a transaction
   *   using this handler will have their view recreated and attached, even though they're not the top-most
   *   controller
   *
   * If a controller pushed onto the backstack will completely cover the previous controller,
   * using a change handler with [removesFromViewOnPush] true should result in no visual interruption
   * to the user, while allowing the previous controller's view to be destroyed to reclaim resources.
   * If instead, the previous controller should still be visible after the new controller is pushed,
   * using a change handler with [removesFromViewOnPush] false will keep the previous controller's
   * view in the view hierarchy, where it can still be seen (and even interacted with).
   */
  open val removesFromViewOnPush: Boolean = true

  private var hasBeenUsed = false

  init {
    try {
      javaClass.getConstructor()
    } catch (e: Throwable) {
      throw RuntimeException("$javaClass does not have a default constructor.")
    }
  }

  /**
   * Responsible for swapping Views from one Controller to another.
   *
   * @param container      The container these Views are hosted in.
   * @param from           The previous View in the container or `null` if there was no Controller before this transition
   * @param to             The next View that should be put in the container or `null` if no Controller is being transitioned to
   * @param isPush         True if this is a push transaction, false if it's a pop.
   * @param changeListener This listener must be called when any transitions or animations are completed.
   */
  abstract fun performChange(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    changeListener: ControllerChangeCompletedListener,
  )

  /**
   * Saves any data about this handler to a Bundle in case the application is killed.
   *
   * @param bundle The Bundle into which data should be stored.
   */
  open fun saveToBundle(bundle: Bundle) {}

  /**
   * Restores data that was saved in the [.saveToBundle] method.
   *
   * @param bundle The bundle that has data to be restored
   */
  open fun restoreFromBundle(bundle: Bundle) {}

  /**
   * Will be called on change handlers that push a controller if the controller being pushed is
   * popped before it has completed.
   *
   * @param newHandler The change handler that has caused this push to be aborted
   * @param newTop     The Controller that will now be at the top of the backstack or `null`
   * if there will be no new Controller at the top
   */
  open fun onAbortPush(newHandler: ControllerChangeHandler, newTop: Controller?) {}

  /**
   * Will be called on change handlers that push a controller if the controller being pushed is
   * needs to be attached immediately, without any animations or transitions.
   */
  open fun completeImmediately() {}

  /**
   * Returns a copy of this ControllerChangeHandler. This method is internally used by the library, so
   * ensure it will return an exact copy of your handler if overriding. If not overriding, the handler
   * will be saved and restored from the Bundle format.
   */
  open fun copy(): ControllerChangeHandler = fromBundle(toBundle())!!

  open fun handleOnBackStarted(container: ViewGroup, to: View?, from: View, swipeEdge: Int) {}

  open fun handleOnBackProgressed(container: ViewGroup, to: View?, from: View, progress: Float, swipeEdge: Int) {}

  open fun handleOnBackCancelled(container: ViewGroup, to: View?, from: View) {}

  protected open fun onEnd() {}

  fun toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString(KEY_CLASS_NAME, javaClass.name)

    val savedState = Bundle()
    saveToBundle(savedState)
    bundle.putBundle(KEY_SAVED_STATE, savedState)

    return bundle
  }

  // Internal modifier plays weirdly with Java, which is what Router is still written in.
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  fun setForceRemoveViewOnPush(forceRemoveViewOnPush: Boolean) {
    this.forceRemoveViewOnPush = forceRemoveViewOnPush
  }

  /**
   * A listener interface useful for allowing external classes to be notified of change events.
   */
  interface ControllerChangeListener {
    /**
     * Called when a [ControllerChangeHandler] has started changing [Controller]s
     *
     * @param to        The new Controller or `null` if no Controller is being transitioned to
     * @param from      The old Controller or `null` if there was no Controller before this transition
     * @param isPush    True if this is a push operation, or false if it's a pop.
     * @param container The containing ViewGroup
     * @param handler   The change handler being used.
     */
    fun onChangeStarted(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup,
      handler: ControllerChangeHandler,
    )

    /**
     * Called when a [ControllerChangeHandler] has completed changing [Controller]s
     *
     * @param to        The new Controller or `null` if no Controller is being transitioned to
     * @param from      The old Controller or `null` if there was no Controller before this transition
     * @param isPush    True if this was a push operation, or false if it's a pop
     * @param container The containing ViewGroup
     * @param handler   The change handler that was used.
     */
    fun onChangeCompleted(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup,
      handler: ControllerChangeHandler,
    )
  }

  class ChangeTransaction(
    @JvmField val to: Controller?,
    @JvmField val from: Controller?,
    @JvmField val isPush: Boolean,
    @JvmField val container: ViewGroup?,
    @JvmField val changeHandler: ControllerChangeHandler?,
    @JvmField val listeners: List<ControllerChangeListener>,
  )

  /**
   * A simplified listener for being notified when the change is complete. This MUST be called by any custom
   * ControllerChangeHandlers in order to ensure that [Controller]s will be notified of this change.
   */
  interface ControllerChangeCompletedListener {
    /**
     * Called when the change is complete.
     */
    fun onChangeCompleted()
  }

  class ChangeHandlerData(val changeHandler: ControllerChangeHandler, val isPush: Boolean)

  companion object {
    private const val KEY_CLASS_NAME = "ControllerChangeHandler.className"
    private const val KEY_SAVED_STATE = "ControllerChangeHandler.savedState"
    val inProgressChangeHandlers: MutableMap<String, ChangeHandlerData> = HashMap()

    @JvmStatic
    fun fromBundle(bundle: Bundle?): ControllerChangeHandler? {
      val bundle = bundle ?: return null
      val className = bundle.getString(KEY_CLASS_NAME) ?: return null
      val savedState = bundle.getBundle(KEY_SAVED_STATE) ?: return null

      return ClassUtils.newInstance<ControllerChangeHandler>(className)?.also {
        it.restoreFromBundle(savedState)
      }
    }

    @JvmStatic
    fun completeHandlerImmediately(controllerInstanceId: String): Boolean {
      inProgressChangeHandlers[controllerInstanceId]?.let { changeHandlerData ->
        changeHandlerData.changeHandler.completeImmediately()
        inProgressChangeHandlers.remove(controllerInstanceId)
        return true
      }

      return false
    }

    fun abortOrComplete(toAbort: Controller, newController: Controller?, newChangeHandler: ControllerChangeHandler) {
      inProgressChangeHandlers[toAbort.getInstanceId()]?.let { changeHandlerData ->
        if (changeHandlerData.isPush) {
          changeHandlerData.changeHandler.onAbortPush(newChangeHandler, newController)
        } else {
          changeHandlerData.changeHandler.completeImmediately()
        }
        inProgressChangeHandlers.remove(toAbort.getInstanceId())
      }
    }

    @JvmStatic
    fun executeChange(transaction: ChangeTransaction) {
      executeChange(
        to = transaction.to,
        from = transaction.from,
        isPush = transaction.isPush,
        container = transaction.container,
        inHandler = transaction.changeHandler,
        listeners = transaction.listeners,
      )
    }

    private fun executeChange(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup?,
      inHandler: ControllerChangeHandler?,
      listeners: List<ControllerChangeListener>,
    ) {
      container ?: return

      val handler: ControllerChangeHandler = if (inHandler == null) {
        SimpleSwapChangeHandler()
      } else if (inHandler.hasBeenUsed && !inHandler.isReusable) {
        inHandler.copy()
      } else {
        inHandler
      }

      handler.hasBeenUsed = true

      if (from != null) {
        if (isPush) {
          completeHandlerImmediately(from.getInstanceId())
        } else {
          abortOrComplete(from, to, handler)
        }
      }

      if (to != null) {
        inProgressChangeHandlers[to.getInstanceId()] = ChangeHandlerData(handler, isPush)
      }

      listeners.forEach { it.onChangeStarted(to, from, isPush, container, handler) }

      val toChangeType = if (isPush) ControllerChangeType.PUSH_ENTER else ControllerChangeType.POP_ENTER
      val fromChangeType = if (isPush) ControllerChangeType.PUSH_EXIT else ControllerChangeType.POP_EXIT
      val toView = to?.let {
        it.inflate(container).also {
          to.changeStarted(handler, toChangeType)
        }
      }

      val fromView = from?.let {
        from.getView().also {
          from.changeStarted(handler, fromChangeType)
        }
      }

      handler.performChange(
        container = container,
        from = fromView,
        to = toView,
        isPush = isPush,
        changeListener = object : ControllerChangeCompletedListener {
          override fun onChangeCompleted() {
            from?.changeEnded(handler, fromChangeType)

            to?.let {
              inProgressChangeHandlers.remove(it.getInstanceId())
              it.changeEnded(handler, toChangeType)
            }

            listeners.forEach { it.onChangeCompleted(to, from, isPush, container, handler) }

            if (handler.forceRemoveViewOnPush) {
              (fromView?.parent as? ViewGroup)?.let {
                it.removeView(fromView)
              }
            }

            if (handler.removesFromViewOnPush) {
              from?.needsAttach = false
            }
          }
        },
      )
    }
  }
}
