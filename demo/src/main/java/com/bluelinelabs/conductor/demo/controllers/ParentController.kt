package com.bluelinelabs.conductor.demo.controllers

import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerParentBinding
import com.bluelinelabs.conductor.demo.util.getMaterialColor
import com.bluelinelabs.conductor.demo.util.viewBinding


class ParentController : BaseController(R.layout.controller_parent) {
  private val binding: ControllerParentBinding by viewBinding(ControllerParentBinding::bind)

  override val title = "Parent/Child Demo"

  private var hasShownAll = false
  private var animatingOut = false

  override fun onChangeEnded(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    if (changeType == ControllerChangeType.PUSH_ENTER) {
      addChild(0)
    }
  }

  override fun handleBack(): Boolean {
    val childControllers = childRouters.filter { it.hasRootController() }.size

    return if (childControllers != NUMBER_OF_CHILDREN || animatingOut) {
      true
    } else {
      animatingOut = true
      super.handleBack()
    }
  }

  private fun addChild(index: Int) {
    val container = when (index) {
      0 -> binding.childContent1
      1 -> binding.childContent2
      2 -> binding.childContent3
      3 -> binding.childContent4
      4 -> binding.childContent5
      else -> throw IllegalStateException("Invalid child index $index")
    }

    val childRouter = getChildRouter(container).setPopsLastView(true)
    if (!childRouter.hasRootController()) {
      val child = ChildController(
        title = "Child Controller #$index",
        backgroundColor = resources!!.getMaterialColor(index),
        colorIsResId = false
      )
      child.addLifecycleListener(object : LifecycleListener() {
        override fun onChangeEnd(
          controller: Controller,
          changeHandler: ControllerChangeHandler,
          changeType: ControllerChangeType
        ) {
          if (!isBeingDestroyed) {
            if (changeType == ControllerChangeType.PUSH_ENTER && !hasShownAll) {
              if (index < NUMBER_OF_CHILDREN - 1) {
                addChild(index + 1)
              } else {
                hasShownAll = true
              }
            } else if (changeType == ControllerChangeType.POP_EXIT) {
              if (index > 0) {
                removeChild(index - 1)
              } else {
                router.popController(this@ParentController)
              }
            }
          }
        }
      })

      childRouter.setRoot(
        RouterTransaction.with(child)
          .pushChangeHandler(FadeChangeHandler())
          .popChangeHandler(FadeChangeHandler())
      )
    }
  }

  private fun removeChild(index: Int) {
    val childRouters = childRouters
    if (index < childRouters.size) {
      removeChildRouter(childRouters[index])
    }
  }

  companion object {
    private const val NUMBER_OF_CHILDREN = 5
  }
}