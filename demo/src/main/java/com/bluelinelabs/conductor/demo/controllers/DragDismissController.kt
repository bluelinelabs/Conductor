package com.bluelinelabs.conductor.demo.controllers

import android.view.View
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.changehandler.ScaleFadeChangeHandler
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.widget.ElasticDragDismissFrameLayout
import com.bluelinelabs.conductor.demo.widget.ElasticDragDismissFrameLayout.ElasticDragDismissCallback

class DragDismissController : BaseController(R.layout.controller_drag_dismiss) {
  override val title = "Drag to Dismiss"

  private val dragDismissListener: ElasticDragDismissCallback = object : ElasticDragDismissCallback() {
    override fun onDragDismissed() {
      overridePopHandler(ScaleFadeChangeHandler())
      router.popController(this@DragDismissController)
    }
  }

  override fun onViewCreated(view: View) {
    (view as ElasticDragDismissFrameLayout).addListener(dragDismissListener)
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    (view as ElasticDragDismissFrameLayout).removeListener(dragDismissListener)
  }
}