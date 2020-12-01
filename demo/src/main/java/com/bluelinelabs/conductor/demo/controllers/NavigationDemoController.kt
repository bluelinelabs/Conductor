package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerNavigationDemoBinding
import com.bluelinelabs.conductor.demo.util.getMaterialColor
import com.bluelinelabs.conductor.demo.util.viewBinding

class NavigationDemoController(args: Bundle) : BaseController(R.layout.controller_navigation_demo, args) {
  private val binding: ControllerNavigationDemoBinding by viewBinding(ControllerNavigationDemoBinding::bind)

  private val index = args.getInt(KEY_INDEX)
  private val displayUpMode = DisplayUpMode.values()[args.getInt(KEY_DISPLAY_UP_MODE)]

  override val title = "Navigation Demos"

  constructor(index: Int, displayUpMode: DisplayUpMode) : this(
    bundleOf(
      KEY_INDEX to index,
      KEY_DISPLAY_UP_MODE to displayUpMode.ordinal
    )
  )

  override fun onViewCreated(view: View) {
    binding.root.setBackgroundColor(view.resources.getMaterialColor(index))
    binding.goUp.isVisible = displayUpMode == DisplayUpMode.SHOW
    binding.title.text = view.resources.getString(R.string.navigation_title, index)

    binding.popToRoot.setOnClickListener { router.popToRoot() }
    binding.goUp.setOnClickListener { router.popToTag(TAG_UP_TRANSACTION) }
    binding.goToNext.setOnClickListener {
      router.pushController(
        RouterTransaction.with(NavigationDemoController(index + 1, displayUpMode.displayUpModeForChild))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }
  }

  override fun onChangeEnded(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    super.onChangeEnded(changeHandler, changeType)
    if (changeType.isEnter) {
      setButtonsEnabled(true)
    }
  }

  override fun onChangeStarted(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    super.onChangeStarted(changeHandler, changeType)
    setButtonsEnabled(false)
  }

  private fun setButtonsEnabled(enabled: Boolean) {
    binding.goToNext.isEnabled = enabled
    binding.goUp.isEnabled = enabled
    binding.popToRoot.isEnabled = enabled
  }

  companion object {
    const val TAG_UP_TRANSACTION = "NavigationDemoController.up"

    private const val KEY_INDEX = "NavigationDemoController.index"
    private const val KEY_DISPLAY_UP_MODE = "NavigationDemoController.displayUpMode"
  }

  enum class DisplayUpMode {
    SHOW, SHOW_FOR_CHILDREN_ONLY, HIDE;

    val displayUpModeForChild: DisplayUpMode
      get() = when (this) {
        HIDE -> HIDE
        else -> SHOW
      }
  }
}