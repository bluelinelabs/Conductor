package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder

class TextController : BaseController {

    @BindView(R.id.text_view)
    internal var textView: TextView? = null

    constructor(text: String) : this(BundleBuilder(Bundle())
            .putString(KEY_TEXT, text)
            .build()
    )

    constructor(args: Bundle) : super(args)

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_text, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        textView!!.text = args.getString(KEY_TEXT)
    }

    companion object {

        private val KEY_TEXT = "TextController.text"
    }

}
