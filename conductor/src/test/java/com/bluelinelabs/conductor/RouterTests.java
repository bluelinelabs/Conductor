package com.bluelinelabs.conductor;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.MockChangeHandler;
import com.bluelinelabs.conductor.util.TestController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RouterTests {

    private Router router;

    @Before
    public void setup() {
        ActivityProxy activityProxy = new ActivityProxy().create(null).start().resume();
        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), null);
    }

    @Test
    public void testSetRoot() {
        String rootTag = "root";

        Controller rootController = new TestController();

        assertFalse(router.hasRootController());

        router.setRoot(RouterTransaction.with(rootController).tag(rootTag));

        assertTrue(router.hasRootController());

        assertEquals(rootController, router.getControllerWithTag(rootTag));
    }

    @Test
    public void testSetNewRoot() {
        String oldRootTag = "oldRoot";
        String newRootTag = "newRoot";

        Controller oldRootController = new TestController();
        Controller newRootController = new TestController();

        router.setRoot(RouterTransaction.with(oldRootController).tag(oldRootTag));
        router.setRoot(RouterTransaction.with(newRootController).tag(newRootTag));

        assertNull(router.getControllerWithTag(oldRootTag));
        assertEquals(newRootController, router.getControllerWithTag(newRootTag));
    }

    @Test
    public void testGetByInstanceId() {
        Controller controller = new TestController();

        router.pushController(RouterTransaction.with(controller));

        assertEquals(controller, router.getControllerWithInstanceId(controller.getInstanceId()));
        assertNull(router.getControllerWithInstanceId("fake id"));
    }

    @Test
    public void testGetByTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        assertEquals(controller2, router.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPushPopControllers() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        assertEquals(1, router.getBackstackSize());

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        assertEquals(2, router.getBackstackSize());

        router.popCurrentController();

        assertEquals(1, router.getBackstackSize());

        assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        assertNull(router.getControllerWithTag(controller2Tag));

        router.popCurrentController();

        assertEquals(0, router.getBackstackSize());

        assertNull(router.getControllerWithTag(controller1Tag));
        assertNull(router.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPopControllerConcurrentModificationException() {
        int step = 1;
        for (int i = 0; i < 10; i++, step++) {
            router.pushController(RouterTransaction.with(new TestController()).tag("1"));
            router.pushController(RouterTransaction.with(new TestController()).tag("2"));
            router.pushController(RouterTransaction.with(new TestController()).tag("3"));

            String tag;
            if (step == 1) {
                tag = "1";
            } else if (step == 2) {
                tag = "2";
            } else {
                tag = "3";
                step = 0;
            }
            Controller controller = router.getControllerWithTag(tag);
            if (controller != null) {
                router.popController(controller);
            }
            router.popToRoot();
        }
    }

    @Test
    public void testPopToTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";
        String controller4Tag = "controller4";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();
        Controller controller4 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        router.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        router.pushController(RouterTransaction.with(controller4)
                .tag(controller4Tag));

        router.popToTag(controller2Tag);

        assertEquals(2, router.getBackstackSize());
        assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        assertEquals(controller2, router.getControllerWithTag(controller2Tag));
        assertNull(router.getControllerWithTag(controller3Tag));
        assertNull(router.getControllerWithTag(controller4Tag));
    }

    @Test
    public void testPopNonCurrent() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        router.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        router.popController(controller2);

        assertEquals(2, router.getBackstackSize());
        assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        assertNull(router.getControllerWithTag(controller2Tag));
        assertEquals(controller3, router.getControllerWithTag(controller3Tag));
    }

    @Test
    public void testSetBackstack() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction middleTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, middleTransaction, topTransaction);
        router.setBackstack(backstack, null);

        assertEquals(3, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(middleTransaction, fetchedBackstack.get(1));
        assertEquals(topTransaction, fetchedBackstack.get(2));
    }

    @Test
    public void testNewSetBackstack() {
        router.setRoot(RouterTransaction.with(new TestController()));

        assertEquals(1, router.getBackstackSize());

        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction middleTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, middleTransaction, topTransaction);
        router.setBackstack(backstack, null);

        assertEquals(3, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(middleTransaction, fetchedBackstack.get(1));
        assertEquals(topTransaction, fetchedBackstack.get(2));

        assertEquals(router, rootTransaction.controller.getRouter());
        assertEquals(router, middleTransaction.controller.getRouter());
        assertEquals(router, topTransaction.controller.getRouter());
    }

    @Test
    public void testNewSetBackstackWithNoRemoveViewOnPush() {
        RouterTransaction oldRootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction oldTopTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(MockChangeHandler.noRemoveViewOnPushHandler());

        router.setRoot(oldRootTransaction);
        router.pushController(oldTopTransaction);
        assertEquals(2, router.getBackstackSize());

        assertTrue(oldRootTransaction.controller.isAttached());
        assertTrue(oldTopTransaction.controller.isAttached());

        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction middleTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(MockChangeHandler.noRemoveViewOnPushHandler());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(MockChangeHandler.noRemoveViewOnPushHandler());

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, middleTransaction, topTransaction);
        router.setBackstack(backstack, null);

        assertEquals(3, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(middleTransaction, fetchedBackstack.get(1));
        assertEquals(topTransaction, fetchedBackstack.get(2));

        assertFalse(oldRootTransaction.controller.isAttached());
        assertFalse(oldTopTransaction.controller.isAttached());
        assertTrue(rootTransaction.controller.isAttached());
        assertTrue(middleTransaction.controller.isAttached());
        assertTrue(topTransaction.controller.isAttached());
    }

    @Test
    public void testPopToRoot() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction transaction1 = RouterTransaction.with(new TestController());
        RouterTransaction transaction2 = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, transaction1, transaction2);
        router.setBackstack(backstack, null);

        assertEquals(3, router.getBackstackSize());

        router.popToRoot();

        assertEquals(1, router.getBackstackSize());
        assertEquals(rootTransaction, router.getBackstack().get(0));

        assertTrue(rootTransaction.controller.isAttached());
        assertFalse(transaction1.controller.isAttached());
        assertFalse(transaction2.controller.isAttached());
    }

    @Test
    public void testPopToRootWithNoRemoveViewOnPush() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(new HorizontalChangeHandler(false));
        RouterTransaction transaction1 = RouterTransaction.with(new TestController()).pushChangeHandler(new HorizontalChangeHandler(false));
        RouterTransaction transaction2 = RouterTransaction.with(new TestController()).pushChangeHandler(new HorizontalChangeHandler(false));

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, transaction1, transaction2);
        router.setBackstack(backstack, null);

        assertEquals(3, router.getBackstackSize());

        router.popToRoot();

        assertEquals(1, router.getBackstackSize());
        assertEquals(rootTransaction, router.getBackstack().get(0));

        assertTrue(rootTransaction.controller.isAttached());
        assertFalse(transaction1.controller.isAttached());
        assertFalse(transaction2.controller.isAttached());
    }

    @Test
    public void testReplaceTopController() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, topTransaction);
        router.setBackstack(backstack, null);

        assertEquals(2, router.getBackstackSize());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(topTransaction, fetchedBackstack.get(1));

        RouterTransaction newTopTransaction = RouterTransaction.with(new TestController());
        router.replaceTopController(newTopTransaction);

        assertEquals(2, router.getBackstackSize());

        fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(newTopTransaction, fetchedBackstack.get(1));
    }

    @Test
    public void testReplaceTopControllerWithNoRemoveViewOnPush() {
        RouterTransaction rootTransaction = RouterTransaction.with(new TestController());
        RouterTransaction topTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(MockChangeHandler.noRemoveViewOnPushHandler());

        List<RouterTransaction> backstack = Arrays.asList(rootTransaction, topTransaction);
        router.setBackstack(backstack, null);

        assertEquals(2, router.getBackstackSize());

        assertTrue(rootTransaction.controller.isAttached());
        assertTrue(topTransaction.controller.isAttached());

        List<RouterTransaction> fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(topTransaction, fetchedBackstack.get(1));

        RouterTransaction newTopTransaction = RouterTransaction.with(new TestController()).pushChangeHandler(MockChangeHandler.noRemoveViewOnPushHandler());
        router.replaceTopController(newTopTransaction);
        newTopTransaction.pushChangeHandler().completeImmediately();

        assertEquals(2, router.getBackstackSize());

        fetchedBackstack = router.getBackstack();
        assertEquals(rootTransaction, fetchedBackstack.get(0));
        assertEquals(newTopTransaction, fetchedBackstack.get(1));

        assertTrue(rootTransaction.controller.isAttached());
        assertFalse(topTransaction.controller.isAttached());
        assertTrue(newTopTransaction.controller.isAttached());
    }

    @Test
    public void testRearrangeTransactionBackstack() {
        RouterTransaction transaction1 = RouterTransaction.with(new TestController());
        RouterTransaction transaction2 = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = Arrays.asList(transaction1, transaction2);
        router.setBackstack(backstack, null);

        assertEquals(1, transaction1.transactionIndex);
        assertEquals(2, transaction2.transactionIndex);

        backstack = Arrays.asList(transaction2, transaction1);
        router.setBackstack(backstack, null);

        assertEquals(1, transaction2.transactionIndex);
        assertEquals(2, transaction1.transactionIndex);

        router.handleBack();

        assertEquals(1, router.getBackstackSize());
        assertEquals(transaction2, router.getBackstack().get(0));

        router.handleBack();
        assertEquals(0, router.getBackstackSize());
    }

    @Test
    public void testChildRouterRearrangeTransactionBackstack() {
        Controller parent = new TestController();
        router.setRoot(RouterTransaction.with(parent));

        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.CHILD_VIEW_ID_1));

        RouterTransaction transaction1 = RouterTransaction.with(new TestController());
        RouterTransaction transaction2 = RouterTransaction.with(new TestController());

        List<RouterTransaction> backstack = Arrays.asList(transaction1, transaction2);
        childRouter.setBackstack(backstack, null);

        assertEquals(2, transaction1.transactionIndex);
        assertEquals(3, transaction2.transactionIndex);

        backstack = Arrays.asList(transaction2, transaction1);
        childRouter.setBackstack(backstack, null);

        assertEquals(2, transaction2.transactionIndex);
        assertEquals(3, transaction1.transactionIndex);

        childRouter.handleBack();

        assertEquals(1, childRouter.getBackstackSize());
        assertEquals(transaction2, childRouter.getBackstack().get(0));

        childRouter.handleBack();
        assertEquals(0, childRouter.getBackstackSize());
    }

    @Test
    public void testRemovesAllViewsOnDestroy() {
        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.setRoot(RouterTransaction.with(controller1));
        router.pushController(RouterTransaction.with(controller2)
                .pushChangeHandler(new FadeChangeHandler(false)));

        assertEquals(2, router.container.getChildCount());

        router.destroy(true);

        assertEquals(0, router.container.getChildCount());
    }

    @Test
    public void testIsBeingDestroyed() {
        final LifecycleListener lifecycleListener = new LifecycleListener() {
            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                assertTrue(controller.isBeingDestroyed());
            }
        };

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        controller2.addLifecycleListener(lifecycleListener);

        router.setRoot(RouterTransaction.with(controller1));
        router.pushController(RouterTransaction.with(controller2));
        assertFalse(controller1.isBeingDestroyed());
        assertFalse(controller2.isBeingDestroyed());

        router.popCurrentController();
        assertFalse(controller1.isBeingDestroyed());
        assertTrue(controller2.isBeingDestroyed());

        Controller controller3 = new TestController();
        controller3.addLifecycleListener(lifecycleListener);
        router.pushController(RouterTransaction.with(controller3));
        assertFalse(controller1.isBeingDestroyed());
        assertFalse(controller3.isBeingDestroyed());

        router.popToRoot();
        assertFalse(controller1.isBeingDestroyed());
        assertTrue(controller3.isBeingDestroyed());
    }

}
