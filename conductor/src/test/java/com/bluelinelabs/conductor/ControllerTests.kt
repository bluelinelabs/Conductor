package com.bluelinelabs.conductor

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import com.bluelinelabs.conductor.Controller.RetainViewMode
import com.bluelinelabs.conductor.util.AttachFakingFrameLayout
import com.bluelinelabs.conductor.util.CallState
import com.bluelinelabs.conductor.util.TestActivity
import com.bluelinelabs.conductor.util.ViewUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ControllerTests {

  private val router = Robolectric.buildActivity(TestActivity::class.java)
    .setup()
    .get()
    .router

  @Test
  fun testViewRetention() {
    val controller = TestController()
    controller.setRouter(router)

    // Test View getting released w/ RELEASE_DETACH
    controller.retainViewMode = RetainViewMode.RELEASE_DETACH
    Assert.assertNull(controller.getView())
    var view = controller.inflate(router.container)
    Assert.assertNotNull(controller.getView())
    ViewUtils.reportAttached(view, true)
    Assert.assertNotNull(controller.getView())
    ViewUtils.reportAttached(view, false)
    Assert.assertNull(controller.getView())

    // Test View getting retained w/ RETAIN_DETACH
    controller.retainViewMode = RetainViewMode.RETAIN_DETACH
    view = controller.inflate(router.container)
    Assert.assertNotNull(controller.getView())
    ViewUtils.reportAttached(view, true)
    Assert.assertNotNull(controller.getView())
    ViewUtils.reportAttached(view, false)
    Assert.assertNotNull(controller.getView())

    // Ensure re-setting RELEASE_DETACH releases
    controller.retainViewMode = RetainViewMode.RELEASE_DETACH
    Assert.assertNull(controller.getView())
  }

  @Test
  fun testActivityResult() {
    val controller = TestController()
    val expectedCallState = CallState(true)
    router.pushController(controller.asTransaction())

    // Ensure that calling onActivityResult w/o requesting a result doesn't do anything
    router.onActivityResult(1, Activity.RESULT_OK, null)
    assertCalls(expectedCallState, controller)

    // Ensure starting an activity for result gets us the result back
    controller.startActivityForResult(Intent("action"), 1)
    router.onActivityResult(1, Activity.RESULT_OK, null)
    expectedCallState.onActivityResultCalls++
    assertCalls(expectedCallState, controller)

    // Ensure requesting a result w/o calling startActivityForResult works
    controller.registerForActivityResult(2)
    router.onActivityResult(2, Activity.RESULT_OK, null)
    expectedCallState.onActivityResultCalls++
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testActivityResultForChild() {
    val parent = TestController()
    val child = TestController()
    router.pushController(parent.asTransaction())
    val childContainer = parent.view!!.findViewById<AttachFakingFrameLayout>(TestController.VIEW_ID)
    childContainer.setAttached(true)
    parent.getChildRouter(childContainer)
      .setRoot(child.asTransaction())
    val childExpectedCallState = CallState(true)
    val parentExpectedCallState = CallState(true)

    // Ensure that calling onActivityResult w/o requesting a result doesn't do anything
    router.onActivityResult(1, Activity.RESULT_OK, null)
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure starting an activity for result gets us the result back
    child.startActivityForResult(Intent("action"), 1)
    router.onActivityResult(1, Activity.RESULT_OK, null)
    childExpectedCallState.onActivityResultCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure requesting a result w/o calling startActivityForResult works
    child.registerForActivityResult(2)
    router.onActivityResult(2, Activity.RESULT_OK, null)
    childExpectedCallState.onActivityResultCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)
  }

  @Test
  fun testPermissionResult() {
    val requestedPermissions = arrayOf("test")
    val controller = TestController()
    val expectedCallState = CallState(true)
    router.pushController(controller.asTransaction())

    // Ensure that calling handleRequestedPermission w/o requesting a result doesn't do anything
    router.onRequestPermissionsResult("anotherId", 1, requestedPermissions, intArrayOf(1))
    assertCalls(expectedCallState, controller)

    // Ensure requesting the permission gets us the result back
    try {
      controller.requestPermissions(requestedPermissions, 1)
    } catch (ignored: NoSuchMethodError) { }

    router.onRequestPermissionsResult(
      controller.instanceId,
      1,
      requestedPermissions,
      intArrayOf(1)
    )
    expectedCallState.onRequestPermissionsResultCalls++
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testPermissionResultForChild() {
    val requestedPermissions = arrayOf("test")
    val parent = TestController()
    val child = TestController()
    router.pushController(parent.asTransaction())
    val childContainer = parent.view!!.findViewById<AttachFakingFrameLayout>(TestController.VIEW_ID)
    childContainer.setAttached(true)
    parent.getChildRouter(childContainer)
      .setRoot(child.asTransaction())
    val childExpectedCallState = CallState(true)
    val parentExpectedCallState = CallState(true)

    // Ensure that calling handleRequestedPermission w/o requesting a result doesn't do anything
    router.onRequestPermissionsResult("anotherId", 1, requestedPermissions, intArrayOf(1))
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure requesting the permission gets us the result back
    try {
      child.requestPermissions(requestedPermissions, 1)
    } catch (ignored: NoSuchMethodError) { }

    router.onRequestPermissionsResult(child.instanceId, 1, requestedPermissions, intArrayOf(1))
    childExpectedCallState.onRequestPermissionsResultCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)
  }

  @Test
  fun testOptionsMenu() {
    val controller = TestController()
    val expectedCallState = CallState(true)
    router.pushController(controller.asTransaction())

    // Ensure that calling onCreateOptionsMenu w/o declaring that we have one doesn't do anything
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    assertCalls(expectedCallState, controller)

    // Ensure calling onCreateOptionsMenu with a menu works
    controller.setHasOptionsMenu(true)
    expectedCallState.createOptionsMenuCalls++
    assertCalls(expectedCallState, controller)

    // Ensure it'll still get called back next time onCreateOptionsMenu is called
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    expectedCallState.createOptionsMenuCalls++
    assertCalls(expectedCallState, controller)

    // Ensure we stop getting them when we hide it
    controller.setOptionsMenuHidden(true)
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    assertCalls(expectedCallState, controller)

    // Ensure we get the callback them when we un-hide it
    controller.setOptionsMenuHidden(false)
    expectedCallState.createOptionsMenuCalls++
    assertCalls(expectedCallState, controller)

    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    expectedCallState.createOptionsMenuCalls++
    assertCalls(expectedCallState, controller)

    // Ensure we don't get the callback when we no longer have a menu
    controller.setHasOptionsMenu(false)
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    assertCalls(expectedCallState, controller)
  }

  @Test
  fun testOptionsMenuForChild() {
    val parent = TestController()
    val child = TestController()
    router.pushController(parent.asTransaction())
    val childContainer = parent.view!!.findViewById<AttachFakingFrameLayout>(TestController.VIEW_ID)
    childContainer.setAttached(true)
    parent.getChildRouter(childContainer)
      .setRoot(child.asTransaction())
    val childExpectedCallState = CallState(true)
    val parentExpectedCallState = CallState(true)

    // Ensure that calling onCreateOptionsMenu w/o declaring that we have one doesn't do anything
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure calling onCreateOptionsMenu with a menu works
    child.setHasOptionsMenu(true)
    childExpectedCallState.createOptionsMenuCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure it'll still get called back next time onCreateOptionsMenu is called
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    childExpectedCallState.createOptionsMenuCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure we stop getting them when we hide it
    child.setOptionsMenuHidden(true)
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure we get the callback them when we un-hide it
    child.setOptionsMenuHidden(false)
    childExpectedCallState.createOptionsMenuCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    childExpectedCallState.createOptionsMenuCalls++
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)

    // Ensure we don't get the callback when we no longer have a menu
    child.setHasOptionsMenu(false)
    router.onCreateOptionsMenu(menu(), menuInflater(router.activity!!))
    assertCalls(childExpectedCallState, child)
    assertCalls(parentExpectedCallState, parent)
  }

  @Test
  fun testAddRemoveChildControllers() {
    val parent = TestController()
    val child1 = TestController()
    val child2 = TestController()
    router.pushController(parent.asTransaction())
    Assert.assertEquals(0, parent.childRouters.size)
    Assert.assertNull(child1.parentController)
    Assert.assertNull(child2.parentController)

    var childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.setPopsLastView(true)
    childRouter.setRoot(child1.asTransaction())
    Assert.assertEquals(1, parent.childRouters.size)
    Assert.assertEquals(childRouter, parent.childRouters[0])
    Assert.assertEquals(1, childRouter.backstackSize)
    Assert.assertEquals(child1, childRouter.controllers[0])
    Assert.assertEquals(parent, child1.parentController)
    Assert.assertNull(child2.parentController)

    childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.VIEW_ID))
    childRouter.pushController(child2.asTransaction())
    Assert.assertEquals(1, parent.childRouters.size)
    Assert.assertEquals(childRouter, parent.childRouters[0])
    Assert.assertEquals(2, childRouter.backstackSize)
    Assert.assertEquals(child1, childRouter.controllers[0])
    Assert.assertEquals(child2, childRouter.controllers[1])
    Assert.assertEquals(parent, child1.parentController)
    Assert.assertEquals(parent, child2.parentController)

    childRouter.popController(child2)
    Assert.assertEquals(1, parent.childRouters.size)
    Assert.assertEquals(childRouter, parent.childRouters[0])
    Assert.assertEquals(1, childRouter.backstackSize)
    Assert.assertEquals(child1, childRouter.controllers[0])
    Assert.assertEquals(parent, child1.parentController)
    Assert.assertNull(child2.parentController)

    childRouter.popController(child1)
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertEquals(1, parent.childRouters.size)
    Assert.assertEquals(childRouter, parent.childRouters[0])
    Assert.assertEquals(0, childRouter.backstackSize)
    Assert.assertNull(child1.parentController)
    Assert.assertNull(child2.parentController)
  }

  @Test
  fun testAddRemoveChildRouters() {
    val parent = TestController()
    val child1 = TestController()
    val child2 = TestController()
    router.pushController(parent.asTransaction())
    Assert.assertEquals(0, parent.childRouters.size)
    Assert.assertNull(child1.parentController)
    Assert.assertNull(child2.parentController)

    val childRouter1 = parent.getChildRouter(parent.view!!.findViewById(TestController.CHILD_VIEW_ID_1))
    val childRouter2 = parent.getChildRouter(parent.view!!.findViewById(TestController.CHILD_VIEW_ID_2))
    childRouter1.setRoot(child1.asTransaction())
    childRouter2.setRoot(child2.asTransaction())
    Assert.assertEquals(2, parent.childRouters.size)
    Assert.assertEquals(childRouter1, parent.childRouters[0])
    Assert.assertEquals(childRouter2, parent.childRouters[1])
    Assert.assertEquals(1, childRouter1.backstackSize)
    Assert.assertEquals(1, childRouter2.backstackSize)
    Assert.assertEquals(child1, childRouter1.controllers[0])
    Assert.assertEquals(child2, childRouter2.controllers[0])
    Assert.assertEquals(parent, child1.parentController)
    Assert.assertEquals(parent, child2.parentController)

    parent.removeChildRouter(childRouter2)
    shadowOf(Looper.getMainLooper()).idle()
    Assert.assertEquals(1, parent.childRouters.size)
    Assert.assertEquals(childRouter1, parent.childRouters[0])
    Assert.assertEquals(1, childRouter1.backstackSize)
    Assert.assertEquals(0, childRouter2.backstackSize)
    Assert.assertEquals(child1, childRouter1.controllers[0])
    Assert.assertEquals(parent, child1.parentController)
    Assert.assertNull(child2.parentController)
    parent.removeChildRouter(childRouter1)
    Assert.assertEquals(0, parent.childRouters.size)
    Assert.assertEquals(0, childRouter1.backstackSize)
    Assert.assertEquals(0, childRouter2.backstackSize)
    Assert.assertNull(child1.parentController)
    Assert.assertNull(child2.parentController)
  }

  @Test
  fun testRestoredChildRouterBackstack() {
    val parent = TestController()
    router.pushController(parent.asTransaction())
    ViewUtils.reportAttached(parent.view, true)

    val childTransaction1 = TestController().asTransaction()
    val childTransaction2 = TestController().asTransaction()
    var childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.CHILD_VIEW_ID_1))
    childRouter.setPopsLastView(true)
    childRouter.setRoot(childTransaction1)
    childRouter.pushController(childTransaction2)
    val savedState = Bundle()
    childRouter.saveInstanceState(savedState)
    parent.removeChildRouter(childRouter)
    childRouter = parent.getChildRouter(parent.view!!.findViewById(TestController.CHILD_VIEW_ID_1))
    Assert.assertEquals(0, childRouter.backstackSize)

    childRouter.restoreInstanceState(savedState)
    childRouter.rebindIfNeeded()
    Assert.assertEquals(2, childRouter.backstackSize)
    val restoredChildTransaction1 = childRouter.getBackstack()[0]
    val restoredChildTransaction2 = childRouter.getBackstack()[1]
    Assert.assertEquals(
      childTransaction1.transactionIndex,
      restoredChildTransaction1.transactionIndex
    )
    Assert.assertEquals(
      childTransaction1.controller.getInstanceId(),
      restoredChildTransaction1.controller.getInstanceId()
    )
    Assert.assertEquals(
      childTransaction2.transactionIndex,
      restoredChildTransaction2.transactionIndex
    )
    Assert.assertEquals(
      childTransaction2.controller.getInstanceId(),
      restoredChildTransaction2.controller.getInstanceId()
    )
    Assert.assertTrue(parent.handleBack())
    Assert.assertEquals(1, childRouter.backstackSize)
    Assert.assertEquals(restoredChildTransaction1, childRouter.getBackstack()[0])
    Assert.assertTrue(parent.handleBack())
    Assert.assertEquals(0, childRouter.backstackSize)
  }

  private fun assertCalls(callState: CallState, controller: TestController) {
    shadowOf(Looper.getMainLooper()).idle()

    Assert.assertEquals(
      "Expected call counts and controller call counts do not match.",
      callState,
      controller.currentCallState
    )
  }

  private fun menu(): Menu {
    return object : Menu {
      override fun add(p0: CharSequence?): MenuItem {
        TODO("Not yet implemented")
      }

      override fun add(p0: Int): MenuItem {
        TODO("Not yet implemented")
      }

      override fun add(p0: Int, p1: Int, p2: Int, p3: CharSequence?): MenuItem {
        TODO("Not yet implemented")
      }

      override fun add(p0: Int, p1: Int, p2: Int, p3: Int): MenuItem {
        TODO("Not yet implemented")
      }

      override fun addSubMenu(p0: CharSequence?): SubMenu {
        TODO("Not yet implemented")
      }

      override fun addSubMenu(p0: Int): SubMenu {
        TODO("Not yet implemented")
      }

      override fun addSubMenu(p0: Int, p1: Int, p2: Int, p3: CharSequence?): SubMenu {
        TODO("Not yet implemented")
      }

      override fun addSubMenu(p0: Int, p1: Int, p2: Int, p3: Int): SubMenu {
        TODO("Not yet implemented")
      }

      override fun addIntentOptions(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: ComponentName?,
        p4: Array<out Intent>?,
        p5: Intent?,
        p6: Int,
        p7: Array<out MenuItem>?
      ): Int {
        TODO("Not yet implemented")
      }

      override fun removeItem(p0: Int) {
        TODO("Not yet implemented")
      }

      override fun removeGroup(p0: Int) {
        TODO("Not yet implemented")
      }

      override fun clear() {
        TODO("Not yet implemented")
      }

      override fun setGroupCheckable(p0: Int, p1: Boolean, p2: Boolean) {
        TODO("Not yet implemented")
      }

      override fun setGroupVisible(p0: Int, p1: Boolean) {
        TODO("Not yet implemented")
      }

      override fun setGroupEnabled(p0: Int, p1: Boolean) {
        TODO("Not yet implemented")
      }

      override fun hasVisibleItems(): Boolean {
        TODO("Not yet implemented")
      }

      override fun findItem(p0: Int): MenuItem {
        TODO("Not yet implemented")
      }

      override fun size(): Int {
        TODO("Not yet implemented")
      }

      override fun getItem(p0: Int): MenuItem {
        TODO("Not yet implemented")
      }

      override fun close() {
        TODO("Not yet implemented")
      }

      override fun performShortcut(p0: Int, p1: KeyEvent?, p2: Int): Boolean {
        TODO("Not yet implemented")
      }

      override fun isShortcutKey(p0: Int, p1: KeyEvent?): Boolean {
        TODO("Not yet implemented")
      }

      override fun performIdentifierAction(p0: Int, p1: Int): Boolean {
        TODO("Not yet implemented")
      }

      override fun setQwertyMode(p0: Boolean) {
        TODO("Not yet implemented")
      }
    }
  }

  private fun menuInflater(context: Context): MenuInflater {
    return MenuInflater(context)
  }
}