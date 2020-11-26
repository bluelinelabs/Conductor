package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerChildBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class ChildController(args: Bundle) : BaseController(R.layout.controller_child, args) {
  private val binding: ControllerChildBinding by viewBinding(ControllerChildBinding::bind)

  constructor(title: String, backgroundColor: Int, colorIsResId: Boolean) : this(
    bundleOf(
      KEY_TITLE to title,
      KEY_BG_COLOR to backgroundColor,
      KEY_COLOR_IS_RES to colorIsResId
    )
  )

  override fun onViewCreated(view: View) {
    binding.title.text = args.getString(KEY_TITLE)

    val bgColor = args.getInt(KEY_BG_COLOR)
    if (args.getBoolean(KEY_COLOR_IS_RES)) {
      view.setBackgroundColor(ContextCompat.getColor(view.context, bgColor))
    } else {
      view.setBackgroundColor(bgColor)
    }
  }

  companion object {
    private const val KEY_TITLE = "ChildController.title"
    private const val KEY_BG_COLOR = "ChildController.bgColor"
    private const val KEY_COLOR_IS_RES = "ChildController.colorIsResId"
  }
}