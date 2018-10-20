package com.bluelinelabs.conductor.demo.kotlin.controllers.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller

abstract class ButterKnifeController : Controller {

    private var unbinder: Unbinder? = null

    protected constructor()
    protected constructor(args: Bundle) : super(args)

    protected abstract fun inflateView(inflater: LayoutInflater, container: ViewGroup): View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflateView(inflater, container)
        unbinder = ButterKnife.bind(this, view)
        onViewBound(view)
        return view
    }

    open fun onViewBound(view: View) {}

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        unbinder!!.unbind()
        unbinder = null
    }

}
