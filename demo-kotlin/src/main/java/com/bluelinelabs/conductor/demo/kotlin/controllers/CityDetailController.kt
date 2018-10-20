package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder

class CityDetailController(args: Bundle) : BaseController(args) {

    @BindView(R.id.recycler_view)
    internal var recyclerView: RecyclerView? = null

    @DrawableRes
    private val imageDrawableRes: Int
    override val title: String

    constructor(@DrawableRes imageDrawableRes: Int, title: String) : this(BundleBuilder(Bundle())
            .putInt(KEY_IMAGE, imageDrawableRes)
            .putString(KEY_TITLE, title)
            .build())

    init {
        imageDrawableRes = getArgs().getInt(KEY_IMAGE)
        title = getArgs().getString(KEY_TITLE)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_city_detail, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(view.context)
        recyclerView!!.adapter = CityDetailAdapter(LayoutInflater.from(view.context), title, imageDrawableRes, LIST_ROWS, title)
    }

    internal class CityDetailAdapter(private val inflater: LayoutInflater, private val title: String, @param:DrawableRes @field:DrawableRes private val imageDrawableRes: Int, private val details: Array<String>, transitionNameBase: String) : RecyclerView.Adapter<CityDetailAdapter.ViewHolder>() {

        private val imageViewTransitionName: String = inflater.context.resources.getString(R.string.transition_tag_image_named, transitionNameBase)
        private val textViewTransitionName: String = inflater.context.resources.getString(R.string.transition_tag_title_named, transitionNameBase)

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                VIEW_TYPE_HEADER
            } else {
                VIEW_TYPE_DETAIL
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return if (viewType == VIEW_TYPE_HEADER) {
                HeaderViewHolder(inflater.inflate(R.layout.row_city_header, parent, false))
            } else {
                DetailViewHolder(inflater.inflate(R.layout.row_city_detail, parent, false))
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (getItemViewType(position) == VIEW_TYPE_HEADER) {
                (holder as HeaderViewHolder).bind(imageDrawableRes, title, imageViewTransitionName, textViewTransitionName)
            } else {
                (holder as DetailViewHolder).bind(details[position - 1])
            }
        }

        override fun getItemCount(): Int {
            return 1 + details.size
        }

        internal open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                ButterKnife.bind(this, itemView)
            }
        }

        internal class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {

            @BindView(R.id.image_view)
            var imageView: ImageView? = null
            @BindView(R.id.text_view)
            var textView: TextView? = null

            fun bind(@DrawableRes imageDrawableRes: Int, title: String, imageTransitionName: String, textViewTransitionName: String) {
                imageView!!.setImageResource(imageDrawableRes)
                textView!!.text = title

                ViewCompat.setTransitionName(imageView, imageTransitionName)
                ViewCompat.setTransitionName(textView, textViewTransitionName)
            }
        }

        internal class DetailViewHolder(itemView: View) : ViewHolder(itemView) {

            @BindView(R.id.text_view)
            var textView: TextView? = null

            fun bind(detail: String) {
                textView!!.text = detail
            }

        }

        companion object {

            private val VIEW_TYPE_HEADER = 0
            private val VIEW_TYPE_DETAIL = 1
        }
    }

    companion object {

        private val KEY_TITLE = "CityDetailController.title"
        private val KEY_IMAGE = "CityDetailController.image"

        private val LIST_ROWS = arrayOf("• This is a city.", "• There's some cool stuff about it.", "• But really this is just a demo, not a city guide app.", "• This demo is meant to show some nice transitions, as long as you're on Lollipop or later.", "• You should have seen some sweet shared element transitions using the ImageView and the TextView in the \"header\" above.", "• This transition utilized some callbacks to ensure all the necessary rows in the RecyclerView were laid about before the transition occurred.", "• Just adding some more lines so it scrolls now...\n\n\n\n\n\n\nThe end.")
    }
}
