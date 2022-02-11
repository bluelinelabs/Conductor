package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerDialogBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class DialogController(args: Bundle) : BaseController(R.layout.controller_dialog, args) {
  private val binding: ControllerDialogBinding by viewBinding(ControllerDialogBinding::bind)

  constructor(title: CharSequence, description: CharSequence) : this(
    bundleOf(
      KEY_TITLE to title,
      KEY_DESCRIPTION to description
    )
  )

  override fun onViewCreated(view: View) {
    binding.title.text = args.getCharSequence(KEY_TITLE)
    binding.description.text = args.getCharSequence(KEY_DESCRIPTION)
    binding.description.movementMethod = LinkMovementMethod.getInstance()
    binding.next.setOnClickListener {
      val backstack = router.backstack.plus(
        RouterTransaction.with(TextController("Came here via setBackstack - Now test back button!"))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
      router.setBackstack(backstack, backstack.last().pushChangeHandler())
    }
    binding.nextPush.setOnClickListener {
      router.pushController(
        RouterTransaction.with(TextController("Came here via pushController - Now test back button!"))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }
    binding.dismiss.setOnClickListener { router.popController(this) }
    binding.dialogBackground.setOnClickListener { router.popController(this) }
  }

  companion object {
    private const val KEY_TITLE = "DialogController.title"
    private const val KEY_DESCRIPTION = "DialogController.description"
  }
}