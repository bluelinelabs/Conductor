package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.CallState;
import com.bluelinelabs.conductor.util.CallStateOwner;
import com.bluelinelabs.conductor.util.TestController;
import com.bluelinelabs.conductor.util.TestMasterDetailController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MasterDetailControllerTests {

    private ActivityProxy activityProxy;
    private Router router;

    private RouterTransaction masterTransaction1 = RouterTransaction.with(new TestController());
    private RouterTransaction masterTransaction2 = RouterTransaction.with(new TestController()).tag("Test1");
    private RouterTransaction masterTransaction3 = RouterTransaction.with(new TestController()).tag("Test2");

    private RouterTransaction detailTransaction1 = RouterTransaction.with(new TestController());
    private RouterTransaction detailTransaction2 = RouterTransaction.with(new TestController()).tag("Test1");
    private RouterTransaction detailTransaction3 = RouterTransaction.with(new TestController()).tag("Test2");

    public void createActivityController(Bundle savedInstanceState) {
        activityProxy = new ActivityProxy().create(savedInstanceState).start().resume();
        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
    }

    @Before
    public void setup() {
        createActivityController(null);
    }


    @Test
    public void testActivityResult() {
        TestMasterDetailController controller = new TestMasterDetailController();
        CallState expectedCallState = new CallState(true);

        router.pushController(RouterTransaction.with(controller));

        // Ensure that calling onActivityResult w/o requesting a result doesn't do anything
        router.onActivityResult(1, Activity.RESULT_OK, null);
        assertCalls(expectedCallState, controller);

        // Ensure starting an activity for result gets us the result back
        controller.startActivityForResult(new Intent("action"), 1);
        router.onActivityResult(1, Activity.RESULT_OK, null);
        expectedCallState.onActivityResultCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure requesting a result w/o calling startActivityForResult works
        controller.registerForActivityResult(2);
        router.onActivityResult(2, Activity.RESULT_OK, null);
        expectedCallState.onActivityResultCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testActivityResultForMasterDetailPortrait() {
        testActivityResultForMasterDetail(false);
    }

    @Test
    public void testActivityResultForMasterDetailLandscape() {
        testActivityResultForMasterDetail(true);
    }

    @Test
    public void testPermissionResult() {
        final String[] requestedPermissions = new String[] {"test"};

        TestMasterDetailController controller = new TestMasterDetailController();
        CallState expectedCallState = new CallState(true);

        router.pushController(RouterTransaction.with(controller));

        // Ensure that calling handleRequestedPermission w/o requesting a result doesn't do anything
        router.onRequestPermissionsResult("anotherId", 1, requestedPermissions, new int[] {1});
        assertCalls(expectedCallState, controller);

        // Ensure requesting the permission gets us the result back
        try {
            controller.requestPermissions(requestedPermissions, 1);
        } catch (NoSuchMethodError ignored) { }

        router.onRequestPermissionsResult(controller.getInstanceId(), 1, requestedPermissions, new int[] {1});
        expectedCallState.onRequestPermissionsResultCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testPermissionResultForMasterDetailPortrait() {
        testPermissionResultForMasterDetail(false);
    }

    @Test
    public void testPermissionResultForMasterDetailLandscape() {
        testPermissionResultForMasterDetail(true);
    }

    @Test
    public void testOptionsMenu() {
        TestMasterDetailController controller = new TestMasterDetailController();
        CallState expectedCallState = new CallState(true);

        router.pushController(RouterTransaction.with(controller));

        // Ensure that calling onCreateOptionsMenu w/o declaring that we have one doesn't do anything
        router.onCreateOptionsMenu(null, null);
        assertCalls(expectedCallState, controller);

        // Ensure calling onCreateOptionsMenu with a menu works
        controller.setHasOptionsMenu(true);

        // Ensure it'll still get called back next time onCreateOptionsMenu is called
        router.onCreateOptionsMenu(null, null);
        expectedCallState.createOptionsMenuCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure we stop getting them when we hide it
        controller.setOptionsMenuHidden(true);
        router.onCreateOptionsMenu(null, null);
        assertCalls(expectedCallState, controller);

        // Ensure we get the callback them when we un-hide it
        controller.setOptionsMenuHidden(false);
        router.onCreateOptionsMenu(null, null);
        expectedCallState.createOptionsMenuCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure we don't get the callback when we no longer have a menu
        controller.setHasOptionsMenu(false);
        router.onCreateOptionsMenu(null, null);
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testOptionsMenuForMasterDetailPortrait() {
        testOptionsMenuForMasterDetail(false);
    }

    @Test
    public void testOptionsMenuForMasterDetailLandscape() {
        testOptionsMenuForMasterDetail(true);
    }

    @Test
    public void testRestoreBackstacksOnRotate() {
        TestMasterDetailController controller = new TestMasterDetailController();
        router.setRoot(RouterTransaction.with(controller).tag("root"));
        assertFalse(controller.isTwoPanesMode());

        TestController masterController = new TestController();
        controller.getMasterRouter().setRoot(RouterTransaction.with(masterController));

        TestController masterController1 = new TestController();
        masterController.getRouter().pushController(RouterTransaction.with(masterController1));

        TestController detailController = new TestController();
        controller.getDetailRouter().setRoot(RouterTransaction.with(detailController));

        TestController detailController1 = new TestController();
        detailController.getRouter().pushController(RouterTransaction.with(detailController1));

        assertEquals(2, controller.getMasterRouter().getBackstackSize());
        assertEquals(2, controller.getDetailRouter().getBackstackSize());

        RouterTransaction masterTransaction = controller.getMasterRouter().getBackstack().get(0);
        RouterTransaction masterTransaction1 = controller.getMasterRouter().getBackstack().get(1);
        RouterTransaction detailTransaction = controller.getDetailRouter().getBackstack().get(0);
        RouterTransaction detailTransaction1 = controller.getDetailRouter().getBackstack().get(1);

        assertEquals(masterController, masterTransaction.controller());
        assertEquals(masterController1, masterTransaction1.controller());
        assertEquals(detailController, detailTransaction.controller());
        assertEquals(detailController1, detailTransaction1.controller());

        controller = rotateAndGetController("root");
        assertTrue(controller.isTwoPanesMode());

        assertEquals(2, controller.getMasterRouter().getBackstackSize());
        assertEquals(2, controller.getDetailRouter().getBackstackSize());
        compareTransactions(masterTransaction, controller.getMasterRouter().getBackstack().get(0));
        compareTransactions(masterTransaction1, controller.getMasterRouter().getBackstack().get(1));
        compareTransactions(detailTransaction, controller.getDetailRouter().getBackstack().get(0));
        compareTransactions(detailTransaction1, controller.getDetailRouter().getBackstack().get(1));
    }

    @Test
    public void testPopCurrentController() {
        TestMasterDetailController controller = prepareController("root");
        controller.getMasterRouter().popCurrentController();
        controller.getDetailRouter().popCurrentController();

        assertEquals(2, controller.getMasterRouter().getBackstackSize());
        assertEquals(2, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
    }

    @Test
    public void testPopToRootController() {
        TestMasterDetailController controller = prepareController("root");
        controller.getMasterRouter().popToRoot();
        controller.getDetailRouter().popToRoot();

        assertEquals(1, controller.getMasterRouter().getBackstackSize());
        assertEquals(1, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
    }

    @Test
    public void testPopToTagController() {
        TestMasterDetailController controller = prepareController("root");
        controller.getMasterRouter().popToTag("Test1");
        controller.getDetailRouter().popToTag("Test1");

        assertEquals(2, controller.getMasterRouter().getBackstackSize());
        assertEquals(2, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
    }

    @Test
    public void testPushController() {
        TestMasterDetailController controller = prepareController("root");
        RouterTransaction masterTransaction4 = RouterTransaction.with(new TestController());
        RouterTransaction detailTransaction4 = RouterTransaction.with(new TestController());

        controller.getMasterRouter().pushController(masterTransaction4);
        controller.getDetailRouter().pushController(detailTransaction4);

        assertEquals(4, controller.getMasterRouter().getBackstackSize());
        assertEquals(4, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(masterTransaction3, controller.getMasterRouter().getBackstack().get(2));
        assertEquals(masterTransaction4, controller.getMasterRouter().getBackstack().get(3));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));
        assertEquals(detailTransaction4, controller.getDetailRouter().getBackstack().get(3));
    }

    @Test
    public void testReplaceTopController() {
        TestMasterDetailController controller = prepareController("root");
        RouterTransaction masterTransaction4 = RouterTransaction.with(new TestController());
        RouterTransaction detailTransaction4 = RouterTransaction.with(new TestController());

        controller.getMasterRouter().replaceTopController(masterTransaction4);
        controller.getDetailRouter().replaceTopController(detailTransaction4);

        assertEquals(3, controller.getMasterRouter().getBackstackSize());
        assertEquals(3, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(masterTransaction4, controller.getMasterRouter().getBackstack().get(2));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction4, controller.getDetailRouter().getBackstack().get(2));
    }

    @Test
    public void testSetRoot() {
        TestMasterDetailController controller = prepareController("root");
        RouterTransaction masterTransaction4 = RouterTransaction.with(new TestController());
        RouterTransaction detailTransaction4 = RouterTransaction.with(new TestController());

        controller.getMasterRouter().setRoot(masterTransaction4);
        assertEquals(1, controller.getMasterRouter().getBackstackSize());
        assertEquals(3, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction4, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));

        controller.getDetailRouter().setRoot(detailTransaction4);
        assertEquals(1, controller.getMasterRouter().getBackstackSize());
        assertEquals(1, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction4, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(detailTransaction4.controller(), controller.getDetailRouter().getBackstack().get(0).controller());
    }

    @Test
    public void testPopToRootFromMaster() {
        TestMasterDetailController controller = prepareController("root");
        Controller masterController4 = new TestController();
        RouterTransaction masterTransaction4 = RouterTransaction.with(masterController4);
        controller.getMasterRouter().pushController(masterTransaction4);

        assertEquals(4, controller.getMasterRouter().getBackstackSize());
        assertEquals(3, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(masterTransaction3, controller.getMasterRouter().getBackstack().get(2));
        assertEquals(masterTransaction4, controller.getMasterRouter().getBackstack().get(3));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));

        masterController4.getRouter().popToRoot();
        assertEquals(1, controller.getMasterRouter().getBackstackSize());
        assertEquals(3, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));
    }

    @Test
    public void testPopToRootFromDetail() {
        TestMasterDetailController controller = prepareController("root");
        Controller detailController4 = new TestController();
        RouterTransaction detailTransaction4 = RouterTransaction.with(detailController4);
        controller.getDetailRouter().pushController(detailTransaction4);

        assertEquals(3, controller.getMasterRouter().getBackstackSize());
        assertEquals(4, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(masterTransaction3, controller.getMasterRouter().getBackstack().get(2));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));
        assertEquals(detailTransaction4, controller.getDetailRouter().getBackstack().get(3));

        detailController4.getRouter().popToRoot();
        assertEquals(3, controller.getMasterRouter().getBackstackSize());
        assertEquals(1, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(masterTransaction3, controller.getMasterRouter().getBackstack().get(2));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
    }

    @Test
    public void testControllersPushedToProperBackstacks() {
        TestMasterDetailController controller = prepareController("root");
        assertFalse(controller.isTwoPanesMode());

        Controller masterController4 = new TestController();
        RouterTransaction masterTransaction4 = RouterTransaction.with(masterController4);
        Controller masterController3 = controller.getMasterRouter().getBackstack().get(2).controller();
        masterController3.getRouter().pushController(masterTransaction4);

        Controller detailController4 = new TestController();
        RouterTransaction detailTransaction4 = RouterTransaction.with(detailController4);
        Controller detailController3 = controller.getDetailRouter().getBackstack().get(2).controller();
        detailController3.getRouter().pushController(detailTransaction4);

        assertEquals(4, controller.getMasterRouter().getBackstackSize());
        assertEquals(4, controller.getDetailRouter().getBackstackSize());
        assertEquals(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        assertEquals(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        assertEquals(masterTransaction3, controller.getMasterRouter().getBackstack().get(2));
        assertEquals(masterTransaction4, controller.getMasterRouter().getBackstack().get(3));
        assertEquals(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        assertEquals(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        assertEquals(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));
        assertEquals(detailTransaction4, controller.getDetailRouter().getBackstack().get(3));

        controller = rotateAndGetController("root");
        assertTrue(controller.isTwoPanesMode());
        assertEquals(4, controller.getMasterRouter().getBackstackSize());
        assertEquals(4, controller.getDetailRouter().getBackstackSize());
        compareTransactions(masterTransaction1, controller.getMasterRouter().getBackstack().get(0));
        compareTransactions(masterTransaction2, controller.getMasterRouter().getBackstack().get(1));
        compareTransactions(masterTransaction3, controller.getMasterRouter().getBackstack().get(2));
        compareTransactions(masterTransaction4, controller.getMasterRouter().getBackstack().get(3));
        compareTransactions(detailTransaction1, controller.getDetailRouter().getBackstack().get(0));
        compareTransactions(detailTransaction2, controller.getDetailRouter().getBackstack().get(1));
        compareTransactions(detailTransaction3, controller.getDetailRouter().getBackstack().get(2));
        compareTransactions(detailTransaction4, controller.getDetailRouter().getBackstack().get(3));
    }

    private TestMasterDetailController prepareController(String tag) {
        TestMasterDetailController controller = new TestMasterDetailController();
        router.setRoot(RouterTransaction.with(controller).tag(tag));
        assertFalse(controller.isTwoPanesMode());

        controller.getMasterRouter().pushController(masterTransaction1);
        controller.getMasterRouter().pushController(masterTransaction2);
        controller.getMasterRouter().pushController(masterTransaction3);
        controller.getDetailRouter().pushController(detailTransaction1);
        controller.getDetailRouter().pushController(detailTransaction2);
        controller.getDetailRouter().pushController(detailTransaction3);

        return controller;
    }

    private TestMasterDetailController rotateAndGetController(String tag) {
        Bundle bundle = new Bundle();
        activityProxy.saveInstanceState(bundle);
        activityProxy.stop(true);
        activityProxy.rotate();

        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), bundle);
        Controller controller = router.getControllerWithTag(tag);
        assertTrue(controller instanceof TestMasterDetailController);

        return (TestMasterDetailController)controller;
    }

    private void compareTransactions(RouterTransaction transaction1, RouterTransaction transaction2) {
        assertEquals(transaction1.isDetail(), transaction2.isDetail());
        assertEquals(transaction1.tag(), transaction2.tag());
        assertEquals(transaction1.controller().getInstanceId(), transaction2.controller().getInstanceId());
    }

    private void testActivityResultForMasterDetail(boolean isLandscape) {
        if (isLandscape) {
            activityProxy.rotate();
        }

        TestMasterDetailController controller = new TestMasterDetailController();
        router.setRoot(RouterTransaction.with(controller));

        assertEquals(isLandscape, controller.isTwoPanesMode());

        TestController masterController = new TestController();
        controller.getMasterRouter().setRoot(RouterTransaction.with(masterController));
        testActivityResultForChildController(masterController);

        TestController masterController1 = new TestController();
        masterController.getRouter().pushController(RouterTransaction.with(masterController1));
        testActivityResultForChildController(masterController1);

        TestController detailController = new TestController();
        controller.getDetailRouter().setRoot(RouterTransaction.with(detailController));
        testActivityResultForChildController(detailController);

        TestController detailController1 = new TestController();
        detailController.getRouter().pushController(RouterTransaction.with(detailController1));
        testActivityResultForChildController(detailController1);
    }

    private void testActivityResultForChildController(TestController controller) {
        CallState expectedCallState = new CallState(true);

        // Ensure that calling onActivityResult w/o requesting a result doesn't do anything
        router.onActivityResult(1, Activity.RESULT_OK, null);
        assertCalls(expectedCallState, controller);

        // Ensure starting an activity for result gets us the result back
        controller.startActivityForResult(new Intent("action"), 1);
        router.onActivityResult(1, Activity.RESULT_OK, null);
        expectedCallState.onActivityResultCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure requesting a result w/o calling startActivityForResult works
        controller.registerForActivityResult(2);
        router.onActivityResult(2, Activity.RESULT_OK, null);
        expectedCallState.onActivityResultCalls++;
        assertCalls(expectedCallState, controller);
    }

    private void testPermissionResultForMasterDetail(boolean isLandscape) {
        if (isLandscape) {
            activityProxy.rotate();
        }

        TestMasterDetailController controller = new TestMasterDetailController();
        router.setRoot(RouterTransaction.with(controller));

        assertEquals(isLandscape, controller.isTwoPanesMode());

        TestController masterController = new TestController();
        controller.getMasterRouter().setRoot(RouterTransaction.with(masterController));
        testPermissionResultForChildController(controller, masterController);

        TestController masterController1 = new TestController();
        masterController.getRouter().pushController(RouterTransaction.with(masterController1));
        testPermissionResultForChildController(controller, masterController1);

        TestController detailController = new TestController();
        controller.getDetailRouter().setRoot(RouterTransaction.with(detailController));
        testPermissionResultForChildController(controller, detailController);

        TestController detailController1 = new TestController();
        detailController.getRouter().pushController(RouterTransaction.with(detailController1));
        testPermissionResultForChildController(controller, detailController1);
    }

    private void testPermissionResultForChildController(TestMasterDetailController parent, TestController child) {
        final String[] requestedPermissions = new String[] {"test"};

        CallState childExpectedCallState = new CallState(true);
        CallState parentExpectedCallState = new CallState(true);

        // Ensure that calling handleRequestedPermission w/o requesting a result doesn't do anything
        router.onRequestPermissionsResult("anotherId", 1, requestedPermissions, new int[] {1});
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure requesting the permission gets us the result back
        try {
            child.requestPermissions(requestedPermissions, 1);
        } catch (NoSuchMethodError ignored) { }

        router.onRequestPermissionsResult(child.getInstanceId(), 1, requestedPermissions, new int[] {1});
        childExpectedCallState.onRequestPermissionsResultCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);
    }

    private void testOptionsMenuForMasterDetail(boolean isLandscape) {
        if (isLandscape) {
            activityProxy.rotate();
        }

        TestMasterDetailController controller = new TestMasterDetailController();
        router.setRoot(RouterTransaction.with(controller));

        assertEquals(isLandscape, controller.isTwoPanesMode());

        TestController masterController = new TestController();
        controller.getMasterRouter().setRoot(RouterTransaction.with(masterController));
        testOptionsMenuForChildController(controller, masterController);

        TestController masterController1 = new TestController();
        masterController.getRouter().pushController(RouterTransaction.with(masterController1));
        testOptionsMenuForChildController(controller, masterController1);

        TestController detailController = new TestController();
        controller.getDetailRouter().setRoot(RouterTransaction.with(detailController));
        testOptionsMenuForChildController(controller, detailController);

        TestController detailController1 = new TestController();
        detailController.getRouter().pushController(RouterTransaction.with(detailController1));
        testOptionsMenuForChildController(controller, detailController1);
    }

    private void testOptionsMenuForChildController(TestMasterDetailController parent, TestController child) {
        CallState childExpectedCallState = new CallState(true);
        CallState parentExpectedCallState = new CallState(true);

        // Ensure that calling onCreateOptionsMenu w/o declaring that we have one doesn't do anything
        router.onCreateOptionsMenu(null, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure calling onCreateOptionsMenu with a menu works
        child.setHasOptionsMenu(true);

        // Ensure it'll still get called back next time onCreateOptionsMenu is called
        router.onCreateOptionsMenu(null, null);
        childExpectedCallState.createOptionsMenuCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure we stop getting them when we hide it
        child.setOptionsMenuHidden(true);
        router.onCreateOptionsMenu(null, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure we get the callback them when we un-hide it
        child.setOptionsMenuHidden(false);
        router.onCreateOptionsMenu(null, null);
        childExpectedCallState.createOptionsMenuCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure we don't get the callback when we no longer have a menu
        child.setHasOptionsMenu(false);
        router.onCreateOptionsMenu(null, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);
    }

    private void assertCalls(CallState callState, CallStateOwner controller) {
        assertEquals("Expected call counts and controller call counts do not match.", callState, controller.currentCallState());
    }

}
