package com.bluelinelabs.conductor.demo.controllers

import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerViewPager2Binding
import com.bluelinelabs.conductor.demo.util.viewBinding
import com.bluelinelabs.conductor.viewpager2.RouterStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

class ViewPager2Controller : BaseController(R.layout.controller_view_pager2) {
  private val binding: ControllerViewPager2Binding by viewBinding(ControllerViewPager2Binding::bind)

  override val title = "ViewPager2 Demo"

  private val pageColors = intArrayOf(R.color.green_300, R.color.cyan_300, R.color.deep_purple_300, R.color.lime_300, R.color.red_300)

  private lateinit var tabLayoutMediator: TabLayoutMediator

  private val pagerAdapter = object : RouterStateAdapter(this) {
    override fun configureRouter(router: Router, position: Int) {
      if (!router.hasRootController()) {
        val page: Controller = ChildController(
          title = String.format(Locale.getDefault(), "Child #%d (Swipe to see more)", position),
          backgroundColor = pageColors[position],
          colorIsResId = true
        )
        router.setRoot(with(page))
      }
    }

    override fun getItemCount() = pageColors.size
  }

  override fun onViewCreated(view: View) {
    binding.viewPager.adapter = pagerAdapter
    tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      tab.text = "Page $position"
    }
    tabLayoutMediator.attach()
  }

  override fun onDestroyView(view: View) {
    if (!activity!!.isChangingConfigurations) {
      binding.viewPager.adapter = null
    }
    tabLayoutMediator.detach()
    super.onDestroyView(view)
  }
}