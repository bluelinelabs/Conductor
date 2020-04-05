package com.bluelinelabs.conductor

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.bluelinelabs.conductor.internal.TransactionIndexer

private const val INVALID_INDEX = -1
private const val KEY_VIEW_CONTROLLER_BUNDLE = "RouterTransaction.controller.bundle"
private const val KEY_PUSH_TRANSITION = "RouterTransaction.pushControllerChangeHandler"
private const val KEY_POP_TRANSITION = "RouterTransaction.popControllerChangeHandler"
private const val KEY_TAG = "RouterTransaction.tag"
private const val KEY_IS_DETAIL = "RouterTransaction.isDetail"
private const val KEY_INDEX = "RouterTransaction.transactionIndex"
private const val KEY_ATTACHED_TO_ROUTER = "RouterTransaction.attachedToRouter"

/**
 * Metadata used for adding [Controller]s to a [Router].
 */
class RouterTransaction
private constructor(
  @get:JvmName("controller")
  val controller: Controller,
  private var tag: String? = null,
  private var pushControllerChangeHandler: ControllerChangeHandler? = null,
  private var popControllerChangeHandler: ControllerChangeHandler? = null,
  private var attachedToRouter: Boolean = false,
  private var isDetail: Boolean = false,
  @RestrictTo(LIBRARY)
  var transactionIndex: Int = INVALID_INDEX
) {


  @RestrictTo(LIBRARY)
  internal constructor(bundle: Bundle) : this(
    controller = Controller.newInstance(bundle.getBundle(KEY_VIEW_CONTROLLER_BUNDLE)!!),
    pushControllerChangeHandler = ControllerChangeHandler.fromBundle(
      bundle.getBundle(
        KEY_PUSH_TRANSITION
      )
    ),
    popControllerChangeHandler = ControllerChangeHandler.fromBundle(
      bundle.getBundle(
        KEY_POP_TRANSITION
      )
    ),
    tag = bundle.getString(KEY_TAG),
    isDetail = bundle.getBoolean(KEY_IS_DETAIL),
    transactionIndex = bundle.getInt(KEY_INDEX),
    attachedToRouter = bundle.getBoolean(KEY_ATTACHED_TO_ROUTER)
  )

  fun onAttachedToRouter() {
    attachedToRouter = true
  }

  fun tag(): String? = tag

  fun tag(tag: String?): RouterTransaction {
    return if (!attachedToRouter) {
      this.tag = tag
      this
    } else {
      throw RuntimeException(javaClass.simpleName + "s can not be modified after being added to a Router.")
    }
  }

  fun isDetail(isDetail: Boolean): RouterTransaction {
    this.isDetail = isDetail
    this.controller.isDetail = isDetail
    return this
  }

  fun isDetail(): Boolean {
    return isDetail
  }

  fun pushChangeHandler(): ControllerChangeHandler? {
    return controller.overriddenPushHandler ?: pushControllerChangeHandler
  }

  fun pushChangeHandler(handler: ControllerChangeHandler?): RouterTransaction {
    return if (!attachedToRouter) {
      pushControllerChangeHandler = handler
      this
    } else {
      throw RuntimeException("${javaClass.simpleName}s can not be modified after being added to a Router.")
    }
  }

  fun popChangeHandler(): ControllerChangeHandler? {
    return controller.overriddenPopHandler ?: popControllerChangeHandler
  }

  fun popChangeHandler(handler: ControllerChangeHandler?): RouterTransaction {
    return if (!attachedToRouter) {
      popControllerChangeHandler = handler
      this
    } else {
      throw RuntimeException("${javaClass.simpleName}s can not be modified after being added to a Router.")
    }
  }

  fun ensureValidIndex(indexer: TransactionIndexer) {
    if (transactionIndex == INVALID_INDEX) {
      transactionIndex = indexer.nextIndex()
    }
  }

  /**
   * Used to serialize this transaction into a Bundle
   */
  fun saveInstanceState(): Bundle = Bundle().apply {
    putBundle(KEY_VIEW_CONTROLLER_BUNDLE, controller.saveInstanceState())
    pushControllerChangeHandler?.let { putBundle(KEY_PUSH_TRANSITION, it.toBundle()) }
    popControllerChangeHandler?.let { putBundle(KEY_POP_TRANSITION, it.toBundle()) }
    putString(KEY_TAG, tag)
    putBoolean(KEY_IS_DETAIL, isDetail)
    putInt(KEY_INDEX, transactionIndex)
    putBoolean(KEY_ATTACHED_TO_ROUTER, attachedToRouter)
  }

  companion object {

    @JvmStatic
    fun with(controller: Controller): RouterTransaction = RouterTransaction(controller)
  }
}

fun Controller.asTransaction(
  popChangeHandler: ControllerChangeHandler? = null,
  pushChangeHandler: ControllerChangeHandler? = null
): RouterTransaction {
  return RouterTransaction.with(this)
    .pushChangeHandler(pushChangeHandler)
    .popChangeHandler(popChangeHandler)
}
