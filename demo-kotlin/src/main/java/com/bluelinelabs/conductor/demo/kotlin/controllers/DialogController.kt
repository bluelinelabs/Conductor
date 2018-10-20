package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder
import kotlinx.android.synthetic.main.controller_dialog.view.*

class DialogController(args: Bundle) : BaseController(args) {

    constructor(title: CharSequence, description: CharSequence) : this(BundleBuilder(Bundle())
            .putCharSequence(KEY_TITLE, title)
            .putCharSequence(KEY_DESCRIPTION, description)
            .build())

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_dialog, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        view.tv_title.text = args.getCharSequence(KEY_TITLE)
        view.tv_description.text = args.getCharSequence(KEY_DESCRIPTION)
        view.tv_description.movementMethod = LinkMovementMethod.getInstance()

        view.dismiss.setOnClickListener { router.popController(this) }
        view.dialog_window.setOnClickListener { router.popController(this) }
    }

    companion object {

        private val KEY_TITLE = "DialogController.title"
        private val KEY_DESCRIPTION = "DialogController.description"
    }
}
