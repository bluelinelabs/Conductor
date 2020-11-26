package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerMasterDetailListBinding
import com.bluelinelabs.conductor.demo.databinding.RowDetailItemBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class MasterDetailListController : BaseController(R.layout.controller_master_detail_list) {
  private val binding: ControllerMasterDetailListBinding by viewBinding(ControllerMasterDetailListBinding::bind)

  override val title = "Master/Detail Flow"

  private lateinit var adapter: DetailItemAdapter
  private var selectedIndex = 0
  private var twoPaneView = false

  private val dataModels = mutableListOf(
    DetailItemModel(
      "Item 1",
      "This is a quick demo of master/detail flow using Conductor. In portrait mode you'll see a standard list. In landscape, you'll see a two-pane layout.",
      R.color.green_300
    ),
    DetailItemModel(
      "Item 2",
      "This is another item.",
      R.color.cyan_300
    ),
    DetailItemModel(
      "Item 3",
      "Wow, a 3rd item!",
      R.color.deep_purple_300
    )
  )

  override fun onViewCreated(view: View) {
    adapter = DetailItemAdapter(LayoutInflater.from(view.context), dataModels, ::onRowSelected)

    binding.recyclerView.setHasFixedSize(true)
    binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
    binding.recyclerView.adapter = adapter
    twoPaneView = binding.detailContainer != null
    if (twoPaneView) {
      onRowSelected(selectedIndex)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX)
  }

  private fun onRowSelected(index: Int) {
    dataModels[selectedIndex] = dataModels[selectedIndex].copy(selected = false)
    selectedIndex = index
    dataModels[selectedIndex] = dataModels[selectedIndex].copy(selected = true)

    val model = dataModels[selectedIndex]
    val controller = ChildController(model.detail, model.backgroundColor, true)
    if (twoPaneView) {
      getChildRouter(binding.detailContainer!!).setRoot(with(controller))
    } else {
      router.pushController(
        with(controller)
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }
  }

  companion object {
    private const val KEY_SELECTED_INDEX = "MasterDetailListController.selectedIndex"
  }
}

data class DetailItemModel(val title: String, val detail: String, val backgroundColor: Int, val selected: Boolean = false)

class DetailItemAdapter(
  private val inflater: LayoutInflater,
  private val items: List<DetailItemModel>,
  private val rowClickListener: (Int) -> Unit
) : RecyclerView.Adapter<DetailItemAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(RowDetailItemBinding.inflate(inflater, parent, false)) { position ->
      rowClickListener(position)
      notifyDataSetChanged()
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position], position)
  }

  override fun getItemCount() = items.size

  class ViewHolder(
    private val binding: RowDetailItemBinding,
    private val rowClickListener: (Int) -> Unit
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DetailItemModel, position: Int) {
      binding.title.text = item.title

      if (item.selected) {
        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.grey_400))
      } else {
        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
      }

      itemView.setOnClickListener { rowClickListener(position) }
    }
  }
}