package com.bluelinelabs.conductor

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import com.bluelinelabs.conductor.util.TestActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ViewLeakTests {

  private val activityController = Robolectric.buildActivity(TestActivity::class.java).setup()
  private val router = activityController.get().router

  @Before
  fun setup() {
    if (!router.hasRootController()) {
      router.setRoot(TestController().asTransaction())
    }
  }

  @Test
  fun testPop() {
    val controller = TestController()
    router.pushController(controller.asTransaction())
    Assert.assertNotNull(controller.getView())
    
    router.popCurrentController()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testPopWhenPushNeverAdded() {
    val controller = TestController()
    router.pushController(controller.asTransaction(pushChangeHandler = NeverAddChangeHandler()))
    Assert.assertNotNull(controller.getView())
    
    router.popCurrentController()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testPopWhenPushNeverCompleted() {
    val controller = TestController()
    router.pushController(controller.asTransaction(pushChangeHandler = NeverCompleteChangeHandler()))
    Assert.assertNotNull(controller.getView())
    
    router.popCurrentController()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testActivityDestroy() {
    val controller = TestController()
    router.pushController(controller.asTransaction())
    Assert.assertNotNull(controller.getView())
    
    activityController.stop().destroy()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testActivityDestroyWhenPushNeverCompleted() {
    val controller = TestController()
    router.pushController(controller.asTransaction(pushChangeHandler = NeverCompleteChangeHandler()))
    Assert.assertNotNull(controller.getView())
    
    activityController.stop().destroy()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testActivityDestroyWhenPushNeverAdded() {
    val controller = TestController()
    router.pushController(controller.asTransaction(pushChangeHandler = NeverAddChangeHandler()))
    Assert.assertNotNull(controller.getView())

    activityController.stop().destroy()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testViewRemovedIfLayeredNotRemovesFromViewOnPush() {
    val controller = TestController()
    router.pushController(controller.asTransaction())
    router.pushController(TestController().asTransaction(pushChangeHandler = SimpleSwapChangeHandler(false)))
    val view = controller.view
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNotNull(view.parent)

    router.pushController(TestController().asTransaction())
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNotNull(view.parent)

    router.popToRoot()
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertNull(view.parent)
  }

  class NeverAddChangeHandler : ControllerChangeHandler() {
    override fun performChange(
      container: ViewGroup,
      from: View?,
      to: View?,
      isPush: Boolean,
      changeListener: ControllerChangeCompletedListener
    ) {
      if (from != null) {
        container.removeView(from)
      }
    }
  }

  class NeverCompleteChangeHandler : ControllerChangeHandler() {
    override fun performChange(
      container: ViewGroup,
      from: View?,
      to: View?,
      isPush: Boolean,
      changeListener: ControllerChangeCompletedListener
    ) {
      if (from != null) {
        container.removeView(from)
      }
      container.addView(to)
    }
  }
}