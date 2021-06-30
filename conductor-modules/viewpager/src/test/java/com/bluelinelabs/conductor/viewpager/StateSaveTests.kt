package com.bluelinelabs.conductor.viewpager

import com.bluelinelabs.conductor.viewpager.util.TestActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StateSaveTests {

  private val testController = Robolectric.buildActivity(TestActivity::class.java)
    .setup()
    .get()
    .testController()

  private val pagerAdapter = testController.pagerAdapter
  private val pager = testController.pager
  private val destroyedItems = testController.destroyedItems

  @Test
  fun testNoMaxSaves() {
    // Load all pages
    for (i in 0 until pagerAdapter.count) {
      pager.currentItem = i
    }

    // Ensure all non-visible pages are saved
    assertEquals(
      destroyedItems.size,
      pagerAdapter.savedPages.size()
    )
  }

  @Test
  fun testMaxSavedSet() {
    val maxPages = 3
    pagerAdapter.setMaxPagesToStateSave(maxPages)

    // Load all pages
    for (i in 0 until pagerAdapter.count) {
      pager.currentItem = i
    }

    val firstSelectedItem = pagerAdapter.count / 2
    for (i in pagerAdapter.count downTo firstSelectedItem) {
      pager.currentItem = i
    }

    var savedPages = pagerAdapter.savedPages

    // Ensure correct number of pages are saved
    assertEquals(maxPages, savedPages.size())

    // Ensure correct pages are saved
    assertEquals(destroyedItems[destroyedItems.lastIndex], savedPages.keyAt(0))
    assertEquals(destroyedItems[destroyedItems.lastIndex - 1], savedPages.keyAt(1))
    assertEquals(destroyedItems[destroyedItems.lastIndex - 2], savedPages.keyAt(2))

    val secondSelectedItem = 1
    for (i in firstSelectedItem downTo secondSelectedItem) {
      pager.currentItem = i
    }

    savedPages = pagerAdapter.savedPages

    // Ensure correct number of pages are saved
    assertEquals(maxPages, savedPages.size())

    // Ensure correct pages are saved
    assertEquals(destroyedItems[destroyedItems.lastIndex], savedPages.keyAt(0))
    assertEquals(destroyedItems[destroyedItems.lastIndex - 1], savedPages.keyAt(1))
    assertEquals(destroyedItems[destroyedItems.lastIndex - 2], savedPages.keyAt(2))
  }
}