package com.bluelinelabs.conductor.demo.controllers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.TargetTitleEntryController.TargetTitleEntryControllerListener
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerTargetDisplayBinding
import com.bluelinelabs.conductor.demo.util.viewBinding
import com.squareup.picasso.Picasso

class TargetDisplayController : BaseController(R.layout.controller_target_display), TargetTitleEntryControllerListener {
  private val binding: ControllerTargetDisplayBinding by viewBinding(ControllerTargetDisplayBinding::bind)

  override val title = "Target Controller Demo"

  private var selectedText: String? = null
  private var imageUri: Uri? = null

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    setTextView()
    setImageView()

    binding.pickTitleButton.setOnClickListener {
      router.pushController(
        with(TargetTitleEntryController(this))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }

    binding.pickImageButton.setOnClickListener {
      val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      intent.type = "image/*"
      intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
      startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_SELECT_IMAGE)
    }
  }

  override fun onTitlePicked(option: String?) {
    selectedText = option
    setTextView()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
      imageUri = data?.data
      setImageView()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_SELECTED_TEXT, selectedText)
    outState.putString(KEY_SELECTED_IMAGE, if (imageUri != null) imageUri.toString() else null)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    selectedText = savedInstanceState.getString(KEY_SELECTED_TEXT)
    val uriString = savedInstanceState.getString(KEY_SELECTED_IMAGE)
    if (!uriString.isNullOrEmpty()) {
      imageUri = Uri.parse(uriString)
    }
  }

  private fun setImageView() {
    view ?: return

    Picasso.with(activity)
      .load(imageUri)
      .fit()
      .centerCrop()
      .into(binding.imageView)
  }

  private fun setTextView() {
    view ?: return

    if (!selectedText.isNullOrEmpty()) {
      binding.selection.text = selectedText
    } else {
      binding.selection.text = "Press pick title to set this title, or pick image to fill in the image view."
    }
  }

  companion object {
    private const val REQUEST_SELECT_IMAGE = 126
    private const val KEY_SELECTED_TEXT = "TargetDisplayController.selectedText"
    private const val KEY_SELECTED_IMAGE = "TargetDisplayController.selectedImage"
  }
}