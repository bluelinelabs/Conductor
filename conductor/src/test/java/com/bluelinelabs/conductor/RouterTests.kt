package com.bluelinelabs.conductor

import android.view.View
import com.bluelinelabs.conductor.Controller.LifecycleListener
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
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
class RouterTests {

  private val router = Robolectric.buildActivity(TestActivity::class.java)
    .setup()
    .get()
    .router

  @Test
  fun testSetRoot() {
    val rootTag = "root"
    val rootController = TestController()
    Assert.assertFalse(router.hasRootController())

    router.setRoot(RouterTransaction.with(rootController).tag(rootTag))
    Assert.assertTrue(router.hasRootController())
    Assert.assertEquals(rootController, router.getControllerWithTag(rootTag))
  }

  @Test
  fun testSetNewRoot() {
    val oldRootTag = "oldRoot"
    val newRootTag = "newRoot"
    val oldRootController = TestController()
    val newRootController = TestController()
    router.setRoot(RouterTransaction.with(oldRootController).tag(oldRootTag))
    router.setRoot(RouterTransaction.with(newRootController).tag(newRootTag))
    Assert.assertNull(router.getControllerWithTag(oldRootTag))
    Assert.assertEquals(newRootController, router.getControllerWithTag(newRootTag))
  }

  @Test
  fun testGetByInstanceId() {
    val controller = TestController()
    router.pushController(controller.asTransaction())
    Assert.assertEquals(
      controller,
      router.getControllerWithInstanceId(controller.getInstanceId())
    )
    Assert.assertNull(router.getControllerWithInstanceId("fake id"))
  }

