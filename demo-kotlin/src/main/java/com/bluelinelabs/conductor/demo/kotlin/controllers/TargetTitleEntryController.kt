package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import butterknife.BindView
import butterknife.OnClick
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController

class TargetTitleEntryController<T : Controller>(targetController: T) : BaseController() {

    @BindView(R.id.edit_text)
    internal var editText: EditText? = null

    override val title: String
        get() = "Target Controller Demo"

    interface TargetTitleEntryControllerListener {
        fun onTitlePicked(option: String)
    }

    init {
        setTargetController(targetController)
    }

    override fun onDetach(view: View) {
        val imm = editText!!.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText!!.windowToken, 0)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_target_title_entry, container, false)
    }

    @OnClick(R.id.btn_use_title)
    internal fun optionPicked() {
        val targetController = targetController
        if (targetController != null) {
            (targetController as TargetTitleEntryControllerListener).onTitlePicked(editText!!.text.toString())
            router.popController(this)
        }
    }
}
