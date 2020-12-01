package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.changehandler.ArcFadeMoveChangeHandler
import com.bluelinelabs.conductor.demo.changehandler.CircularRevealChangeHandlerCompat
import com.bluelinelabs.conductor.demo.changehandler.FlipChangeHandler
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerTransitionDemoBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class TransitionDemoController(args: Bundle) : BaseController(R.layout.controller_transition_demo, args) {
  private val binding: ControllerTransitionDemoBinding by viewBinding(ControllerTransitionDemoBinding::bind)

  override val title = "Transition Demos"

  private val demo = TransitionDemo.values()[args.getInt(KEY_INDEX)]

  constructor(index: Int) : this(bundleOf(KEY_INDEX to index))

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    if (demo.layout == TransactionDemoLayout.SHARED) {
      binding.bgView.isGone = true
    }

    binding.title.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = view.resources.getDimension(demo.layout.topMargin).toInt() }
    binding.next.updateLayoutParams<FrameLayout.LayoutParams> { gravity = demo.layout.fabGravity }

    if (demo.colorId != 0) {
      binding.bgView.setBackgroundColor(ContextCompat.getColor(activity!!, demo.colorId))
    }

    val nextIndex = demo.ordinal + 1
    val buttonColor = if (nextIndex < TransitionDemo.values().size) {
      TransitionDemo.values()[nextIndex].colorId
    } else {
      R.color.blue_grey_300
    }

    binding.next.backgroundTintList = ContextCompat.getColorStateList(view.context, buttonColor)
    binding.title.text = demo.title

    binding.next.setOnClickListener {
      if (nextIndex < TransitionDemo.values().size) {
        router.pushController(getRouterTransaction(nextIndex, this))
      } else {
        router.popToRoot()
      }
    }
  }

  private fun getChangeHandler(from: Controller): ControllerChangeHandler {
    return when (demo) {
      TransitionDemo.VERTICAL -> VerticalChangeHandler()
      TransitionDemo.CIRCULAR -> {
        val demoController = from as TransitionDemoController
        CircularRevealChangeHandlerCompat(demoController.binding.next, demoController.binding.transitionRoot)
      }
      TransitionDemo.FADE -> FadeChangeHandler()
      TransitionDemo.FLIP -> FlipChangeHandler()
      TransitionDemo.ARC_FADE -> ArcFadeMoveChangeHandler(
        from.resources!!.getString(R.string.transition_tag_dot), from.resources!!.getString(R.string.transition_tag_title)
      )
      TransitionDemo.ARC_FADE_RESET -> ArcFadeMoveChangeHandler(
        from.resources!!.getString(R.string.transition_tag_dot), from.resources!!.getString(R.string.transition_tag_title)
      )
      TransitionDemo.HORIZONTAL -> HorizontalChangeHandler()
    }
  }

  companion object {
    private const val KEY_INDEX = "TransitionDemoController.index"

    fun getRouterTransaction(index: Int, fromController: Controller): RouterTransaction {
      val toController = TransitionDemoController(index)
      return RouterTransaction.with(toController)
        .pushChangeHandler(toController.getChangeHandler(fromController))
        .popChangeHandler(toController.getChangeHandler(fromController))
    }
  }
}

enum class TransactionDemoLayout(@DimenRes val topMargin: Int, val fabGravity: Int) {
  STANDARD(R.dimen.transition_margin_top_standard, Gravity.BOTTOM or Gravity.END),
  SHARED(R.dimen.transition_margin_top_shared, Gravity.CENTER)
}

enum class TransitionDemo(val title: String, val layout: TransactionDemoLayout, @ColorRes val colorId: Int) {
  VERTICAL(
    "Vertical Slide Animation",
    TransactionDemoLayout.STANDARD,
    R.color.blue_grey_300
  ),
  CIRCULAR(
    "Circular Reveal Animation",
    TransactionDemoLayout.STANDARD,
    R.color.red_300
  ),
  FADE("Fade Animation",
    TransactionDemoLayout.STANDARD,
    R.color.blue_300
  ),
  FLIP(
    "Flip Animation",
    TransactionDemoLayout.STANDARD,
    R.color.deep_orange_300
  ),
  HORIZONTAL(
    "Horizontal Slide Animation",
    TransactionDemoLayout.STANDARD,
    R.color.green_300
  ),
  ARC_FADE(
    "Arc/Fade Shared Element Transition",
    TransactionDemoLayout.SHARED,
    R.color.blue_grey_300
  ),
  ARC_FADE_RESET(
    "Arc/Fade Shared Element Transition",
    TransactionDemoLayout.STANDARD,
    R.color.pink_300
  )
}