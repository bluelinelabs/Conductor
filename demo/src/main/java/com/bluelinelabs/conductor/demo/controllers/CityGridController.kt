package com.bluelinelabs.conductor.demo.controllers

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.changehandler.CityGridSharedElementTransitionChangeHandler
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerCityGridBinding
import com.bluelinelabs.conductor.demo.databinding.RowCityGridBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class CityGridController(args: Bundle) : BaseController(R.layout.controller_city_grid, args) {
  private val binding: ControllerCityGridBinding by viewBinding(ControllerCityGridBinding::bind)

  override val title = "Shared Element Demos"

  constructor(title: String?, dotColor: Int, fromPosition: Int) : this(
    bundleOf(
      KEY_TITLE to title,
      KEY_DOT_COLOR to dotColor,
      KEY_FROM_POSITION to fromPosition
    )
  )

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    binding.title.text = args.getString(KEY_TITLE)!!
    binding.dot.drawable.setColorFilter(ContextCompat.getColor(view.context, args.getInt(KEY_DOT_COLOR)), PorterDuff.Mode.SRC_ATOP)

    binding.title.transitionName = view.resources.getString(R.string.transition_tag_title_indexed, args.getInt(KEY_FROM_POSITION))
    binding.dot.transitionName = view.resources.getString(R.string.transition_tag_dot_indexed, args.getInt(KEY_FROM_POSITION))

    binding.recyclerView.setHasFixedSize(true)
    binding.recyclerView.layoutManager = GridLayoutManager(view.context, 2)
    binding.recyclerView.adapter = CityGridAdapter(LayoutInflater.from(view.context), CITY_MODELS, ::onModelRowClick)
  }

  private fun onModelRowClick(model: CityModel) {
    val names = listOf(
      resources!!.getString(R.string.transition_tag_title_named, model.title),
      resources!!.getString(R.string.transition_tag_image_named, model.title)
    )

    router.pushController(
      RouterTransaction.with(CityDetailController(model.drawableRes, model.title))
        .pushChangeHandler(CityGridSharedElementTransitionChangeHandler(names))
        .popChangeHandler(CityGridSharedElementTransitionChangeHandler(names))
    )
  }

  companion object {
    private const val KEY_TITLE = "CityGridController.title"
    private const val KEY_DOT_COLOR = "CityGridController.dotColor"
    private const val KEY_FROM_POSITION = "CityGridController.position"

    private val CITY_MODELS = arrayOf(
      CityModel(R.drawable.chicago, "Chicago"),
      CityModel(R.drawable.jakarta, "Jakarta"),
      CityModel(R.drawable.london, "London"),
      CityModel(R.drawable.sao_paulo, "Sao Paulo"),
      CityModel(R.drawable.tokyo, "Tokyo")
    )
  }
}

data class CityModel(@DrawableRes val drawableRes: Int, val title: String)

private class CityGridAdapter(
  private val inflater: LayoutInflater,
  private val items: Array<CityModel>,
  private val modelClickListener: (CityModel) -> Unit
) : RecyclerView.Adapter<CityGridAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      RowCityGridBinding.inflate(inflater, parent, false),
      modelClickListener
    )
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount() = items.size

  class ViewHolder(
    private val binding: RowCityGridBinding,
    private val modelClickListener: (CityModel) -> Unit
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CityModel) {
      binding.image.setImageResource(item.drawableRes)
      binding.title.text = item.title

      binding.image.transitionName = itemView.resources.getString(R.string.transition_tag_image_named, item.title)
      binding.image.transitionName = itemView.resources.getString(R.string.transition_tag_title_named, item.title)

      itemView.setOnClickListener { modelClickListener(item) }
    }
  }
}