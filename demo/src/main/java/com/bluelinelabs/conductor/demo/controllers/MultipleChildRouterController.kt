package com.bluelinelabs.conductor.demo.controllers

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerMultipleChildRoutersBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class MultipleChildRouterController : BaseController(R.layout.controller_multiple_child_routers) {
  private val binding: ControllerMultipleChildRoutersBinding by viewBinding(ControllerMultipleChildRoutersBinding::bind)

  override val title = "Child Router Demo"

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    val childContainers = listOf(binding.container0, binding.container1, binding.container2)

    childContainers.forEach { container ->
      val childRouter = getChildRouter(container).setPopsLastView(false)
      if (!childRouter.hasRootController()) {
        childRouter.setRoot(RouterTransaction.with(NavigationDemoController(0, NavigationDemoController.DisplayUpMode.HIDE)))
      }
    }
  }
}