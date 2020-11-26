package com.bluelinelabs.conductor.demo.controllers

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerAdditionalModulesBinding
import com.bluelinelabs.conductor.demo.databinding.RowHomeBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class ExternalModulesController : BaseController(R.layout.controller_additional_modules) {
  private val binding: ControllerAdditionalModulesBinding by viewBinding(ControllerAdditionalModulesBinding::bind)

  override val title = "External Module Demos"

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    binding.recyclerView.setHasFixedSize(true)
    binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
    binding.recyclerView.adapter = AdditionalModulesAdapter(
      LayoutInflater.from(view.context),
      ModuleModel.values(),
      ::onModelRowClick
    )
  }

  private fun onModelRowClick(model: ModuleModel) {
    when (model) {
      ModuleModel.AUTODISPOSE -> router.pushController(
        with(AutodisposeController())
          .pushChangeHandler(FadeChangeHandler())
          .popChangeHandler(FadeChangeHandler())
      )
      ModuleModel.RX_LIFECYCLE_2 -> router.pushController(
        with(RxLifecycle2Controller())
          .pushChangeHandler(FadeChangeHandler())
          .popChangeHandler(FadeChangeHandler())
      )
      ModuleModel.ARCH_LIFECYCLE -> router.pushController(
        with(ArchLifecycleController())
          .pushChangeHandler(FadeChangeHandler())
          .popChangeHandler(FadeChangeHandler())
      )
    }
  }
}

private enum class ModuleModel(val title: String, @ColorRes val color: Int) {
  AUTODISPOSE("Autodispose", R.color.purple_300),
  RX_LIFECYCLE_2("Rx Lifecycle 2", R.color.blue_grey_300),
  ARCH_LIFECYCLE("Arch Components Lifecycle", R.color.orange_300);
}

private class AdditionalModulesAdapter(
  private val inflater: LayoutInflater,
  private val items: Array<ModuleModel>,
  private val modelClickListener: (ModuleModel) -> Unit
) : RecyclerView.Adapter<AdditionalModulesAdapter.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(RowHomeBinding.inflate(inflater, parent, false), modelClickListener)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int {
    return items.size
  }

  class ViewHolder(
    private val binding: RowHomeBinding,
    private val modelClickListener: (ModuleModel) -> Unit
  ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ModuleModel) {
      binding.title.text = item.title
      binding.dot.drawable.setColorFilter(ContextCompat.getColor(itemView.context, item.color), PorterDuff.Mode.SRC_ATOP)
      itemView.setOnClickListener { modelClickListener(item) }
    }
  }
}