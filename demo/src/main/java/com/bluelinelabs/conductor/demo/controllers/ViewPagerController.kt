package com.bluelinelabs.conductor.demo.controllers

import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.databinding.ControllerViewPagerBinding
import com.bluelinelabs.conductor.demo.util.viewBinding
import com.bluelinelabs.conductor.viewpager.RouterPagerAdapter
import java.util.*

class ViewPagerController : BaseController(R.layout.controller_view_pager) {
  private val binding: ControllerViewPagerBinding by viewBinding(ControllerViewPagerBinding::bind)

  override val title = "ViewPager Demo"

  private val pageColors = intArrayOf(R.color.green_300, R.color.cyan_300, R.color.deep_purple_300, R.color.lime_300, R.color.red_300)

  private val pagerAdapter = object : RouterPagerAdapter(this) {
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

    override fun getCount() = pageColors.size

    override fun getPageTitle(position: Int) = "Page $position"
  }

  override fun onViewCreated(view: View) {
    binding.viewPager.adapter = pagerAdapter
    binding.tabLayout.setupWithViewPager(binding.viewPager)
  }

  override fun onDestroyView(view: View) {
    if (!activity!!.isChangingConfigurations) {
      binding.viewPager.adapter = null
    }
    binding.tabLayout.setupWithViewPager(null)
    super.onDestroyView(view)
  }
}
