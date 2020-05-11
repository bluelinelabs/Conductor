package com.bluelinelabs.conductor.viewpager

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.viewpager.util.FakePager
import com.bluelinelabs.conductor.viewpager.util.TestController
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StateSaveTests {

  private val pager: FakePager
  private val pagerAdapter: RouterPagerAdapter

  init {
    val activityController = Robolectric.buildActivity(AppCompatActivity::class.java)
    activityController.get().setTheme(R.style.Theme_AppCompat)
    activityController.setup()

    val router = Conductor.attachRouter(activityController.get(), FrameLayout(activityController.get()), null)
    val controller = TestController()
    router.setRoot(with(controller))
    pager = FakePager(FrameLayout(activityController.get()).also {
      it.id = ViewCompat.generateViewId()
    })
    pager.offscreenPageLimit = 1
    pagerAdapter = object : RouterPagerAdapter(controller) {
      override fun configureRouter(router: Router, position: Int) {
        if (!router.hasRootController()) {
          router.setRoot(with(TestController()))
        }
      }

      override fun getCount(): Int {
        return 20
      }
    }
    pager.setAdapter(pagerAdapter)
  }

  @Test
  fun testNoMaxSaves() {
    // Load all pages
    for (i in 0 until pagerAdapter.count) {
      pager.pageTo(i)
    }
    pager.pageTo(pagerAdapter.count / 2)

    // Ensure all non-visible pages are saved
    assertEquals(
      pagerAdapter.count - 1 - (pager.offscreenPageLimit * 2),
      pagerAdapter.savedPages.size()
    )
  }

  @Test
  fun testMaxSavedSet() {
    val maxPages = 3
    pagerAdapter.setMaxPagesToStateSave(maxPages)

    // Load all pages
    for (i in 0 until pagerAdapter.count) {
      pager.pageTo(i)
    }
    val firstSelectedItem = pagerAdapter.count / 2
    pager.pageTo(firstSelectedItem)
    var savedPages = pagerAdapter.savedPages

    // Ensure correct number of pages are saved
    assertEquals(maxPages, savedPages.size())

    // Ensure correct pages are saved
    assertEquals(
      pagerAdapter.count - 3,
      savedPages.keyAt(0)
    )
    assertEquals(
      pagerAdapter.count - 2,
      savedPages.keyAt(1)
    )
    assertEquals(
      pagerAdapter.count - 1,
      savedPages.keyAt(2)
    )
    val secondSelectedItem = 1
    pager.pageTo(secondSelectedItem)
    savedPages = pagerAdapter.savedPages

    // Ensure correct number of pages are saved
    assertEquals(maxPages, savedPages.size())

    // Ensure correct pages are saved
    assertEquals(firstSelectedItem - 1, savedPages.keyAt(0))
    assertEquals(firstSelectedItem, savedPages.keyAt(1))
    assertEquals(firstSelectedItem + 1, savedPages.keyAt(2))
  }
}