package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.graphics.PorterDuff.Mode
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController

class ExternalModulesController : BaseController() {

    @BindView(R.id.recycler_view)
    internal var recyclerView: RecyclerView? = null

    override val title: String
        get() = "External Module Demos"

    enum class DemoModel private constructor(internal var title: String, @param:ColorRes @field:ColorRes internal var color: Int) {
        RX_LIFECYCLE("Rx Lifecycle", R.color.red_300),
        RX_LIFECYCLE_2("Rx Lifecycle 2", R.color.blue_grey_300),
        AUTODISPOSE("Autodispose", R.color.purple_300),
        ARCH_LIFECYCLE("Arch Components Lifecycle", R.color.orange_300)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_additional_modules, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(view.context)
        recyclerView!!.adapter = AdditionalModulesAdapter(LayoutInflater.from(view.context), DemoModel.values())
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        if (changeType.isEnter) {
            setTitle()
        }
    }

    internal fun onModelRowClick(model: DemoModel?) {
        when (model) {
            ExternalModulesController.DemoModel.RX_LIFECYCLE -> router.pushController(RouterTransaction.with(RxLifecycleController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
            ExternalModulesController.DemoModel.RX_LIFECYCLE_2 -> router.pushController(RouterTransaction.with(RxLifecycle2Controller())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
            ExternalModulesController.DemoModel.AUTODISPOSE -> router.pushController(RouterTransaction.with(AutodisposeController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
            ExternalModulesController.DemoModel.ARCH_LIFECYCLE -> router.pushController(RouterTransaction.with(ArchLifecycleController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
        }
    }

    internal inner class AdditionalModulesAdapter(private val inflater: LayoutInflater, private val items: Array<DemoModel>) : RecyclerView.Adapter<AdditionalModulesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_home, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            @BindView(R.id.tv_title)
            var tvTitle: TextView? = null
            @BindView(R.id.img_dot)
            var imgDot: ImageView? = null
            private var model: DemoModel? = null

            init {
                ButterKnife.bind(this, itemView)
            }

            fun bind(item: DemoModel) {
                model = item
                tvTitle!!.text = item.title
                imgDot!!.drawable.setColorFilter(ContextCompat.getColor(activity!!, item.color), Mode.SRC_ATOP)
            }

            @OnClick(R.id.row_root)
            fun onRowClick() {
                onModelRowClick(model)
            }

        }
    }

}