package com.bluelinelabs.conductor;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.bluelinelabs.conductor.Controller.RetainViewMode;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.CallState;
import com.bluelinelabs.conductor.util.MockChangeHandler;
import com.bluelinelabs.conductor.util.MockChangeHandler.ChangeHandlerListener;
import com.bluelinelabs.conductor.util.TestController;
import com.bluelinelabs.conductor.util.ViewUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ControllerLifecycleCallbacksTests {

    private Router router;

    private ActivityProxy activityProxy;
    private CallState currentCallState;

    public void createActivityController(Bundle savedInstanceState, boolean includeStartAndResume) {
        activityProxy = new ActivityProxy().create(savedInstanceState);

        if (includeStartAndResume) {
            activityProxy.start().resume();
        }

        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new TestController()));
        }
    }

    @Before
    public void setup() {
        createActivityController(null, true);

        currentCallState = new CallState(false);
    }

    @Test
    public void testNormalLifecycle() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller))
                .popChangeHandler(getPopHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        router.popCurrentController();

        assertNull(controller.getView());

        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityStop() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        activityProxy.getActivity().isDestroying = true;
        activityProxy.pause();

        assertCalls(expectedCallState, controller);

        activityProxy.stop(false);

        expectedCallState.detachCalls++;
        assertCalls(expectedCallState, controller);

        assertNotNull(controller.getView());
        ViewUtils.reportAttached(controller.getView(), false);

        expectedCallState.saveViewStateCalls++;
        expectedCallState.destroyViewCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityDestroy() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        activityProxy.getActivity().isDestroying = true;
        activityProxy.pause();

        assertCalls(expectedCallState, controller);

        activityProxy.stop(true);

        expectedCallState.saveViewStateCalls++;
        expectedCallState.detachCalls++;
        expectedCallState.destroyViewCalls++;
        assertCalls(expectedCallState, controller);

        activityProxy.destroy();

        expectedCallState.contextUnavailableCalls++;
        expectedCallState.destroyCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityConfigurationChange() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller))
                .tag("root"));

        assertCalls(expectedCallState, controller);

        activityProxy.getActivity().isChangingConfigurations = true;

        Bundle bundle = new Bundle();
        activityProxy.saveInstanceState(bundle);

        expectedCallState.saveViewStateCalls++;
        expectedCallState.saveInstanceStateCalls++;
        assertCalls(expectedCallState, controller);

        activityProxy.pause();
        assertCalls(expectedCallState, controller);

        activityProxy.stop(true);
        expectedCallState.detachCalls++;
        expectedCallState.destroyViewCalls++;
        assertCalls(expectedCallState, controller);

        activityProxy.destroy();
        expectedCallState.contextUnavailableCalls++;
        assertCalls(expectedCallState, controller);

        createActivityController(bundle, false);
        controller = (TestController)router.getControllerWithTag("root");

        expectedCallState.contextAvailableCalls++;
        expectedCallState.restoreInstanceStateCalls++;
        expectedCallState.restoreViewStateCalls++;
        expectedCallState.changeStartCalls++;
        expectedCallState.createViewCalls++;

        // Lifecycle listener isn't attached during restore, grab the current views from the controller for this stuff...
        currentCallState.restoreInstanceStateCalls = controller.currentCallState.restoreInstanceStateCalls;
        currentCallState.restoreViewStateCalls = controller.currentCallState.restoreViewStateCalls;
        currentCallState.changeStartCalls = controller.currentCallState.changeStartCalls;
        currentCallState.changeEndCalls = controller.currentCallState.changeEndCalls;
        currentCallState.createViewCalls = controller.currentCallState.createViewCalls;
        currentCallState.attachCalls = controller.currentCallState.attachCalls;
        currentCallState.contextAvailableCalls = controller.currentCallState.contextAvailableCalls;

        assertCalls(expectedCallState, controller);

        activityProxy.start().resume();
        currentCallState.changeEndCalls = controller.currentCallState.changeEndCalls;
        currentCallState.attachCalls = controller.currentCallState.attachCalls;
        expectedCallState.changeEndCalls++;
        expectedCallState.attachCalls++;

        assertCalls(expectedCallState, controller);

        activityProxy.resume();
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityBackground() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        activityProxy.pause();

        Bundle bundle = new Bundle();
        activityProxy.saveInstanceState(bundle);

        expectedCallState.saveInstanceStateCalls++;
        expectedCallState.saveViewStateCalls++;
        assertCalls(expectedCallState, controller);

        activityProxy.resume();

        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleCallOrder() {
        final TestController testController = new TestController();
        final CallState callState = new CallState(false);

        testController.addLifecycleListener(new LifecycleListener() {
            @Override
            public void preCreateView(@NonNull Controller controller) {
                callState.createViewCalls++;
                assertEquals(1, callState.createViewCalls);
                assertEquals(0, testController.currentCallState.createViewCalls);

                assertEquals(0, callState.attachCalls);
                assertEquals(0, testController.currentCallState.attachCalls);

                assertEquals(0, callState.detachCalls);
                assertEquals(0, testController.currentCallState.detachCalls);

                assertEquals(0, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                callState.createViewCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(0, callState.attachCalls);
                assertEquals(0, testController.currentCallState.attachCalls);

                assertEquals(0, callState.detachCalls);
                assertEquals(0, testController.currentCallState.detachCalls);

                assertEquals(0, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                callState.attachCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(1, callState.attachCalls);
                assertEquals(0, testController.currentCallState.attachCalls);

                assertEquals(0, callState.detachCalls);
                assertEquals(0, testController.currentCallState.detachCalls);

                assertEquals(0, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postAttach(@NonNull Controller controller, @NonNull View view) {
                callState.attachCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(0, callState.detachCalls);
                assertEquals(0, testController.currentCallState.detachCalls);

                assertEquals(0, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                callState.detachCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(1, callState.detachCalls);
                assertEquals(0, testController.currentCallState.detachCalls);

                assertEquals(0, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postDetach(@NonNull Controller controller, @NonNull View view) {
                callState.detachCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(2, callState.detachCalls);
                assertEquals(1, testController.currentCallState.detachCalls);

                assertEquals(0, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                callState.destroyViewCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(2, callState.detachCalls);
                assertEquals(1, testController.currentCallState.detachCalls);

                assertEquals(1, callState.destroyViewCalls);
                assertEquals(0, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postDestroyView(@NonNull Controller controller) {
                callState.destroyViewCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(2, callState.detachCalls);
                assertEquals(1, testController.currentCallState.detachCalls);

                assertEquals(2, callState.destroyViewCalls);
                assertEquals(1, testController.currentCallState.destroyViewCalls);

                assertEquals(0, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                callState.destroyCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(2, callState.detachCalls);
                assertEquals(1, testController.currentCallState.detachCalls);

                assertEquals(2, callState.destroyViewCalls);
                assertEquals(1, testController.currentCallState.destroyViewCalls);

                assertEquals(1, callState.destroyCalls);
                assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postDestroy(@NonNull Controller controller) {
                callState.destroyCalls++;
                assertEquals(2, callState.createViewCalls);
                assertEquals(1, testController.currentCallState.createViewCalls);

                assertEquals(2, callState.attachCalls);
                assertEquals(1, testController.currentCallState.attachCalls);

                assertEquals(2, callState.detachCalls);
                assertEquals(1, testController.currentCallState.detachCalls);

                assertEquals(2, callState.destroyViewCalls);
                assertEquals(1, testController.currentCallState.destroyViewCalls);

                assertEquals(2, callState.destroyCalls);
                assertEquals(1, testController.currentCallState.destroyCalls);
            }
        });

        router.pushController(RouterTransaction.with(testController)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        router.popController(testController);

        assertEquals(2, callState.createViewCalls);
        assertEquals(2, callState.attachCalls);
        assertEquals(2, callState.detachCalls);
        assertEquals(2, callState.destroyViewCalls);
        assertEquals(2, callState.destroyCalls);
    }

    @Test
    public void testChildLifecycle() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        attachLifecycleListener(child);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, child);

        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
            childRouter
                .setRoot(RouterTransaction.with(child)
                        .pushChangeHandler(getPushHandler(expectedCallState, child))
                        .popChangeHandler(getPopHandler(expectedCallState, child)));

        assertCalls(expectedCallState, child);

        parent.removeChildRouter(childRouter);

        assertCalls(expectedCallState, child);
    }

    @Test
    public void testChildLifecycle2() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        attachLifecycleListener(child);

        CallState expectedCallState = new CallState(false);

        assertCalls(expectedCallState, child);

        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
        childRouter
                .setRoot(RouterTransaction.with(child)
                        .pushChangeHandler(getPushHandler(expectedCallState, child))
                        .popChangeHandler(getPopHandler(expectedCallState, child)));

        assertCalls(expectedCallState, child);

        router.popCurrentController();

        expectedCallState.detachCalls++;
        expectedCallState.destroyViewCalls++;
        expectedCallState.contextUnavailableCalls++;
        expectedCallState.destroyCalls++;

        assertCalls(expectedCallState, child);
    }

    @Test
    public void testChildLifecycleOrderingAfterUnexpectedAttach() {
        Controller parent = new TestController();
        parent.setRetainViewMode(RetainViewMode.RETAIN_DETACH);
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        child.setRetainViewMode(RetainViewMode.RETAIN_DETACH);
        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
        childRouter
                .setRoot(RouterTransaction.with(child)
                        .pushChangeHandler(new SimpleSwapChangeHandler())
                        .popChangeHandler(new SimpleSwapChangeHandler()));

        assertTrue(parent.isAttached());
        assertTrue(child.isAttached());

        ViewUtils.reportAttached(parent.getView(), false, true);
        assertFalse(parent.isAttached());
        assertFalse(child.isAttached());

        ViewUtils.reportAttached(child.getView(), true);
        assertFalse(parent.isAttached());
        assertFalse(child.isAttached());

        ViewUtils.reportAttached(parent.getView(), true);
        assertTrue(parent.isAttached());
        assertTrue(child.isAttached());
    }

    private MockChangeHandler getPushHandler(final CallState expectedCallState, final TestController controller) {
        return MockChangeHandler.listeningChangeHandler(new ChangeHandlerListener() {
            @Override
            public void willStartChange() {
                expectedCallState.contextAvailableCalls++;
                expectedCallState.changeStartCalls++;
                expectedCallState.createViewCalls++;
                assertCalls(expectedCallState, controller);
            }

            @Override
            public void didAttachOrDetach() {
                expectedCallState.attachCalls++;
                assertCalls(expectedCallState, controller);
            }

            @Override
            public void didEndChange() {
                expectedCallState.changeEndCalls++;
                assertCalls(expectedCallState, controller);
            }
        });
    }

    private MockChangeHandler getPopHandler(final CallState expectedCallState, final TestController controller) {
        return MockChangeHandler.listeningChangeHandler(new ChangeHandlerListener() {
            @Override
            public void willStartChange() {
                expectedCallState.changeStartCalls++;
                assertCalls(expectedCallState, controller);
            }

            @Override
            public void didAttachOrDetach() {
                expectedCallState.destroyViewCalls++;
                expectedCallState.detachCalls++;
                expectedCallState.contextUnavailableCalls++;
                expectedCallState.destroyCalls++;
                assertCalls(expectedCallState, controller);
            }

            @Override
            public void didEndChange() {
                expectedCallState.changeEndCalls++;
                assertCalls(expectedCallState, controller);
            }
        });
    }

    private void assertCalls(CallState callState, TestController controller) {
        assertEquals("Expected call counts and controller call counts do not match.", callState, controller.currentCallState);
        assertEquals("Expected call counts and lifecycle call counts do not match.", callState, currentCallState);
    }

    private void attachLifecycleListener(Controller controller) {
        controller.addLifecycleListener(new LifecycleListener() {
            @Override
            public void onChangeStart(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
                currentCallState.changeStartCalls++;
            }

            @Override
            public void onChangeEnd(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
                currentCallState.changeEndCalls++;
            }

            @Override
            public void postContextAvailable(@NonNull Controller controller, @NonNull Context context) {
                currentCallState.contextAvailableCalls++;
            }

            @Override
            public void postContextUnavailable(@NonNull Controller controller) {
                currentCallState.contextUnavailableCalls++;
            }

            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                currentCallState.createViewCalls++;
            }

            @Override
            public void postAttach(@NonNull Controller controller, @NonNull View view) {
                currentCallState.attachCalls++;
            }

            @Override
            public void postDestroyView(@NonNull Controller controller) {
                currentCallState.destroyViewCalls++;
            }

            @Override
            public void postDetach(@NonNull Controller controller, @NonNull View view) {
                currentCallState.detachCalls++;
            }

            @Override
            public void postDestroy(@NonNull Controller controller) {
                currentCallState.destroyCalls++;
            }

            @Override
            public void onSaveInstanceState(@NonNull Controller controller, @NonNull Bundle outState) {
                currentCallState.saveInstanceStateCalls++;
            }

            @Override
            public void onRestoreInstanceState(@NonNull Controller controller, @NonNull Bundle savedInstanceState) {
                currentCallState.restoreInstanceStateCalls++;
            }

            @Override
            public void onSaveViewState(@NonNull Controller controller, @NonNull Bundle outState) {
                currentCallState.saveViewStateCalls++;
            }

            @Override
            public void onRestoreViewState(@NonNull Controller controller, @NonNull Bundle savedViewState) {
                currentCallState.restoreViewStateCalls++;
            }
        });
    }

}
