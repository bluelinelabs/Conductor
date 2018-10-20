package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindViews
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController

class MultipleChildRouterController : BaseController() {

    @BindViews(R.id.container_0, R.id.container_1, R.id.container_2)
    internal var childContainers: Array<ViewGroup>? = null

    override val title: String
        get() = "Child Router Demo"

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_multiple_child_routers, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        for (childContainer in childContainers!!) {
            val childRouter = getChildRouter(childContainer).setPopsLastView(false)
            if (!childRouter.hasRootController()) {
                childRouter.setRoot(RouterTransaction.with(NavigationDemoController(0, NavigationDemoController.DisplayUpMode.HIDE)))
            }
        }
    }

}
