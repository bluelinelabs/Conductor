package com.bluelinelabs.conductor.viewpager.util

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.asTransaction
import com.bluelinelabs.conductor.viewpager.RouterPagerAdapter

class TestActivity : Activity() {

  private lateinit var router: Router

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    router = Conductor.attachRouter(
      this,
      findViewById(android.R.id.content),
      savedInstanceState
    )
    if (!router.hasRootController()) {
      router.setRoot(TestController().asTransaction())
    }
  }

  fun testController(): TestController {
    return router.backstack.single().controller as TestController
  }
}

class TestController : Controller() {

  val destroyedItems = mutableListOf<Int>()
  lateinit var pagerAdapter: RouterPagerAdapter
  lateinit var pager: ViewPager

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    pager = ViewPager(container.context).also {
      it.id = ViewCompat.generateViewId()
    }
    pager.offscreenPageLimit = 1
    pagerAdapter = object : RouterPagerAdapter(this) {

      override fun configureRouter(router: Router, position: Int) {
        if (!router.hasRootController()) {
          router.setRoot(RouterTransaction.with(PageController()))
        }
      }

      override fun getCount(): Int {
        return 20
      }

      override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        destroyedItems.add(position)
      }
    }
    pager.adapter = pagerAdapter
    return pager
  }
}

class PageController : Controller() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return FrameLayout(container.context)
  }
}