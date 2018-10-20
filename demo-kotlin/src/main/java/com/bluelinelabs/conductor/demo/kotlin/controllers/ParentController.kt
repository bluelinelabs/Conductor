package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.ColorUtil

class ParentController : BaseController() {
    private var finishing: Boolean = false
    private var hasShownAll: Boolean = false

    override val title: String
        get() = "Parent/Child Demo"

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_parent, container, false)
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)

        if (changeType == ControllerChangeType.PUSH_ENTER) {
            addChild(0)
        }
    }

    private fun addChild(index: Int) {
        @IdRes val frameId = resources?.getIdentifier("child_content_" + (index + 1), "id", activity?.packageName)
        val container = view?.findViewById<ViewGroup>(frameId!!)!!
        val childRouter = getChildRouter(container).setPopsLastView(true)

        if (!childRouter.hasRootController()) {
            val childController = ChildController("Child Controller #$index", ColorUtil.getMaterialColor(resources!!, index), false)

            childController.addLifecycleListener(object : LifecycleListener() {
                override fun onChangeEnd(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
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

            childRouter.setRoot(RouterTransaction.with(childController)
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
        }
    }

    private fun removeChild(index: Int) {
        val childRouters = childRouters
        if (index < childRouters.size) {
            removeChildRouter(childRouters[index])
        }
    }

    override fun handleBack(): Boolean {
        var childControllers = 0
        for (childRouter in childRouters) {
            if (childRouter.hasRootController()) {
                childControllers++
            }
        }

        return if (childControllers != NUMBER_OF_CHILDREN || finishing) {
            true
        } else {
            finishing = true
            super.handleBack()
        }
    }

    companion object {

        private val NUMBER_OF_CHILDREN = 5
    }

}
