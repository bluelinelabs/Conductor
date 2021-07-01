package com.bluelinelabs.conductor

import android.os.Bundle
import android.os.Looper
import com.bluelinelabs.conductor.Conductor.attachRouter
import com.bluelinelabs.conductor.internal.LifecycleHandler
import com.bluelinelabs.conductor.util.ActivityProxy
import com.bluelinelabs.conductor.util.AttachFakingFrameLayout
import com.bluelinelabs.conductor.util.MockChangeHandler
import com.bluelinelabs.conductor.util.TestActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ReattachCaseTests {

  private val activityController = Robolectric.buildActivity(TestActivity::class.java).setup()
  private val router = activityController.get().router

  @Test
  fun testNeedsAttachingOnPauseAndOrientation() {
    val controllerA = TestController()
    val controllerB = TestController()
    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertFalse(controllerB.isAttached)

    sleepWakeDevice()
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertFalse(controllerB.isAttached)

    router.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)

    activityController.configurationChange()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
  }

  @Test
  fun testChildNeedsAttachOnPauseAndOrientation() {
    val controllerA = TestController()
    val childController = TestController()
    val controllerB = TestController()
    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    val childRouter = controllerA.getChildRouter(
      controllerA.view!!.findViewById(TestController.VIEW_ID)
    )
    childRouter.pushController(
      childController.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertTrue(childController.isAttached)
    Assert.assertFalse(controllerB.isAttached)

    sleepWakeDevice()
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertTrue(childController.isAttached)
    Assert.assertFalse(controllerB.isAttached)

    router.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertFalse(childController.isAttached)
    Assert.assertTrue(controllerB.isAttached)

    activityController.configurationChange()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertFalse(childController.isAttached)
    Assert.assertTrue(childController.needsAttach)
    Assert.assertTrue(controllerB.isAttached)
  }

  @Test
  fun testChildHandleBackOnOrientation() {
    val controllerA = TestController()
    val controllerB = TestController()
    val childController = TestController()
    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertFalse(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)

    router.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    val childRouter = controllerB.getChildRouter(
      controllerB.view!!.findViewById(TestController.VIEW_ID)
    )
    childRouter.setPopsLastView(true)
    childRouter.pushController(
      childController.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertTrue(childController.isAttached)

    activityController.configurationChange()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertTrue(childController.isAttached)

    router.handleBack()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)

    router.handleBack()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertFalse(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)
  }

  // Attempt to test https://github.com/bluelinelabs/Conductor/issues/86#issuecomment-231381271
  @Test
  fun testReusedChildRouterHandleBackOnOrientation() {
    val controllerA = TestController()
    val controllerB = TestController()
    var childController = TestController()
    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertFalse(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)

    router.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    val childRouter = controllerB.getChildRouter(
      controllerB.view!!.findViewById(TestController.VIEW_ID)
    )
    childRouter.setPopsLastView(true)
    childRouter.pushController(
      childController.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertTrue(childController.isAttached)

    router.handleBack()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)

    childController = TestController()
    childRouter.pushController(
      childController.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertTrue(childController.isAttached)

    activityController.configurationChange()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertTrue(childController.isAttached)

    router.handleBack()
    childController = TestController()
    childRouter.pushController(
      childController.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertTrue(childController.isAttached)

    router.handleBack()
    Assert.assertFalse(controllerA.isAttached)
    Assert.assertTrue(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)

    router.handleBack()
    Assert.assertTrue(controllerA.isAttached)
    Assert.assertFalse(controllerB.isAttached)
    Assert.assertFalse(childController.isAttached)
  }

  // Attempt to test https://github.com/bluelinelabs/Conductor/issues/367
  @Test
  fun testViewIsAttachedAfterStartedActivityIsRecreated() {
    val controller1 = TestController()
    val controller2 = TestController()
    router.setRoot(controller1.asTransaction())
    Assert.assertTrue(controller1.isAttached)

    // Lock screen
    val bundle = Bundle()
    activityController.pause().saveInstanceState(bundle).stop()

    // Push a 2nd controller, which will rotate the screen once it unlocked
    router.pushController(controller2.asTransaction())
    Assert.assertTrue(controller2.isAttached)
    Assert.assertTrue(controller2.needsAttach)

    // Unlock screen and rotate
    activityController.start()
    activityController.configurationChange()
    Assert.assertTrue(controller2.isAttached)
  }

  @Test
  fun testPopMiddleControllerAttaches() {
    var controller1 = TestController()
    var controller2 = TestController()
    var controller3 = TestController()
    router.setRoot(controller1.asTransaction())
    router.pushController(controller2.asTransaction())
    router.pushController(controller3.asTransaction())
    router.popController(controller2)
    Assert.assertFalse(controller1.isAttached)
    Assert.assertFalse(controller2.isAttached)
    Assert.assertTrue(controller3.isAttached)

    controller1 = TestController()
    controller2 = TestController()
    controller3 = TestController()
    router.setRoot(controller1.asTransaction())
    router.pushController(controller2.asTransaction())
    router.pushController(
      controller3.asTransaction(
        pushChangeHandler = MockChangeHandler.noRemoveViewOnPushHandler()
      )
    )
    router.popController(controller2)
    Assert.assertTrue(controller1.isAttached())
    Assert.assertFalse(controller2.isAttached())
    Assert.assertTrue(controller3.isAttached())
  }

  @Test
  fun testPendingChanges() {
    val controller1 = TestController()
    val controller2 = TestController()
    val activityProxy = ActivityProxy().create(null)
    val container = AttachFakingFrameLayout(activityProxy.activity)
    container.setNeedDelayPost(true) // to simulate calling posts after resume
    activityProxy.view = container
    val router = attachRouter(activityProxy.activity, container, null)
    router.setRoot(controller1.asTransaction())
    router.pushController(controller2.asTransaction())
    activityProxy.start().resume()
    container.setNeedDelayPost(false)
    Assert.assertTrue(controller2.isAttached)
  }

  @Test
  fun testPendingChangesAfterRotation() {
    val controller1 = TestController()
    val controller2 = TestController()

    // first activity
    var activityProxy = ActivityProxy().create(null)
    val container1 = AttachFakingFrameLayout(activityProxy.activity)
    container1.setNeedDelayPost(true) // delay forever as view will be removed
    activityProxy.view = container1

    // first attachRouter: Conductor.attachRouter(activityProxy.getActivity(), container1, null)
    val lifecycleHandler = LifecycleHandler.install(activityProxy.activity)
    var router = lifecycleHandler.getRouter(container1, null)
    router.setRoot(controller1.asTransaction())

    // setup controllers
    router.pushController(controller2.asTransaction())

    // simulate setRequestedOrientation in activity onCreate
    activityProxy.start().resume()
    val savedState = Bundle()
    activityProxy.saveInstanceState(savedState).pause().stop(true)

    // recreate activity and view
    activityProxy = ActivityProxy().create(savedState)
    val container2 = AttachFakingFrameLayout(activityProxy.activity)
    activityProxy.view = container2

    // second attach router with the same lifecycleHandler (do manually as robolectric recreates retained fragments)
    // Conductor.attachRouter(activityProxy.getActivity(), container2, savedState);
    router = lifecycleHandler.getRouter(container2, savedState)
    router.rebindIfNeeded()
    activityProxy.start().resume()
    Assert.assertTrue(controller2.isAttached)
  }

  @Test
  fun testHostAvailableDuringRotation() {
    val controllerA = TestController()
    val childControllerA = TestController()
    val controllerB = TestController()
    val childControllerB = TestController()
    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    val childRouterA = controllerA.getChildRouter(
      controllerA.view!!.findViewById(TestController.VIEW_ID)
    )
    childRouterA.pushController(
      childControllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertNotNull(controllerA.activity)
    Assert.assertNotNull(childControllerA.activity)

    router.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    val childRouterB = controllerB.getChildRouter(
      controllerB.view!!.findViewById(
        TestController.VIEW_ID
      )
    )
    childRouterB.pushController(
      childControllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertNotNull(controllerA.activity)
    Assert.assertNotNull(childControllerA.activity)
    Assert.assertNotNull(controllerB.activity)
    Assert.assertNotNull(childControllerB.activity)

    activityController.configurationChange()
    Assert.assertNotNull(controllerA.activity)
    Assert.assertNotNull(childControllerA.activity)
    Assert.assertNotNull(controllerB.activity)
    Assert.assertNotNull(childControllerB.activity)
  }

  private fun sleepWakeDevice() {
    activityController.saveInstanceState(Bundle()).pause()
    activityController.resume()
  }
}