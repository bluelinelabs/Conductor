package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerTextBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class TextController(args: Bundle) : BaseController(R.layout.controller_text, args) {
  private val binding: ControllerTextBinding by viewBinding(ControllerTextBinding::bind)

  constructor(text: String) : this(bundleOf(KEY_TEXT to text))

  override fun onViewCreated(view: View) {
    binding.textView.text = args.getString(KEY_TEXT)
  }

  companion object {
    private const val KEY_TEXT = "TextController.text"
  }
}