package com.bluelinelabs.conductor.demo.controllers

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerTargetTitleEntryBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class TargetTitleEntryController() : BaseController(R.layout.controller_target_title_entry) {
  private val binding: ControllerTargetTitleEntryBinding by viewBinding(ControllerTargetTitleEntryBinding::bind)

  override val title = "Target Controller Demo"

  constructor(targetController: TargetTitleEntryControllerListener) : this() {
    check(targetController is Controller)
    setTargetController(targetController)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    binding.useTitleButton.setOnClickListener {
      targetController?.let { listener ->
        (listener as TargetTitleEntryControllerListener).onTitlePicked(binding.editText.text.toString())
        router.popController(this)
      }
    }
  }

  override fun onDetach(view: View) {
    val imm = binding.editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
  }

  interface TargetTitleEntryControllerListener {
    fun onTitlePicked(option: String?)
  }
}