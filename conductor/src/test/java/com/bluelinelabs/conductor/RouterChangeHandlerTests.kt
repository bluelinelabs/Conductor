package com.bluelinelabs.conductor

import com.bluelinelabs.conductor.util.MockChangeHandler
import com.bluelinelabs.conductor.util.TestActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RouterChangeHandlerTests {

  private val router = Robolectric.buildActivity(TestActivity::class.java)
    .setup()
    .get()
    .router

  @Test
  fun testSetRootHandler() {
    val handler = MockChangeHandler.taggedHandler("root", true)
    val rootController = TestController()
    router.setRoot(
      rootController.asTransaction(pushChangeHandler = handler)
    )

    Assert.assertTrue(rootController.changeHandlerHistory.isValidHistory)
    Assert.assertNull(rootController.changeHandlerHistory.latestFromView())
    Assert.assertNotNull(rootController.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      rootController.view,
      rootController.changeHandlerHistory.latestToView()
    )
    Assert.assertTrue(rootController.changeHandlerHistory.latestIsPush())
    Assert.assertEquals(handler.tag, rootController.changeHandlerHistory.latestChangeHandler().tag)
  }

  @Test
  fun testPushPopHandlers() {
    val rootController = TestController()
    router.setRoot(
      rootController.asTransaction(pushChangeHandler = MockChangeHandler.defaultHandler())
    )

    val rootView = rootController.view
    val pushHandler = MockChangeHandler.taggedHandler("push", true)
    val popHandler = MockChangeHandler.taggedHandler("pop", true)
    val pushController = TestController()
    router.pushController(
      pushController.asTransaction(
        pushChangeHandler = pushHandler,
        popChangeHandler = popHandler
      )
    )

    Assert.assertTrue(rootController.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(pushController.changeHandlerHistory.isValidHistory)
    Assert.assertNotNull(pushController.changeHandlerHistory.latestFromView())
    Assert.assertNotNull(pushController.changeHandlerHistory.latestToView())
    Assert.assertEquals(rootView, pushController.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      pushController.view,
      pushController.changeHandlerHistory.latestToView()
    )
    Assert.assertTrue(pushController.changeHandlerHistory.latestIsPush())
    Assert.assertEquals(
      pushHandler.tag,
      pushController.changeHandlerHistory.latestChangeHandler().tag
    )

    val pushView = pushController.view
    router.popController(pushController)
    Assert.assertNotNull(pushController.changeHandlerHistory.latestFromView())
    Assert.assertNotNull(pushController.changeHandlerHistory.latestToView())
    Assert.assertEquals(pushView, pushController.changeHandlerHistory.fromViewAt(1))
    Assert.assertEquals(
      rootController.view,
      pushController.changeHandlerHistory.latestToView()
    )
    Assert.assertFalse(pushController.changeHandlerHistory.latestIsPush())
    Assert.assertEquals(
      popHandler.tag,
      pushController.changeHandlerHistory.latestChangeHandler().tag
    )
  }

  @Test
  fun testResetRootHandlers() {
    val initialController1 = TestController()
    router.setRoot(
      initialController1.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler("initialPush1", true),
        popChangeHandler = MockChangeHandler.taggedHandler("initialPop1", true)
      )
    )

    val initialController2 = TestController()
    router.pushController(
      initialController2.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler("initialPush2", false),
        popChangeHandler = MockChangeHandler.taggedHandler("initialPop2", false)
      )
    )

    val initialView1 = initialController1.view
    val initialView2 = initialController2.view
    val newRootController = TestController()
    val newRootHandlerTag = "newRootHandler"
    router.setRoot(
      newRootController.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler(newRootHandlerTag, true)
      )
    )

    Assert.assertTrue(initialController1.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(initialController2.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(newRootController.changeHandlerHistory.isValidHistory)
    Assert.assertEquals(3, initialController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(2, initialController2.changeHandlerHistory.size().toLong())
    Assert.assertEquals(1, newRootController.changeHandlerHistory.size().toLong())
    Assert.assertNotNull(initialController1.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newRootController.view,
      initialController1.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView1, initialController1.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      newRootHandlerTag,
      initialController1.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController1.changeHandlerHistory.latestIsPush())
    Assert.assertNull(initialController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView2, initialController2.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      newRootHandlerTag,
      initialController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController2.changeHandlerHistory.latestIsPush())
    Assert.assertNotNull(newRootController.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newRootController.view,
      newRootController.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView1, newRootController.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      newRootHandlerTag,
      newRootController.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(newRootController.changeHandlerHistory.latestIsPush())
  }

  @Test
  fun testSetBackstackHandlers() {
    val initialController1 = TestController()
    router.setRoot(
      initialController1.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler("initialPush1", true),
        popChangeHandler = MockChangeHandler.taggedHandler("initialPop1", true)
      )
    )

    val initialController2 = TestController()
    router.pushController(
      initialController2.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler("initialPush2", false),
        popChangeHandler = MockChangeHandler.taggedHandler("initialPop2", false)
      )
    )

    val initialView1 = initialController1.view
    val initialView2 = initialController2.view
    val newController1 = TestController()
    val newController2 = TestController()
    val setBackstackHandler = MockChangeHandler.taggedHandler("setBackstackHandler", true)
    val newBackstack = listOf(newController1.asTransaction(), newController2.asTransaction())
    router.setBackstack(newBackstack, setBackstackHandler)

    Assert.assertTrue(initialController1.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(initialController2.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(newController1.changeHandlerHistory.isValidHistory)
    Assert.assertEquals(3, initialController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(2, initialController2.changeHandlerHistory.size().toLong())
    Assert.assertEquals(0, newController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(1, newController2.changeHandlerHistory.size().toLong())
    Assert.assertNotNull(initialController1.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newController2.view,
      initialController1.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView1, initialController1.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController1.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController1.changeHandlerHistory.latestIsPush())
    Assert.assertNull(initialController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView2, initialController2.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController2.changeHandlerHistory.latestIsPush())
    Assert.assertNotNull(newController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newController2.view,
      newController2.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView1, newController2.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      newController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(newController2.changeHandlerHistory.latestIsPush())
  }

  @Test
  fun testSetBackstackWithTwoVisibleHandlers() {
    val initialController1 = TestController()
    router.setRoot(
      initialController1.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler("initialPush1", true),
        popChangeHandler = MockChangeHandler.taggedHandler("initialPop1", true)
      )
    )

    val initialController2 = TestController()
    router.pushController(
      initialController2.asTransaction(
        pushChangeHandler = MockChangeHandler.taggedHandler("initialPush2", false),
        popChangeHandler = MockChangeHandler.taggedHandler("initialPop2", false)
      )
    )

    val initialView1 = initialController1.view
    val initialView2 = initialController2.view
    val newController1 = TestController()
    val newController2 = TestController()
    val setBackstackHandler = MockChangeHandler.taggedHandler("setBackstackHandler", true)
    val pushController2Handler = MockChangeHandler.noRemoveViewOnPushHandler("pushController2")
    val newBackstack = listOf(
      newController1.asTransaction(),
      newController2.asTransaction(pushChangeHandler = pushController2Handler)
    )
    router.setBackstack(newBackstack, setBackstackHandler)

    Assert.assertTrue(initialController1.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(initialController2.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(newController1.changeHandlerHistory.isValidHistory)
    Assert.assertEquals(3, initialController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(2, initialController2.changeHandlerHistory.size().toLong())
    Assert.assertEquals(2, newController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(1, newController2.changeHandlerHistory.size().toLong())
    Assert.assertNotNull(initialController1.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newController1.view,
      initialController1.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView1, initialController1.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController1.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController1.changeHandlerHistory.latestIsPush())
    Assert.assertNull(initialController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView2, initialController2.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController2.changeHandlerHistory.latestIsPush())
    Assert.assertNotNull(newController1.changeHandlerHistory.latestToView())
    Assert.assertEquals(newController1.view, newController1.changeHandlerHistory.toViewAt(0))
    Assert.assertEquals(
      newController2.view,
      newController1.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView1, newController1.changeHandlerHistory.fromViewAt(0))
    Assert.assertEquals(
      newController1.view,
      newController1.changeHandlerHistory.latestFromView()
    )
    Assert.assertEquals(
      setBackstackHandler.tag,
      newController1.changeHandlerHistory.changeHandlerAt(0).tag
    )
    Assert.assertEquals(
      pushController2Handler.tag,
      newController1.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(newController1.changeHandlerHistory.latestIsPush())
    Assert.assertNotNull(newController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newController2.view,
      newController2.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(
      newController1.view,
      newController2.changeHandlerHistory.latestFromView()
    )
    Assert.assertEquals(
      pushController2Handler.tag,
      newController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(newController2.changeHandlerHistory.latestIsPush())
  }

  @Test
  fun testSetBackstackForPushHandlers() {
    val initialController = TestController()
    val initialTransaction = initialController.asTransaction(
      pushChangeHandler = MockChangeHandler.taggedHandler("initialPush1", true),
      popChangeHandler = MockChangeHandler.taggedHandler("initialPop1", true)
    )
    router.setRoot(initialTransaction)

    val initialView = initialController.view
    val newController = TestController()
    val setBackstackHandler = MockChangeHandler.taggedHandler("setBackstackHandler", true)
    val newBackstack = listOf(initialTransaction, newController.asTransaction())
    router.setBackstack(newBackstack, setBackstackHandler)

    Assert.assertTrue(initialController.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(newController.changeHandlerHistory.isValidHistory)
    Assert.assertEquals(2, initialController.changeHandlerHistory.size().toLong())
    Assert.assertEquals(1, newController.changeHandlerHistory.size().toLong())
    Assert.assertNotNull(initialController.changeHandlerHistory.latestToView())
    Assert.assertEquals(
      newController.view,
      initialController.changeHandlerHistory.latestToView()
    )
    Assert.assertEquals(initialView, initialController.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController.changeHandlerHistory.latestIsPush())
    Assert.assertTrue(newController.changeHandlerHistory.latestIsPush())
  }

  @Test
  fun testSetBackstackForInvertHandlersWithRemovesView() {
    val initialController1 = TestController()
    val initialTransaction1 = initialController1.asTransaction(
      pushChangeHandler = MockChangeHandler.taggedHandler("initialPush1", true),
      popChangeHandler = MockChangeHandler.taggedHandler("initialPop1", true)
    )
    router.setRoot(initialTransaction1)

    val initialController2 = TestController()
    val initialTransaction2 = initialController2.asTransaction(
      pushChangeHandler = MockChangeHandler.taggedHandler("initialPush2", true),
      popChangeHandler = MockChangeHandler.taggedHandler("initialPop2", true)
    )
    router.pushController(initialTransaction2)

    val initialView2 = initialController2.view
    val setBackstackHandler = MockChangeHandler.taggedHandler("setBackstackHandler", true)
    val newBackstack = listOf(initialTransaction2, initialTransaction1)
    router.setBackstack(newBackstack, setBackstackHandler)

    Assert.assertTrue(initialController1.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(initialController2.changeHandlerHistory.isValidHistory)
    Assert.assertEquals(3, initialController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(2, initialController2.changeHandlerHistory.size().toLong())
    Assert.assertNotNull(initialController1.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView2, initialController1.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController1.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertFalse(initialController1.changeHandlerHistory.latestIsPush())
    Assert.assertNotNull(initialController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView2, initialController2.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertFalse(initialController2.changeHandlerHistory.latestIsPush())
  }

  @Test
  fun testSetBackstackForInvertHandlersWithoutRemovesView() {
    val initialController1 = TestController()
    val initialTransaction1 = initialController1.asTransaction(
      pushChangeHandler = MockChangeHandler.taggedHandler("initialPush1", true),
      popChangeHandler = MockChangeHandler.taggedHandler("initialPop1", true)
    )
    router.setRoot(initialTransaction1)

    val initialController2 = TestController()
    val initialPushHandler2Tag = "initialPush2"
    val initialTransaction2 = initialController2.asTransaction(
      pushChangeHandler = MockChangeHandler.taggedHandler(initialPushHandler2Tag, false),
      popChangeHandler = MockChangeHandler.taggedHandler("initialPop2", false)
    )
    router.pushController(initialTransaction2)

    val initialView1 = initialController1.view
    val initialView2 = initialController2.view
    val setBackstackHandler = MockChangeHandler.taggedHandler("setBackstackHandler", true)
    val newBackstack = listOf(initialTransaction2, initialTransaction1)
    router.setBackstack(newBackstack, setBackstackHandler)

    Assert.assertTrue(initialController1.changeHandlerHistory.isValidHistory)
    Assert.assertTrue(initialController2.changeHandlerHistory.isValidHistory)
    Assert.assertEquals(2, initialController1.changeHandlerHistory.size().toLong())
    Assert.assertEquals(2, initialController2.changeHandlerHistory.size().toLong())
    Assert.assertNotNull(initialController1.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView1, initialController1.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      initialPushHandler2Tag,
      initialController1.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertTrue(initialController1.changeHandlerHistory.latestIsPush())
    Assert.assertNull(initialController2.changeHandlerHistory.latestToView())
    Assert.assertEquals(initialView2, initialController2.changeHandlerHistory.latestFromView())
    Assert.assertEquals(
      setBackstackHandler.tag,
      initialController2.changeHandlerHistory.latestChangeHandler().tag
    )
    Assert.assertFalse(initialController2.changeHandlerHistory.latestIsPush())
  }
}