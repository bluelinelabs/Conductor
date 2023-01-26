package com.bluelinelabs.conductor

import android.content.Context
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.view.View
import com.bluelinelabs.conductor.Controller.LifecycleListener
import com.bluelinelabs.conductor.Controller.RetainViewMode
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import com.bluelinelabs.conductor.util.CallState
import com.bluelinelabs.conductor.util.MockChangeHandler
import com.bluelinelabs.conductor.util.MockChangeHandler.ChangeHandlerListener
import com.bluelinelabs.conductor.util.TestActivity
import com.bluelinelabs.conductor.util.ViewUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ControllerLifecycleCallbacksTests {

  private lateinit var activityController: ActivityController<TestActivity>
  private lateinit var currentCallState: CallState

  private fun createActivityController(savedInstanceState: Bundle?, includeStartAndResume: Boolean) {
    activityController = Robolectric.buildActivity(TestActivity::class.java)

    activityController.create(savedInstanceState)

    if (savedInstanceState != null) {
      activityController.restoreInstanceState(savedInstanceState)
    }

    if (includeStartAndResume) {
      activityController
        .start()
        .postCreate(savedInstanceState)
        .resume()
        .visible()
    }

    if (!activityController.get().router.hasRootController()) {
      activityController.get().router.setRoot(TestController().asTransaction())
    }
  }

  @Before
  fun setup() {
    createActivityController(null, true)
    currentCallState = CallState(false)
  }

  @Test
  fun testNormalLifecycle() {
    val controller = TestController()
    attachLifecycleListener(controller)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, controller)

    activityController.get().router.pushController(
      controller.asTransaction(
        pushChangeHandler = getPushHandler(expectedCallState, controller),
        popChangeHandler = getPopHandler(expectedCallState, controller)
      )
    )
    assertCalls(expectedCallState, controller)

    activityController.get().router.popCurrentController()
    Assert.assertNull(controller.view)
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testLifecycleWithActivityStop() {
    val controller = TestController()
    attachLifecycleListener(controller)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, controller)

    activityController.get().router.pushController(
      controller.asTransaction(
        pushChangeHandler = getPushHandler(expectedCallState, controller)
      )
    )
    assertCalls(expectedCallState, controller)

    activityController.get().destroying = true
    activityController.pause()
    assertCalls(expectedCallState, controller)

    activityController.stop()
    expectedCallState.detachCalls++
    assertCalls(expectedCallState, controller)
    Assert.assertNotNull(controller.view)

    ViewUtils.reportAttached(controller.view, false)
    expectedCallState.saveViewStateCalls++
    expectedCallState.destroyViewCalls++
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testLifecycleWithActivityDestroy() {
    val controller = TestController()
    attachLifecycleListener(controller)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, controller)

    activityController.get().router.pushController(
      controller.asTransaction(
        pushChangeHandler = getPushHandler(expectedCallState, controller)
      )
    )
    assertCalls(expectedCallState, controller)

    activityController.get().destroying = true
    activityController.pause()
    assertCalls(expectedCallState, controller)

    activityController.stop()
    expectedCallState.detachCalls++
    assertCalls(expectedCallState, controller)

    activityController.destroy()
    expectedCallState.destroyViewCalls++
    expectedCallState.contextUnavailableCalls++
    expectedCallState.destroyCalls++
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testLifecycleWithActivityConfigurationChange() {
    var controller = TestController()
    attachLifecycleListener(controller)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, controller)

    activityController.get().router.pushController(
      RouterTransaction.with(controller)
        .pushChangeHandler(getPushHandler(expectedCallState, controller))
        .tag("root")
    )
    assertCalls(expectedCallState, controller)

    activityController.get().changingConfigurations = true
    val bundle = Bundle()
    activityController.saveInstanceState(bundle)
    expectedCallState.saveViewStateCalls++
    expectedCallState.saveInstanceStateCalls++
    assertCalls(expectedCallState, controller)

    activityController.pause()
    assertCalls(expectedCallState, controller)

    activityController.stop()
    expectedCallState.detachCalls++
    assertCalls(expectedCallState, controller)

    activityController.destroy()
    expectedCallState.destroyViewCalls++
    expectedCallState.contextUnavailableCalls++
    assertCalls(expectedCallState, controller)

    createActivityController(bundle, false)
    controller = activityController.get().router.getControllerWithTag("root") as TestController
    expectedCallState.contextAvailableCalls++
    expectedCallState.restoreInstanceStateCalls++
    expectedCallState.restoreViewStateCalls++
    expectedCallState.changeStartCalls++
    expectedCallState.createViewCalls++

    // Lifecycle listener isn't attached during restore, grab the current views from the controller for this stuff...
    currentCallState.restoreInstanceStateCalls = controller.currentCallState.restoreInstanceStateCalls
    currentCallState.restoreViewStateCalls = controller.currentCallState.restoreViewStateCalls
    currentCallState.changeStartCalls = controller.currentCallState.changeStartCalls
    currentCallState.changeEndCalls = controller.currentCallState.changeEndCalls
    currentCallState.createViewCalls = controller.currentCallState.createViewCalls
    currentCallState.attachCalls = controller.currentCallState.attachCalls
    currentCallState.contextAvailableCalls = controller.currentCallState.contextAvailableCalls
    assertCalls(expectedCallState, controller)

    activityController
      .start()
      .postCreate(bundle)
      .resume()
      .visible()

    currentCallState.changeEndCalls = controller.currentCallState.changeEndCalls
    currentCallState.attachCalls = controller.currentCallState.attachCalls
    expectedCallState.changeEndCalls++
    expectedCallState.attachCalls++
    assertCalls(expectedCallState, controller)

    activityController.resume()
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testLifecycleWithActivityBackground() {
    val controller = TestController()
    attachLifecycleListener(controller)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, controller)

    activityController.get().router.pushController(
      controller.asTransaction(
        pushChangeHandler = getPushHandler(expectedCallState, controller)
      )
    )
    assertCalls(expectedCallState, controller)

    activityController.pause()
    val bundle = Bundle()
    activityController.saveInstanceState(bundle)
    expectedCallState.saveInstanceStateCalls++
    expectedCallState.saveViewStateCalls++
    assertCalls(expectedCallState, controller)

    activityController.resume()
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testLifecycleCallOrder() {
    val testController = TestController()
    val callState = CallState(false)
    testController.addLifecycleListener(object : LifecycleListener() {
      override fun preCreateView(controller: Controller) {
        callState.createViewCalls++
        Assert.assertEquals(1, callState.createViewCalls)
        Assert.assertEquals(0, testController.currentCallState.createViewCalls)
        Assert.assertEquals(0, callState.attachCalls)
        Assert.assertEquals(0, testController.currentCallState.attachCalls)
        Assert.assertEquals(0, callState.detachCalls)
        Assert.assertEquals(0, testController.currentCallState.detachCalls)
        Assert.assertEquals(0, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun postCreateView(controller: Controller, view: View) {
        callState.createViewCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(0, callState.attachCalls)
        Assert.assertEquals(0, testController.currentCallState.attachCalls)
        Assert.assertEquals(0, callState.detachCalls)
        Assert.assertEquals(0, testController.currentCallState.detachCalls)
        Assert.assertEquals(0, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun preAttach(controller: Controller, view: View) {
        callState.attachCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(1, callState.attachCalls)
        Assert.assertEquals(0, testController.currentCallState.attachCalls)
        Assert.assertEquals(0, callState.detachCalls)
        Assert.assertEquals(0, testController.currentCallState.detachCalls)
        Assert.assertEquals(0, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun postAttach(controller: Controller, view: View) {
        callState.attachCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(0, callState.detachCalls)
        Assert.assertEquals(0, testController.currentCallState.detachCalls)
        Assert.assertEquals(0, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun preDetach(controller: Controller, view: View) {
        callState.detachCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(1, callState.detachCalls)
        Assert.assertEquals(0, testController.currentCallState.detachCalls)
        Assert.assertEquals(0, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun postDetach(controller: Controller, view: View) {
        callState.detachCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(2, callState.detachCalls)
        Assert.assertEquals(1, testController.currentCallState.detachCalls)
        Assert.assertEquals(0, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun preDestroyView(controller: Controller, view: View) {
        callState.destroyViewCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(2, callState.detachCalls)
        Assert.assertEquals(1, testController.currentCallState.detachCalls)
        Assert.assertEquals(1, callState.destroyViewCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun postDestroyView(controller: Controller) {
        callState.destroyViewCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(2, callState.detachCalls)
        Assert.assertEquals(1, testController.currentCallState.detachCalls)
        Assert.assertEquals(2, callState.destroyViewCalls)
        Assert.assertEquals(1, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(0, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun preDestroy(controller: Controller) {
        callState.destroyCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(2, callState.detachCalls)
        Assert.assertEquals(1, testController.currentCallState.detachCalls)
        Assert.assertEquals(2, callState.destroyViewCalls)
        Assert.assertEquals(1, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(1, callState.destroyCalls)
        Assert.assertEquals(0, testController.currentCallState.destroyCalls)
      }

      override fun postDestroy(controller: Controller) {
        callState.destroyCalls++
        Assert.assertEquals(2, callState.createViewCalls)
        Assert.assertEquals(1, testController.currentCallState.createViewCalls)
        Assert.assertEquals(2, callState.attachCalls)
        Assert.assertEquals(1, testController.currentCallState.attachCalls)
        Assert.assertEquals(2, callState.detachCalls)
        Assert.assertEquals(1, testController.currentCallState.detachCalls)
        Assert.assertEquals(2, callState.destroyViewCalls)
        Assert.assertEquals(1, testController.currentCallState.destroyViewCalls)
        Assert.assertEquals(2, callState.destroyCalls)
        Assert.assertEquals(1, testController.currentCallState.destroyCalls)
      }
    })
    activityController.get().router.pushController(
      testController.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    activityController.get().router.popController(testController)
    Assert.assertEquals(2, callState.createViewCalls)
    Assert.assertEquals(2, callState.attachCalls)
    Assert.assertEquals(2, callState.detachCalls)
    Assert.assertEquals(2, callState.destroyViewCalls)
    Assert.assertEquals(2, callState.destroyCalls)
  }

  @Test
  fun testLifecycleWhenPopNonCurrentController() {
    val controller1Tag = "controller1"
    val controller2Tag = "controller2"
    val controller3Tag = "controller3"
    val controller1 = TestController()
    val controller2 = TestController()
    val controller3 = TestController()
    activityController.get().router.pushController(
      RouterTransaction.with(controller1).tag(controller1Tag)
    )
    activityController.get().router.pushController(
      RouterTransaction.with(controller2).tag(controller2Tag)
    )
    activityController.get().router.pushController(
      RouterTransaction.with(controller3).tag(controller3Tag)
    )
    activityController.get().router.popController(controller2)
    Assert.assertEquals(1, controller2.currentCallState.attachCalls)
    Assert.assertEquals(1, controller2.currentCallState.createViewCalls)
    Assert.assertEquals(1, controller2.currentCallState.detachCalls)
    Assert.assertEquals(1, controller2.currentCallState.destroyViewCalls)
    Assert.assertEquals(1, controller2.currentCallState.destroyCalls)
    Assert.assertEquals(1, controller2.currentCallState.contextAvailableCalls)
    Assert.assertEquals(1, controller2.currentCallState.contextUnavailableCalls)
    Assert.assertEquals(1, controller2.currentCallState.saveViewStateCalls)
    Assert.assertEquals(0, controller2.currentCallState.restoreViewStateCalls)
  }

  @Test
  fun testChildLifecycle() {
    val parent = TestController()
    activityController.get().router.pushController(
      parent.asTransaction(pushChangeHandler = MockChangeHandler.defaultHandler())
    )

    val child = TestController()
    attachLifecycleListener(child)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, child)

    val childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setRoot(
      child.asTransaction(
        pushChangeHandler = getPushHandler(expectedCallState, child),
        popChangeHandler = getPopHandler(expectedCallState, child)
      )
    )
    assertCalls(expectedCallState, child)

    parent.removeChildRouter(childRouter)
    assertCalls(expectedCallState, child)
  }

  @Test
  fun testChildLifecycle2() {
    val parent = TestController()
    activityController.get().router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    attachLifecycleListener(child)
    val expectedCallState = CallState(false)
    assertCalls(expectedCallState, child)

    val childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setRoot(
      child.asTransaction(
        pushChangeHandler = getPushHandler(expectedCallState, child),
        popChangeHandler = getPopHandler(expectedCallState, child)
      )
    )
    assertCalls(expectedCallState, child)

    activityController.get().router.popCurrentController()
    expectedCallState.changeStartCalls++
    expectedCallState.changeEndCalls++
    expectedCallState.detachCalls++
    expectedCallState.destroyViewCalls++
    expectedCallState.contextUnavailableCalls++
    expectedCallState.destroyCalls++
    assertCalls(expectedCallState, child)
  }

  @Test
  fun testChildLifecycleOrderingAfterUnexpectedAttach() {
    val parent = TestController()
    parent.retainViewMode = RetainViewMode.RETAIN_DETACH
    activityController.get().router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    child.retainViewMode = RetainViewMode.RETAIN_DETACH
    val childRouter = parent.getChildRouter(parent.getView()!!.findViewById(TestController.VIEW_ID))
    childRouter.setRoot(
      child.asTransaction(
        pushChangeHandler = SimpleSwapChangeHandler(),
        popChangeHandler = SimpleSwapChangeHandler()
      )
    )
    Assert.assertTrue(parent.isAttached)
    Assert.assertTrue(child.isAttached)

    ViewUtils.reportAttached(parent.view, false, true)
    Assert.assertFalse(parent.isAttached)
    Assert.assertFalse(child.isAttached)

    ViewUtils.reportAttached(child.view, true)
    Assert.assertFalse(parent.isAttached)
    Assert.assertFalse(child.isAttached)

    ViewUtils.reportAttached(parent.view, true)
    Assert.assertTrue(parent.isAttached)
    Assert.assertTrue(child.isAttached)
  }

  @Test
  fun testChildLifecycleAfterPushAndPop() {
    val parent = TestController()
    parent.retainViewMode = RetainViewMode.RETAIN_DETACH
    activityController.get().router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    val childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setRoot(
      child.asTransaction(
        pushChangeHandler = SimpleSwapChangeHandler(),
        popChangeHandler = SimpleSwapChangeHandler()
      )
    )

    val nextController = TestController()
    activityController.get().router.pushController(nextController.asTransaction())
    activityController.get().router.popCurrentController()

    shadowOf(getMainLooper()).idle()

    Assert.assertTrue(parent.isAttached)
    Assert.assertTrue(child.isAttached)
  }

  @Test
  fun testChildLifecycleAfterPushPopPush() {
    val parent = TestController()
    parent.retainViewMode = RetainViewMode.RETAIN_DETACH
    activityController.get().router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    val childRouter = parent.getChildRouter(parent.getView()!!.findViewById(TestController.VIEW_ID))
    childRouter.setRoot(
      child.asTransaction(
        pushChangeHandler = SimpleSwapChangeHandler(),
        popChangeHandler = SimpleSwapChangeHandler()
      )
    )

    val nextController = TestController()
    activityController.get().router.pushController(nextController.asTransaction())

    val child2 = TestController()
    childRouter.pushController(child2.asTransaction())
    activityController.get().router.popCurrentController()

    shadowOf(getMainLooper()).idle()

    Assert.assertTrue(parent.isAttached)
    Assert.assertFalse(child.isAttached)
    Assert.assertTrue(child2.isAttached)
  }

  private fun getPushHandler(
    expectedCallState: CallState,
    controller: TestController
  ): MockChangeHandler {
    return MockChangeHandler.listeningChangeHandler(object : ChangeHandlerListener() {
      override fun willStartChange() {
        expectedCallState.contextAvailableCalls++
        expectedCallState.changeStartCalls++
        expectedCallState.createViewCalls++
        assertCalls(expectedCallState, controller)
      }

      override fun didAttachOrDetach() {
        expectedCallState.attachCalls++
        assertCalls(expectedCallState, controller)
      }

      override fun didEndChange() {
        expectedCallState.changeEndCalls++
        assertCalls(expectedCallState, controller)
      }
    })
  }

  private fun getPopHandler(
    expectedCallState: CallState,
    controller: TestController
  ): MockChangeHandler {
    return MockChangeHandler.listeningChangeHandler(object : ChangeHandlerListener() {
      override fun willStartChange() {
        expectedCallState.changeStartCalls++
        assertCalls(expectedCallState, controller)
      }

      override fun didAttachOrDetach() {
        expectedCallState.destroyViewCalls++
        expectedCallState.detachCalls++
        expectedCallState.contextUnavailableCalls++
        expectedCallState.destroyCalls++
        assertCalls(expectedCallState, controller)
      }

      override fun didEndChange() {
        expectedCallState.changeEndCalls++
        assertCalls(expectedCallState, controller)
      }
    })
  }

  private fun assertCalls(callState: CallState, controller: TestController) {
    shadowOf(getMainLooper()).idle()

    Assert.assertEquals(
      "Expected call counts and controller call counts do not match.",
      callState,
      controller.currentCallState
    )
    Assert.assertEquals(
      "Expected call counts and lifecycle call counts do not match.",
      callState,
      currentCallState
    )
  }

  private fun attachLifecycleListener(controller: Controller?) {
    controller!!.addLifecycleListener(object : LifecycleListener() {
      override fun onChangeStart(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        currentCallState.changeStartCalls++
      }

      override fun onChangeEnd(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        currentCallState.changeEndCalls++
      }

      override fun postContextAvailable(controller: Controller, context: Context) {
        currentCallState.contextAvailableCalls++
      }

      override fun postContextUnavailable(controller: Controller) {
        currentCallState.contextUnavailableCalls++
      }

      override fun postCreateView(controller: Controller, view: View) {
        currentCallState.createViewCalls++
      }

      override fun postAttach(controller: Controller, view: View) {
        currentCallState.attachCalls++
      }

      override fun postDestroyView(controller: Controller) {
        currentCallState.destroyViewCalls++
      }

      override fun postDetach(controller: Controller, view: View) {
        currentCallState.detachCalls++
      }

      override fun postDestroy(controller: Controller) {
        currentCallState.destroyCalls++
      }

      override fun onSaveInstanceState(controller: Controller, outState: Bundle) {
        currentCallState.saveInstanceStateCalls++
      }

      override fun onRestoreInstanceState(controller: Controller, savedInstanceState: Bundle) {
        currentCallState.restoreInstanceStateCalls++
      }

      override fun onSaveViewState(controller: Controller, outState: Bundle) {
        currentCallState.saveViewStateCalls++
      }

      override fun onRestoreViewState(controller: Controller, savedViewState: Bundle) {
        currentCallState.restoreViewStateCalls++
      }
    })
  }
}