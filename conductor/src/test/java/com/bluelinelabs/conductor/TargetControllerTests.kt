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
class TargetControllerTests {

  private val router = Robolectric.buildActivity(TestActivity::class.java)
    .setup()
    .get()
    .router

  @Test
  fun testSiblingTarget() {
    val controllerA = TestController()
    val controllerB = TestController()
    Assert.assertNull(controllerA.targetController)
    Assert.assertNull(controllerB.targetController)

    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    controllerB.targetController = controllerA
    router.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertNull(controllerA.targetController)
    Assert.assertEquals(controllerA, controllerB.targetController)
  }

  @Test
  fun testParentChildTarget() {
    val controllerA = TestController()
    val controllerB = TestController()
    Assert.assertNull(controllerA.targetController)
    Assert.assertNull(controllerB.targetController)

    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    controllerB.targetController = controllerA
    val childRouter = controllerA.getChildRouter(
      controllerA.view!!.findViewById(TestController.VIEW_ID)
    )
    childRouter.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertNull(controllerA.targetController)
    Assert.assertEquals(controllerA, controllerB.targetController)
  }

  @Test
  fun testChildParentTarget() {
    val controllerA = TestController()
    val controllerB = TestController()
    Assert.assertNull(controllerA.targetController)
    Assert.assertNull(controllerB.targetController)

    router.pushController(
      controllerA.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    controllerA.targetController = controllerB
    val childRouter = controllerA.getChildRouter(
      controllerA.view!!.findViewById(TestController.VIEW_ID)
    )
    childRouter.pushController(
      controllerB.asTransaction(
        pushChangeHandler = MockChangeHandler.defaultHandler(),
        popChangeHandler = MockChangeHandler.defaultHandler()
      )
    )
    Assert.assertNull(controllerB.targetController)
    Assert.assertEquals(controllerB, controllerA.targetController)
  }
}