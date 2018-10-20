package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder

class ChildController(args: Bundle) : BaseController(args) {

    @BindView(R.id.tv_title)
    internal var tvTitle: TextView? = null

    constructor(title: String, backgroundColor: Int, colorIsResId: Boolean) : this(BundleBuilder(Bundle())
            .putString(KEY_TITLE, title)
            .putInt(KEY_BG_COLOR, backgroundColor)
            .putBoolean(KEY_COLOR_IS_RES, colorIsResId)
            .build())

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_child, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        tvTitle?.text = args.getString(KEY_TITLE)

        var bgColor = args.getInt(KEY_BG_COLOR)

        if (args.getBoolean(KEY_COLOR_IS_RES)) {
            bgColor = ContextCompat.getColor(activity!!, bgColor)
        }

        view.setBackgroundColor(bgColor)
    }

    companion object {

        private val KEY_TITLE = "ChildController.title"
        private val KEY_BG_COLOR = "ChildController.bgColor"
        private val KEY_COLOR_IS_RES = "ChildController.colorIsResId"
    }
}