  @Test
  fun testGetByTag() {
    val controller1Tag = "controller1"
    val controller2Tag = "controller2"
    val controller1 = TestController()
    val controller2 = TestController()
    router.pushController(
      RouterTransaction.with(controller1).tag(controller1Tag)
    )
    router.pushController(
      RouterTransaction.with(controller2).tag(controller2Tag)
    )
    Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag))
    Assert.assertEquals(controller2, router.getControllerWithTag(controller2Tag))
  }

  @Test
  fun testPushPopControllers() {
    val controller1Tag = "controller1"
    val controller2Tag = "controller2"
    val controller1 = TestController()
    val controller2 = TestController()
    router.pushController(
      RouterTransaction.with(controller1).tag(controller1Tag)
    )
    Assert.assertEquals(1, router.backstackSize.toLong())

    router.pushController(
      RouterTransaction.with(controller2).tag(controller2Tag)
    )
    Assert.assertEquals(2, router.backstackSize.toLong())

    router.popCurrentController()
    Assert.assertEquals(1, router.backstackSize.toLong())
    Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag))
    Assert.assertNull(router.getControllerWithTag(controller2Tag))

    router.popCurrentController()
    Assert.assertEquals(0, router.backstackSize.toLong())
    Assert.assertNull(router.getControllerWithTag(controller1Tag))
    Assert.assertNull(router.getControllerWithTag(controller2Tag))
  }

  @Test
  fun testPopControllerConcurrentModificationException() {
    var step = 1
    var i = 0
    while (i < 10) {
      router.pushController(RouterTransaction.with(TestController()).tag("1"))
      router.pushController(RouterTransaction.with(TestController()).tag("2"))
      router.pushController(RouterTransaction.with(TestController()).tag("3"))
      val tag = when (step) {
        1 -> "1"
        2 -> "2"
        else -> {
          step = 0
          "3"
        }
      }
      val controller = router.getControllerWithTag(tag)
      if (controller != null) {
        router.popController(controller)
      }
      router.popToRoot()
      i++
      step++
    }
  }

  @Test
  fun testPopToTag() {
    val controller1Tag = "controller1"
    val controller2Tag = "controller2"
    val controller3Tag = "controller3"
    val controller4Tag = "controller4"
    val controller1 = TestController()
    val controller2 = TestController()
    val controller3 = TestController()
    val controller4 = TestController()
    router.pushController(
      RouterTransaction.with(controller1).tag(controller1Tag)
    )
    router.pushController(
      RouterTransaction.with(controller2).tag(controller2Tag)
    )
    router.pushController(
      RouterTransaction.with(controller3).tag(controller3Tag)
    )
    router.pushController(
      RouterTransaction.with(controller4).tag(controller4Tag)
    )
    router.popToTag(controller2Tag)

    Assert.assertEquals(2, router.backstackSize.toLong())
    Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag))
    Assert.assertEquals(controller2, router.getControllerWithTag(controller2Tag))
    Assert.assertNull(router.getControllerWithTag(controller3Tag))
    Assert.assertNull(router.getControllerWithTag(controller4Tag))
  }

  @Test
  fun testPopNonCurrent() {
    val controller1Tag = "controller1"
    val controller2Tag = "controller2"
    val controller3Tag = "controller3"
    val controller1 = TestController()
    val controller2 = TestController()
    val controller3 = TestController()
    router.pushController(
      RouterTransaction.with(controller1).tag(controller1Tag)
    )
    router.pushController(
      RouterTransaction.with(controller2).tag(controller2Tag)
    )
    router.pushController(
      RouterTransaction.with(controller3).tag(controller3Tag)
    )
    router.popController(controller2)

    Assert.assertEquals(2, router.backstackSize.toLong())
    Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag))
    Assert.assertNull(router.getControllerWithTag(controller2Tag))
    Assert.assertEquals(controller3, router.getControllerWithTag(controller3Tag))
  }

  @Test
  fun testSetBackstack() {
    val rootTransaction = TestController().asTransaction()
    val middleTransaction = TestController().asTransaction()
    val topTransaction = TestController().asTransaction()
    val backstack = listOf(rootTransaction, middleTransaction, topTransaction)
    router.setBackstack(backstack, null)
    Assert.assertEquals(3, router.backstackSize.toLong())

    val fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(middleTransaction, fetchedBackstack[1])
    Assert.assertEquals(topTransaction, fetchedBackstack[2])
  }

  @Test
  fun testNewSetBackstack() {
    router.setRoot(TestController().asTransaction())
    Assert.assertEquals(1, router.backstackSize.toLong())

    val rootTransaction = TestController().asTransaction()
    val middleTransaction = TestController().asTransaction()
    val topTransaction = TestController().asTransaction()
    val backstack = listOf(rootTransaction, middleTransaction, topTransaction)
    router.setBackstack(backstack, null)

    Assert.assertEquals(3, router.backstackSize.toLong())
    val fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(middleTransaction, fetchedBackstack[1])
    Assert.assertEquals(topTransaction, fetchedBackstack[2])
    Assert.assertEquals(router, rootTransaction.controller.getRouter())
    Assert.assertEquals(router, middleTransaction.controller.getRouter())
    Assert.assertEquals(router, topTransaction.controller.getRouter())
  }

  @Test
  fun testNewSetBackstackWithNoRemoveViewOnPush() {
    val oldRootTransaction = TestController().asTransaction()
    val oldTopTransaction = TestController().asTransaction(
      pushChangeHandler = MockChangeHandler.noRemoveViewOnPushHandler()
    )
    router.setRoot(oldRootTransaction)
    router.pushController(oldTopTransaction)
    Assert.assertEquals(2, router.backstackSize.toLong())
    Assert.assertTrue(oldRootTransaction.controller.isAttached)
    Assert.assertTrue(oldTopTransaction.controller.isAttached)

    val rootTransaction = TestController().asTransaction()
    val middleTransaction = TestController().asTransaction(
      pushChangeHandler = MockChangeHandler.noRemoveViewOnPushHandler()
    )
    val topTransaction = TestController().asTransaction(
      pushChangeHandler = MockChangeHandler.noRemoveViewOnPushHandler()
    )
    val backstack = listOf(rootTransaction, middleTransaction, topTransaction)
    router.setBackstack(backstack, null)

    Assert.assertEquals(3, router.backstackSize.toLong())
    val fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(middleTransaction, fetchedBackstack[1])
    Assert.assertEquals(topTransaction, fetchedBackstack[2])
    Assert.assertFalse(oldRootTransaction.controller.isAttached)
    Assert.assertFalse(oldTopTransaction.controller.isAttached)
    Assert.assertTrue(rootTransaction.controller.isAttached)
    Assert.assertTrue(middleTransaction.controller.isAttached)
    Assert.assertTrue(topTransaction.controller.isAttached)
  }

  @Test
  fun testPopToRoot() {
    val rootTransaction = TestController().asTransaction()
    val transaction1 = TestController().asTransaction()
    val transaction2 = TestController().asTransaction()
    val backstack = listOf(rootTransaction, transaction1, transaction2)
    router.setBackstack(backstack, null)
    Assert.assertEquals(3, router.backstackSize.toLong())

    router.popToRoot()
    Assert.assertEquals(1, router.backstackSize.toLong())
    Assert.assertEquals(rootTransaction, router.getBackstack()[0])
    Assert.assertTrue(rootTransaction.controller.isAttached)
    Assert.assertFalse(transaction1.controller.isAttached)
    Assert.assertFalse(transaction2.controller.isAttached)
  }

  @Test
  fun testPopToRootWithNoRemoveViewOnPush() {
    val rootTransaction = TestController().asTransaction(
      pushChangeHandler = HorizontalChangeHandler(false)
    )
    val transaction1 = TestController().asTransaction(
      pushChangeHandler = HorizontalChangeHandler(false)
    )
    val transaction2 = TestController().asTransaction(
      pushChangeHandler = HorizontalChangeHandler(false)
    )
    val backstack = listOf(rootTransaction, transaction1, transaction2)
    router.setBackstack(backstack, null)
    Assert.assertEquals(3, router.backstackSize.toLong())

    router.popToRoot()
    Assert.assertEquals(1, router.backstackSize.toLong())
    Assert.assertEquals(rootTransaction, router.getBackstack()[0])
    Assert.assertTrue(rootTransaction.controller.isAttached)
    Assert.assertFalse(transaction1.controller.isAttached)
    Assert.assertFalse(transaction2.controller.isAttached)
  }

  @Test
  fun testReplaceTopController() {
    val rootTransaction = TestController().asTransaction()
    val topTransaction = TestController().asTransaction()
    val backstack = listOf(rootTransaction, topTransaction)
    router.setBackstack(backstack, null)
    Assert.assertEquals(2, router.backstackSize.toLong())

    var fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(topTransaction, fetchedBackstack[1])

    val newTopTransaction = TestController().asTransaction()
    router.replaceTopController(newTopTransaction)
    Assert.assertEquals(2, router.backstackSize.toLong())
    fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(newTopTransaction, fetchedBackstack[1])
  }

  @Test
  fun testReplaceTopControllerWithNoRemoveViewOnPush() {
    val rootTransaction = TestController().asTransaction()
    val topTransaction = TestController().asTransaction(
      pushChangeHandler = MockChangeHandler.noRemoveViewOnPushHandler()
    )
    val backstack = listOf(rootTransaction, topTransaction)
    router.setBackstack(backstack, null)
    Assert.assertEquals(2, router.backstackSize.toLong())
    Assert.assertTrue(rootTransaction.controller.isAttached)
    Assert.assertTrue(topTransaction.controller.isAttached)

    var fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(topTransaction, fetchedBackstack[1])

    val newTopTransaction = TestController().asTransaction(
      pushChangeHandler = MockChangeHandler.noRemoveViewOnPushHandler()
    )
    router.replaceTopController(newTopTransaction)
    newTopTransaction.pushChangeHandler()!!.completeImmediately()
    Assert.assertEquals(2, router.backstackSize.toLong())
    fetchedBackstack = router.getBackstack()
    Assert.assertEquals(rootTransaction, fetchedBackstack[0])
    Assert.assertEquals(newTopTransaction, fetchedBackstack[1])
    Assert.assertTrue(rootTransaction.controller.isAttached)
    Assert.assertFalse(topTransaction.controller.isAttached)
    Assert.assertTrue(newTopTransaction.controller.isAttached)
  }

  @Test
  fun testRearrangeTransactionBackstack() {
    val transaction1 = TestController().asTransaction()
    val transaction2 = TestController().asTransaction()
    var backstack = listOf(transaction1, transaction2)
    router.setBackstack(backstack, null)
    Assert.assertEquals(1, transaction1.transactionIndex.toLong())
    Assert.assertEquals(2, transaction2.transactionIndex.toLong())

    backstack = listOf(transaction2, transaction1)
    router.setBackstack(backstack, null)
    Assert.assertEquals(1, transaction2.transactionIndex.toLong())
    Assert.assertEquals(2, transaction1.transactionIndex.toLong())

    router.handleBack()
    Assert.assertEquals(1, router.backstackSize.toLong())
    Assert.assertEquals(transaction2, router.getBackstack()[0])

    router.handleBack()
    Assert.assertEquals(0, router.backstackSize.toLong())
  }

  @Test
  fun testChildRouterRearrangeTransactionBackstack() {
    val parent = TestController()
    router.setRoot(parent.asTransaction())
    val childRouter = parent.getChildRouter(
      parent.view!!.findViewById(TestController.CHILD_VIEW_ID_1)
    )
    val transaction1 = TestController().asTransaction()
    val transaction2 = TestController().asTransaction()
    var backstack = listOf(transaction1, transaction2)
    childRouter.setBackstack(backstack, null)
    Assert.assertEquals(2, transaction1.transactionIndex.toLong())
    Assert.assertEquals(3, transaction2.transactionIndex.toLong())

    backstack = listOf(transaction2, transaction1)
    childRouter.setBackstack(backstack, null)
    Assert.assertEquals(2, transaction2.transactionIndex.toLong())
    Assert.assertEquals(3, transaction1.transactionIndex.toLong())

    childRouter.handleBack()
    Assert.assertEquals(1, childRouter.backstackSize.toLong())
    Assert.assertEquals(transaction2, childRouter.getBackstack()[0])

    childRouter.handleBack()
    Assert.assertEquals(0, childRouter.backstackSize.toLong())
  }

  @Test
  fun testRemovesAllViewsOnDestroy() {
    router.setRoot(TestController().asTransaction())
    router.pushController(
      TestController().asTransaction(
        pushChangeHandler = FadeChangeHandler(false)
      )
    )
    Assert.assertEquals(2, router.container.childCount.toLong())

    router.destroy(true)
    Assert.assertEquals(0, router.container.childCount.toLong())
  }

  @Test
  fun testIsBeingDestroyed() {
    val lifecycleListener: LifecycleListener = object : LifecycleListener() {
      override fun preDestroyView(controller: Controller, view: View) {
        Assert.assertTrue(controller.isBeingDestroyed())
      }
    }
    val controller1 = TestController()
    val controller2 = TestController()
    controller2.addLifecycleListener(lifecycleListener)
    router.setRoot(controller1.asTransaction())
    router.pushController(controller2.asTransaction())
    Assert.assertFalse(controller1.isBeingDestroyed())
    Assert.assertFalse(controller2.isBeingDestroyed())

    router.popCurrentController()
    Assert.assertFalse(controller1.isBeingDestroyed())
    Assert.assertTrue(controller2.isBeingDestroyed())

    val controller3 = TestController()
    controller3.addLifecycleListener(lifecycleListener)
    router.pushController(controller3.asTransaction())
    Assert.assertFalse(controller1.isBeingDestroyed())
    Assert.assertFalse(controller3.isBeingDestroyed())

    router.popToRoot()
    Assert.assertFalse(controller1.isBeingDestroyed())
    Assert.assertTrue(controller3.isBeingDestroyed())
  }
}