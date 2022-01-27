package com.bluelinelabs.conductor

import android.os.Looper.getMainLooper
import android.view.View
import com.bluelinelabs.conductor.Controller.LifecycleListener
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
class ControllerLifecycleActivityReferenceTests {

  private val activityController = Robolectric.buildActivity(TestActivity::class.java).setup()
  private val activity = activityController.get()

  @Test
  fun testSingleControllerActivityOnPush() {
    val controller = TestController()
    Assert.assertNull(controller.activity)

    val listener = ActivityReferencingLifecycleListener()
    controller.addLifecycleListener(listener)

    activity.router.pushController(
      controller.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    Assert.assertEquals(listOf(true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(emptyList<Any>(), listener.postDetachReferences)
    Assert.assertEquals(emptyList<Any>(), listener.postDestroyViewReferences)
    Assert.assertEquals(emptyList<Any>(), listener.postDestroyReferences)
  }

  @Test
  fun testChildControllerActivityOnPush() {
    val parent = TestController()
    activity.router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    Assert.assertNull(child.activity)

    val listener = ActivityReferencingLifecycleListener()
    child.addLifecycleListener(listener)

    val childRouter = parent.getChildRouter((parent.view!!.findViewById(TestController.VIEW_ID)))
    childRouter.pushController(
      child.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    Assert.assertEquals(listOf(true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(emptyList<Any>(), listener.postDetachReferences)
    Assert.assertEquals(emptyList<Any>(), listener.postDestroyViewReferences)
    Assert.assertEquals(emptyList<Any>(), listener.postDestroyReferences)
  }

  @Test
  fun testSingleControllerActivityOnPop() {
    val controller = TestController()
    val listener = ActivityReferencingLifecycleListener()
    controller.addLifecycleListener(listener)

    activity.router.pushController(
      controller.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    activity.router.popCurrentController()

    Assert.assertEquals(listOf(true, true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(listOf(true), listener.postDetachReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyViewReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyReferences)
  }

  @Test
  fun testChildControllerActivityOnPop() {
    val parent = TestController()
    activity.router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    val listener = ActivityReferencingLifecycleListener()
    child.addLifecycleListener(listener)

    val childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setPopRootControllerMode(Router.PopRootControllerMode.POP_ROOT_CONTROLLER_AND_VIEW)
    childRouter.pushController(
      child.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    childRouter.popCurrentController()

    shadowOf(getMainLooper()).idle()

    Assert.assertEquals(listOf(true, true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(listOf(true), listener.postDetachReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyViewReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyReferences)
  }

  @Test
  fun testChildControllerActivityOnParentPop() {
    val parent = TestController()
    activity.router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    val listener = ActivityReferencingLifecycleListener()
    child.addLifecycleListener(listener)

    val childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setPopRootControllerMode(Router.PopRootControllerMode.POP_ROOT_CONTROLLER_AND_VIEW)
    childRouter.pushController(
      child.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    activity.router.popCurrentController()

    Assert.assertEquals(listOf(true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(listOf(true), listener.postDetachReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyViewReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyReferences)
  }

  @Test
  fun testSingleControllerActivityOnDestroy() {
    val controller = TestController()
    val listener = ActivityReferencingLifecycleListener()
    controller.addLifecycleListener(listener)

    activity.router.pushController(
      controller.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    activityController.pause().stop().destroy()

    Assert.assertEquals(listOf(true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(listOf(true), listener.postDetachReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyViewReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyReferences)
  }

  @Test
  fun testChildControllerActivityOnDestroy() {
    val parent = TestController()
    activity.router.pushController(
      parent.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )

    val child = TestController()
    val listener = ActivityReferencingLifecycleListener()
    child.addLifecycleListener(listener)
    val childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setPopRootControllerMode(Router.PopRootControllerMode.POP_ROOT_CONTROLLER_AND_VIEW)
    childRouter.pushController(
      child.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    activityController.pause().stop().destroy()

    Assert.assertEquals(listOf(true), listener.changeEndReferences)
    Assert.assertEquals(listOf(true), listener.postCreateViewReferences)
    Assert.assertEquals(listOf(true), listener.postAttachReferences)
    Assert.assertEquals(listOf(true), listener.postDetachReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyViewReferences)
    Assert.assertEquals(listOf(true), listener.postDestroyReferences)
  }

  internal class ActivityReferencingLifecycleListener : LifecycleListener() {
    val changeEndReferences = mutableListOf<Boolean>()
    val postCreateViewReferences = mutableListOf<Boolean>()
    val postAttachReferences = mutableListOf<Boolean>()
    val postDetachReferences = mutableListOf<Boolean>()
    val postDestroyViewReferences = mutableListOf<Boolean>()
    val postDestroyReferences = mutableListOf<Boolean>()

    override fun onChangeEnd(
      controller: Controller,
      changeHandler: ControllerChangeHandler,
      changeType: ControllerChangeType
    ) {
      changeEndReferences.add(controller.activity != null)
    }

    override fun postCreateView(controller: Controller, view: View) {
      postCreateViewReferences.add(controller.activity != null)
    }

    override fun postAttach(controller: Controller, view: View) {
      postAttachReferences.add(controller.activity != null)
    }

    override fun postDetach(controller: Controller, view: View) {
      postDetachReferences.add(controller.activity != null)
    }

    override fun postDestroyView(controller: Controller) {
      postDestroyViewReferences.add(controller.activity != null)
    }

    override fun postDestroy(controller: Controller) {
      postDestroyReferences.add(controller.activity != null)
    }
  }
}