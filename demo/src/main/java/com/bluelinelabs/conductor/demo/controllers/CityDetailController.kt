package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerCityDetailBinding
import com.bluelinelabs.conductor.demo.databinding.RowCityDetailBinding
import com.bluelinelabs.conductor.demo.databinding.RowCityHeaderBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class CityDetailController(args: Bundle) : BaseController(R.layout.controller_city_detail, args) {
  private val binding: ControllerCityDetailBinding by viewBinding(ControllerCityDetailBinding::bind)

  override val title = args.getString(KEY_TITLE)!!

  constructor(@DrawableRes image: Int, title: String) : this(
    bundleOf(
      KEY_TITLE to title,
      KEY_IMAGE to image
    )
  )

  override fun onViewCreated(view: View) {
    binding.recyclerView.setHasFixedSize(true)
    binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
    binding.recyclerView.adapter = CityDetailAdapter(
      inflater = LayoutInflater.from(view.context),
      title = title,
      imageDrawableRes = args.getInt(KEY_IMAGE),
      details = LIST_ROWS,
      transitionNameBase = title
    )
  }

  companion object {
    private const val KEY_TITLE = "CityDetailController.title"
    private const val KEY_IMAGE = "CityDetailController.image"

    private val LIST_ROWS = listOf(
      "• This is a city.",
      "• There's some cool stuff about it.",
      "• But really this is just a demo, not a city guide app.",
      "• This demo is meant to show some nice transitions.",
      "• You should have seen some sweet shared element transitions using the ImageView and the TextView in the \"header\" above.",
      "• This transition utilized some callbacks to ensure all the necessary rows in the RecyclerView were laid about before the transition occurred.",
      "• Just adding some more lines so it scrolls now...\n\n\n\n\n\n\nThe end."
    )
  }

  class CityDetailAdapter(
    private val inflater: LayoutInflater,
    private val title: String,
    @DrawableRes private val imageDrawableRes: Int,
    private val details: List<String>,
    private val transitionNameBase: String
  ) : RecyclerView.Adapter<ViewHolder<*>>() {

    override fun getItemViewType(position: Int): Int {
      return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_DETAIL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
      return if (viewType == VIEW_TYPE_HEADER) {
        HeaderViewHolder(RowCityHeaderBinding.inflate(inflater, parent, false))
      } else {
        DetailViewHolder(RowCityDetailBinding.inflate(inflater, parent, false))
      }
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
      when (holder) {
        is HeaderViewHolder -> {
          holder.bind(
            imageDrawableRes = imageDrawableRes,
            title = title,
            imageTransitionName = inflater.context.resources.getString(R.string.transition_tag_image_named, transitionNameBase),
            textViewTransitionName = inflater.context.resources.getString(R.string.transition_tag_title_named, transitionNameBase)
          )
        }
        is DetailViewHolder -> {
          holder.bind(details[position - 1])
        }
        else -> {
          throw IllegalStateException("Invalid view holder class ${holder.javaClass.canonicalName}")
        }
      }
    }

    override fun getItemCount() = details.size + 1

    companion object {
      private const val VIEW_TYPE_HEADER = 0
      private const val VIEW_TYPE_DETAIL = 1
    }
  }

  open class ViewHolder<Binding : ViewBinding>(binding: Binding) : RecyclerView.ViewHolder(binding.root)

  class HeaderViewHolder(private val binding: RowCityHeaderBinding) : ViewHolder<RowCityHeaderBinding>(binding) {
    fun bind(@DrawableRes imageDrawableRes: Int, title: String?, imageTransitionName: String?, textViewTransitionName: String?) {
      binding.imageView.setImageResource(imageDrawableRes)
      binding.textView.text = title
      binding.imageView.transitionName = imageTransitionName
      binding.textView.transitionName = textViewTransitionName
    }
  }

  class DetailViewHolder(private val binding: RowCityDetailBinding) : ViewHolder<RowCityDetailBinding>(binding) {
    fun bind(detail: String) {
      binding.textView.text = detail
    }
  }
}