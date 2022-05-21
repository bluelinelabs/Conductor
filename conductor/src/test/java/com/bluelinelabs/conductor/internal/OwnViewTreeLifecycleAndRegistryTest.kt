package com.bluelinelabs.conductor.internal

import android.content.Context
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.TestController
import com.bluelinelabs.conductor.asTransaction
import com.bluelinelabs.conductor.util.TestActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class OwnViewTreeLifecycleAndRegistryTest {

  private val router = Robolectric.buildActivity(TestActivity::class.java)
    .setup()
    .get()
    .router

  @Test
  fun `onCreate lifecycle event before create view`() {
    assertControllerState(
      preCreateViewAssertedState = Lifecycle.State.CREATED,
      setup = { router.setRoot(it.asTransaction()) }
    )
  }

  @Test
  fun `onStart lifecycle event after create view`() {
    assertControllerState(
      postCreateViewAssertedState = Lifecycle.State.STARTED,
      setup = { router.setRoot(it.asTransaction()) }
    )
  }

  @Test
  fun `onResume lifecycle event on attach`() {
    assertControllerState(
      postAttachAssertedState = Lifecycle.State.RESUMED,
      setup = { router.setRoot(it.asTransaction()) }
    )
  }

  @Test
  fun `onPause lifecycle event on exit change start`() {
    assertControllerState(
      onChangeStartAssertedState = Lifecycle.State.STARTED,
      setup = {
        router.setRoot(it.asTransaction())
        router.pushController(TestController().asTransaction())
      }
    )
  }

  @Test
  fun `onPause lifecycle event on parent exit change start`() {
    val parent = TestController()
    val controller = TestController()
    var hasAsserted = false
    val ownViewTreeLifecycleAndRegistry = OwnViewTreeLifecycleAndRegistry.own(controller)

    // Ensure our listener gets added after OwnViewTreeLifecycleAndRegistry's by waiting until
    // postContextAvailable to add the lifecycle listener on the parent controller
    controller.addLifecycleListener(object : Controller.LifecycleListener() {
      override fun postContextAvailable(controller: Controller, context: Context) {
        parent.addLifecycleListener(object : Controller.LifecycleListener() {
          override fun onChangeStart(
            controller: Controller,
            changeHandler: ControllerChangeHandler,
            changeType: ControllerChangeType
          ) {
            Assert.assertEquals(Lifecycle.State.STARTED, ownViewTreeLifecycleAndRegistry.lifecycle.currentState)
            hasAsserted = true
          }
        })
      }
    })

    router.setRoot(parent.asTransaction())
    parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID)).setRoot(controller.asTransaction())
    router.pushController(TestController().asTransaction())
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    Assert.assertTrue(hasAsserted)
  }

  @Test
  fun `onStop lifecycle event on detach`() {
    assertControllerState(
      preDetachAssertedState = Lifecycle.State.CREATED,
      setup = {
        router.setRoot(it.asTransaction())
        router.pushController(TestController().asTransaction())
      }
    )
  }

  @Test
  fun `onDestroy lifecycle event on destroy view`() {
    assertControllerState(
      preDestroyViewAssertedState = Lifecycle.State.DESTROYED,
      setup = {
        router.setRoot(it.asTransaction())
        router.pushController(TestController().asTransaction())
      }
    )
  }

  private fun assertControllerState(
    preCreateViewAssertedState: Lifecycle.State? = null,
    postCreateViewAssertedState: Lifecycle.State? = null,
    postAttachAssertedState: Lifecycle.State? = null,
    preDetachAssertedState: Lifecycle.State? = null,
    preDestroyViewAssertedState: Lifecycle.State? = null,
    onChangeStartAssertedState: Lifecycle.State? = null,
    setup: (Controller) -> Unit = { },
  ) {
    val controller = TestController()
    val ownViewTreeLifecycleAndRegistry = OwnViewTreeLifecycleAndRegistry.own(controller)
    var hasAsserted = false

    val assertState: (Lifecycle.State) -> Unit = {
      Assert.assertEquals(it, ownViewTreeLifecycleAndRegistry.lifecycle.currentState)
    }

    controller.addLifecycleListener(object : Controller.LifecycleListener() {
      override fun preCreateView(controller: Controller) {
        preCreateViewAssertedState?.let {
          assertState(it)
          hasAsserted = true
        }
      }

      override fun postCreateView(controller: Controller, view: View) {
        postCreateViewAssertedState?.let {
          assertState(it)
          hasAsserted = true
        }
      }

      override fun postAttach(controller: Controller, view: View) {
        postAttachAssertedState?.let {
          assertState(it)
          hasAsserted = true
        }
      }

      override fun preDetach(controller: Controller, view: View) {
        preDetachAssertedState?.let {
          assertState(it)
          hasAsserted = true
        }
      }

      override fun preDestroyView(controller: Controller, view: View) {
        preDestroyViewAssertedState?.let {
          assertState(it)
          hasAsserted = true
        }
      }

      override fun onChangeStart(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        if (!changeType.isEnter) {
          onChangeStartAssertedState?.let {
            assertState(it)
            hasAsserted = true
          }
        }
      }
    })

    setup(controller)
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    Assert.assertTrue(hasAsserted)
  }
}