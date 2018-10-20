package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.annotation.TargetApi
import android.os.Build.VERSION_CODES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.changehandler.ScaleFadeChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.widget.ElasticDragDismissFrameLayout

@TargetApi(VERSION_CODES.LOLLIPOP)
class DragDismissController : BaseController() {

    private val dragDismissListener = object : ElasticDragDismissFrameLayout.ElasticDragDismissCallback() {

        override fun onDragDismissed() {
            overridePopHandler(ScaleFadeChangeHandler())
            router.popController(this@DragDismissController)
        }
    }

    override val title: String
        get() = "Drag to Dismiss"

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_drag_dismiss, container, false)
    }

    override fun onViewBound(view: View) {
        (view as ElasticDragDismissFrameLayout).addListener(dragDismissListener)
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)

        (view as ElasticDragDismissFrameLayout).removeListener(dragDismissListener)
    }
}
