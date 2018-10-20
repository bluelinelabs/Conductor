package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.content.Intent
import android.graphics.PorterDuff.Mode
import android.net.Uri
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.URLSpan
import android.view.*
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.changehandler.ArcFadeMoveChangeHandlerCompat
import com.bluelinelabs.conductor.demo.kotlin.changehandler.FabToDialogTransitionChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import kotlinx.android.synthetic.main.controller_home.view.*
import kotlinx.android.synthetic.main.row_home.view.*

class HomeController : BaseController() {

    override val title: String
        get() = "Conductor Demos"

    enum class DemoModel(internal var title: String, @param:ColorRes @field:ColorRes internal var color: Int) {
        NAVIGATION("Navigation Demos", R.color.red_300),
        TRANSITIONS("Transition Demos", R.color.blue_grey_300),
        SHARED_ELEMENT_TRANSITIONS("Shared Element Demos", R.color.purple_300),
        CHILD_CONTROLLERS("Child Controllers", R.color.orange_300),
        VIEW_PAGER("ViewPager", R.color.green_300),
        TARGET_CONTROLLER("Target Controller", R.color.pink_300),
        MULTIPLE_CHILD_ROUTERS("Multiple Child Routers", R.color.deep_orange_300),
        MASTER_DETAIL("Master Detail", R.color.grey_300),
        DRAG_DISMISS("Drag Dismiss", R.color.lime_300),
        EXTERNAL_MODULES("Bonus Modules", R.color.teal_300)
    }

    init {
        setHasOptionsMenu(true)
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_home, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        view.recycler_view.setHasFixedSize(true)
        view.recycler_view.layoutManager = LinearLayoutManager(view.context)
        view.recycler_view.adapter = HomeAdapter(LayoutInflater.from(view.context), DemoModel.values())

        view.fab.setOnClickListener { onFabClicked(true) }
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        outState.putInt(KEY_FAB_VISIBILITY, view.fab.visibility)
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)

        view.fab.visibility = savedViewState.getInt(KEY_FAB_VISIBILITY)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home, menu)
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        setOptionsMenuHidden(!changeType.isEnter)

        if (changeType.isEnter) {
            setTitle()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.about) {
            onFabClicked(false)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFabClicked(fromFab: Boolean) {
        val details = SpannableString("A small, yet full-featured framework that allows building View-based Android applications")
        details.setSpan(AbsoluteSizeSpan(16, true), 0, details.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val url = "https://github.com/bluelinelabs/Conductor"
        val link = SpannableString(url)
        link.setSpan(object : URLSpan(url) {
            override fun onClick(widget: View) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }, 0, link.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val description = SpannableStringBuilder()
        description.append(details)
        description.append("\n\n")
        description.append(link)

        val pushHandler = if (fromFab) TransitionChangeHandlerCompat(FabToDialogTransitionChangeHandler(), FadeChangeHandler(false)) else FadeChangeHandler(false)
        val popHandler = if (fromFab) TransitionChangeHandlerCompat(FabToDialogTransitionChangeHandler(), FadeChangeHandler()) else FadeChangeHandler()

        router.pushController(RouterTransaction.with(DialogController("Conductor", description))
                .pushChangeHandler(pushHandler)
                .popChangeHandler(popHandler))

    }

    internal fun onModelRowClick(model: DemoModel?, position: Int) {
        when (model) {
            HomeController.DemoModel.NAVIGATION -> router.pushController(RouterTransaction.with(NavigationDemoController(0, NavigationDemoController.DisplayUpMode.SHOW_FOR_CHILDREN_ONLY))
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler())
                    .tag(NavigationDemoController.TAG_UP_TRANSACTION)
            )
            HomeController.DemoModel.TRANSITIONS -> router.pushController(TransitionDemoController.getRouterTransaction(0, this))
            HomeController.DemoModel.TARGET_CONTROLLER -> router.pushController(
                    RouterTransaction.with(TargetDisplayController())
                            .pushChangeHandler(FadeChangeHandler())
                            .popChangeHandler(FadeChangeHandler()))
            HomeController.DemoModel.VIEW_PAGER -> router.pushController(RouterTransaction.with(PagerController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
            HomeController.DemoModel.CHILD_CONTROLLERS -> router.pushController(RouterTransaction.with(ParentController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
            HomeController.DemoModel.SHARED_ELEMENT_TRANSITIONS -> {
                val titleSharedElementName = resources?.getString(R.string.transition_tag_title_indexed, position)
                        ?: ""
                val dotSharedElementName = resources?.getString(R.string.transition_tag_dot_indexed, position)
                        ?: ""

                router.pushController(RouterTransaction.with(CityGridController(model.title, model.color, position))
                        .pushChangeHandler(ArcFadeMoveChangeHandlerCompat(titleSharedElementName, dotSharedElementName))
                        .popChangeHandler(ArcFadeMoveChangeHandlerCompat(titleSharedElementName, dotSharedElementName)))
            }
            HomeController.DemoModel.DRAG_DISMISS -> router.pushController(RouterTransaction.with(DragDismissController())
                    .pushChangeHandler(FadeChangeHandler(false))
                    .popChangeHandler(FadeChangeHandler()))
            HomeController.DemoModel.EXTERNAL_MODULES -> router.pushController(RouterTransaction.with(ExternalModulesController())
                    .pushChangeHandler(HorizontalChangeHandler())
                    .popChangeHandler(HorizontalChangeHandler()))
            HomeController.DemoModel.MULTIPLE_CHILD_ROUTERS -> router.pushController(RouterTransaction.with(MultipleChildRouterController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
            HomeController.DemoModel.MASTER_DETAIL -> router.pushController(RouterTransaction.with(MasterDetailListController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler()))
        }
    }

    internal inner class HomeAdapter(private val inflater: LayoutInflater, private val items: Array<DemoModel>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_home, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position, items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private var model: DemoModel? = null

            fun bind(position: Int, item: DemoModel) {
                model = item
                itemView.tv_title.text = item.title
                itemView.img_dot.drawable.setColorFilter(ContextCompat.getColor(activity!!, item.color), Mode.SRC_ATOP)

                itemView.row_root.setOnClickListener { onModelRowClick(model, position) }

                ViewCompat.setTransitionName(itemView.tv_title, resources?.getString(R.string.transition_tag_title_indexed, position))
                ViewCompat.setTransitionName(itemView.img_dot, resources?.getString(R.string.transition_tag_dot_indexed, position))
            }
        }
    }

    companion object {

        private val KEY_FAB_VISIBILITY = "HomeController.fabVisibility"
    }

}
