package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController

class MasterDetailListController : BaseController() {

    @BindView(R.id.recycler_view)
    internal var recyclerView: RecyclerView? = null
    @BindView(R.id.detail_container)
    internal var detailContainer: ViewGroup? = null

    private var selectedIndex: Int = 0
    private var twoPaneView: Boolean = false

    override val title: String
        get() = "Master/Detail Flow"

    enum class DetailItemModel private constructor(internal var title: String, internal var detail: String, internal var backgroundColor: Int) {
        ONE("Item 1", "This is a quick demo of master/detail flow using Conductor. In portrait mode you'll see a standard list. In landscape, you'll see a two-pane layout.", R.color.green_300),
        TWO("Item 2", "This is another item.", R.color.cyan_300),
        THREE("Item 3", "Wow, a 3rd item!", R.color.deep_purple_300)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_master_detail_list, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(view.context)
        recyclerView!!.adapter = DetailItemAdapter(LayoutInflater.from(view.context), DetailItemModel.values())

        twoPaneView = detailContainer != null
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

    internal fun onRowSelected(index: Int) {
        selectedIndex = index

        val model = DetailItemModel.values()[index]
        val controller = ChildController(model.detail, model.backgroundColor, true)

        if (twoPaneView) {
            getChildRouter(detailContainer!!).setRoot(RouterTransaction.with(controller))
        } else {
            router.pushController(RouterTransaction.with(controller)
                    .pushChangeHandler(HorizontalChangeHandler())
                    .popChangeHandler(HorizontalChangeHandler()))
        }
    }

    internal inner class DetailItemAdapter(private val inflater: LayoutInflater, private val items: Array<DetailItemModel>) : RecyclerView.Adapter<DetailItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_detail_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            @BindView(R.id.row_root)
            var root: View? = null
            @BindView(R.id.tv_title)
            var tvTitle: TextView? = null

            init {
                ButterKnife.bind(this, itemView)
            }

            fun bind(item: DetailItemModel, position: Int) {
                tvTitle!!.text = item.title

                if (twoPaneView && position == selectedIndex) {
                    root!!.setBackgroundColor(ContextCompat.getColor(root!!.context, R.color.grey_400))
                } else {
                    root!!.setBackgroundColor(ContextCompat.getColor(root!!.context, android.R.color.transparent))
                }
            }

            @OnClick(R.id.row_root)
            fun onRowClick() {
                onRowSelected(position)
                notifyDataSetChanged()
            }

        }
    }

    companion object {

        private val KEY_SELECTED_INDEX = "MasterDetailListController.selectedIndex"
    }

}
