package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.graphics.PorterDuff.Mode
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.changehandler.CityGridSharedElementTransitionChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder
import kotlinx.android.synthetic.main.controller_city_grid.view.*
import kotlinx.android.synthetic.main.row_city_grid.view.*
import java.util.*

class CityGridController(args: Bundle) : BaseController(args) {

    override var title: String = "Shared Element Demos"
    private val dotColor: Int
    private val fromPosition: Int

    constructor(title: String, dotColor: Int, fromPosition: Int) : this(BundleBuilder(Bundle())
            .putString(KEY_TITLE, title)
            .putInt(KEY_DOT_COLOR, dotColor)
            .putInt(KEY_FROM_POSITION, fromPosition)
            .build())

    init {
        title = getArgs().getString(KEY_TITLE)
        dotColor = getArgs().getInt(KEY_DOT_COLOR)
        fromPosition = getArgs().getInt(KEY_FROM_POSITION)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_city_grid, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        view.tv_title.text = title
        view.img_dot!!.drawable.setColorFilter(ContextCompat.getColor(activity!!, dotColor), Mode.SRC_ATOP)

        ViewCompat.setTransitionName(view.tv_title, resources?.getString(R.string.transition_tag_title_indexed, fromPosition))
        ViewCompat.setTransitionName(view.img_dot, resources?.getString(R.string.transition_tag_dot_indexed, fromPosition))

        view.recycler_view.setHasFixedSize(true)
        view.recycler_view.layoutManager = GridLayoutManager(view.context, 2)
        view.recycler_view.adapter = CityGridAdapter(LayoutInflater.from(view.context), CITY_MODELS)
    }

    internal fun onModelRowClick(model: CityModel?) {
        val imageTransitionName = resources?.getString(R.string.transition_tag_image_named, model?.title)
                ?: ""
        val titleTransitionName = resources?.getString(R.string.transition_tag_title_named, model?.title)
                ?: ""

        val names = ArrayList<String>()
        names.add(imageTransitionName)
        names.add(titleTransitionName)

        router.pushController(RouterTransaction.with(CityDetailController(model!!.drawableRes, model.title))
                .pushChangeHandler(TransitionChangeHandlerCompat(CityGridSharedElementTransitionChangeHandler(names), FadeChangeHandler()))
                .popChangeHandler(TransitionChangeHandlerCompat(CityGridSharedElementTransitionChangeHandler(names), FadeChangeHandler())))
    }

    internal inner class CityGridAdapter(private val inflater: LayoutInflater, private val items: Array<CityModel>) : RecyclerView.Adapter<CityGridAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_city_grid, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            @BindView(R.id.tv_title)
            var textView: TextView? = null
            @BindView(R.id.img_city)
            var imageView: ImageView? = null
            private var model: CityModel? = null

            init {
                ButterKnife.bind(this, itemView)
            }

            fun bind(item: CityModel) {
                model = item
                itemView.img_city.setImageResource(item.drawableRes)
                itemView.tv_row_title.text = item.title

                ViewCompat.setTransitionName(textView, resources?.getString(R.string.transition_tag_title_named, model?.title))
                ViewCompat.setTransitionName(imageView, resources?.getString(R.string.transition_tag_image_named, model?.title))

                itemView.row_root.setOnClickListener { onModelRowClick(model) }
            }

        }
    }

    class CityModel(@param:DrawableRes @field:DrawableRes internal var drawableRes: Int, internal var title: String)

    companion object {

        private val KEY_TITLE = "CityGridController.title"
        private val KEY_DOT_COLOR = "CityGridController.dotColor"
        private val KEY_FROM_POSITION = "CityGridController.position"

        private val CITY_MODELS = arrayOf(CityModel(R.drawable.chicago, "Chicago"), CityModel(R.drawable.jakarta, "Jakarta"), CityModel(R.drawable.london, "London"), CityModel(R.drawable.sao_paulo, "Sao Paulo"), CityModel(R.drawable.tokyo, "Tokyo"))
    }
}
