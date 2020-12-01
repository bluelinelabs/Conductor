package com.bluelinelabs.conductor.demo.controllers

import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.changehandler.ArcFadeMoveChangeHandler
import com.bluelinelabs.conductor.demo.changehandler.FabToDialogTransitionChangeHandler
import com.bluelinelabs.conductor.demo.controllers.NavigationDemoController.DisplayUpMode
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerHomeBinding
import com.bluelinelabs.conductor.demo.databinding.RowHomeBinding
import com.bluelinelabs.conductor.demo.util.viewBinding

class HomeController : BaseController(R.layout.controller_home) {
  private val binding: ControllerHomeBinding by viewBinding(ControllerHomeBinding::bind)

  override val title = "Conductor Demos"

  override fun onViewCreated(view: View) {
    binding.recyclerView.setHasFixedSize(true)
    binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
    binding.recyclerView.adapter = HomeAdapter(
      LayoutInflater.from(view.context),
      DemoModel.values(),
      ::onModelRowClick
    )

    binding.fab.setOnClickListener { showAboutDialog(fromFab = true) }
  }

  override fun configureToolbar(toolbar: Toolbar) {
    super.configureToolbar(toolbar)

    toolbar.setOnMenuItemClickListener {
      if (it.itemId == R.id.about) {
        showAboutDialog(fromFab = false)
        true
      } else {
        false
      }
    }
  }

  override fun configureMenu(toolbar: Toolbar) {
    super.configureMenu(toolbar)

    toolbar.inflateMenu(R.menu.home)
  }

  override fun onSaveViewState(view: View, outState: Bundle) {
    super.onSaveViewState(view, outState)
    outState.putInt(KEY_FAB_VISIBILITY, binding.fab.visibility)
  }

  override fun onRestoreViewState(view: View, savedViewState: Bundle) {
    super.onRestoreViewState(view, savedViewState)
    binding.fab.visibility = savedViewState.getInt(KEY_FAB_VISIBILITY)
  }

  private fun onModelRowClick(model: DemoModel, position: Int) {
    when (model) {
      DemoModel.NAVIGATION -> {
        router.pushController(
          RouterTransaction.with(NavigationDemoController(0, DisplayUpMode.SHOW_FOR_CHILDREN_ONLY))
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
            .tag(NavigationDemoController.TAG_UP_TRANSACTION)
        )
      }
      DemoModel.TRANSITIONS -> {
        router.pushController(TransitionDemoController.getRouterTransaction(0, this))
      }
      DemoModel.TARGET_CONTROLLER -> {
        router.pushController(
          RouterTransaction.with(TargetDisplayController())
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
        )
      }
      DemoModel.VIEW_PAGER -> {
        router.pushController(
          RouterTransaction.with(ViewPagerController())
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
        )
      }
      DemoModel.VIEW_PAGER_2 -> {
        router.pushController(
          RouterTransaction.with(ViewPager2Controller())
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
        )
      }
      DemoModel.CHILD_CONTROLLERS -> {
        router.pushController(
          RouterTransaction.with(ParentController())
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
        )
      }
      DemoModel.SHARED_ELEMENT_TRANSITIONS -> {
        val titleSharedElementName = resources!!.getString(R.string.transition_tag_title_indexed, position)
        val dotSharedElementName = resources!!.getString(R.string.transition_tag_dot_indexed, position)
        router.pushController(
          RouterTransaction.with(CityGridController(model.title, model.color, position))
            .pushChangeHandler(ArcFadeMoveChangeHandler(titleSharedElementName, dotSharedElementName))
            .popChangeHandler(ArcFadeMoveChangeHandler(titleSharedElementName, dotSharedElementName))
        )
      }
      DemoModel.DRAG_DISMISS -> {
        router.pushController(
          RouterTransaction.with(DragDismissController())
            .pushChangeHandler(FadeChangeHandler(false))
            .popChangeHandler(FadeChangeHandler())
        )
      }
      DemoModel.EXTERNAL_MODULES -> {
        router.pushController(
          RouterTransaction.with(ExternalModulesController())
            .pushChangeHandler(HorizontalChangeHandler())
            .popChangeHandler(HorizontalChangeHandler())
        )
      }
      DemoModel.MULTIPLE_CHILD_ROUTERS -> {
        router.pushController(
          RouterTransaction.with(MultipleChildRouterController())
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
        )
      }
      DemoModel.MASTER_DETAIL -> {
        router.pushController(
          RouterTransaction.with(MasterDetailListController())
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
        )
      }
    }
  }

  private fun showAboutDialog(fromFab: Boolean) {
    val details = SpannableString("A small, yet full-featured framework that allows building View-based Android applications").apply {
      setSpan(AbsoluteSizeSpan(16, true), 0, length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }

    val link = SpannableString(CONDUCTOR_URL).apply {
      setSpan(object : URLSpan(CONDUCTOR_URL) {
        override fun onClick(widget: View) {
          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
      }, 0, length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }

    val description = buildSpannedString {
      append(details)
      append("\n\n")
      append(link)
    }

    val pushHandler = if (fromFab) FabToDialogTransitionChangeHandler() else FadeChangeHandler(false)
    val popHandler = if (fromFab) FabToDialogTransitionChangeHandler() else FadeChangeHandler()
    router.pushController(
      RouterTransaction.with(DialogController("Conductor", description))
        .pushChangeHandler(pushHandler)
        .popChangeHandler(popHandler)
    )
  }

  companion object {
    private const val KEY_FAB_VISIBILITY = "HomeController.fabVisibility"
    private const val CONDUCTOR_URL = "https://github.com/bluelinelabs/Conductor"
  }
}

private enum class DemoModel(val title: String, @ColorRes val color: Int) {
  NAVIGATION("Navigation Demos", R.color.red_300),
  TRANSITIONS("Transition Demos", R.color.blue_grey_300),
  SHARED_ELEMENT_TRANSITIONS("Shared Element Demos", R.color.purple_300),
  CHILD_CONTROLLERS("Child Controllers", R.color.orange_300),
  VIEW_PAGER("ViewPager", R.color.green_300),
  VIEW_PAGER_2("ViewPager2", R.color.pink_300),
  TARGET_CONTROLLER("Target Controller", R.color.deep_orange_300),
  MULTIPLE_CHILD_ROUTERS("Multiple Child Routers", R.color.grey_300),
  MASTER_DETAIL("Master Detail", R.color.lime_300),
  DRAG_DISMISS("Drag Dismiss", R.color.teal_300),
  EXTERNAL_MODULES("Bonus Modules", R.color.deep_purple_300)
}

private class HomeAdapter(
  private val inflater: LayoutInflater,
  private val items: Array<DemoModel>,
  private val modelClickListener: (DemoModel, Int) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      RowHomeBinding.inflate(inflater, parent, false),
      modelClickListener
    )
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(position, items[position])
  }

  override fun getItemCount() = items.size

  class ViewHolder(
    private val binding: RowHomeBinding,
    private val modelClickListener: (DemoModel, Int) -> Unit
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, item: DemoModel) {
      binding.title.text = item.title
      binding.dot.drawable.setColorFilter(
        ContextCompat.getColor(binding.root.context, item.color),
        PorterDuff.Mode.SRC_ATOP
      )
      binding.root.setOnClickListener { modelClickListener(item, position) }

      binding.title.transitionName =
        binding.root.resources.getString(R.string.transition_tag_title_indexed, position)
      binding.dot.transitionName =
        binding.root.resources.getString(R.string.transition_tag_dot_indexed, position)
    }
  }
}