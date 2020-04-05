package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.MockChangeHandler;
import com.bluelinelabs.conductor.util.TestController;
import com.bluelinelabs.conductor.util.TestMasterDetailController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ControllerLifecycleActivityReferenceTests {

    private Router router;

    private ActivityProxy activityProxy;

    public void createActivityController(Bundle savedInstanceState, boolean includeStartAndResume) {
        activityProxy = new ActivityProxy().create(savedInstanceState);

        if (includeStartAndResume) {
            activityProxy.start().resume();
        }

        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
        router.setPopsLastView(true);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new TestController()));
        }
    }

    @Before
    public void setup() {
        createActivityController(null, true);
    }

    @Test
    public void testSingleControllerActivityOnPush() {
        testSingleControllerActivityOnPush(new TestController());
    }

    @Test
    public void testMasterDetailControllerActivityOnPush() {
        testSingleControllerActivityOnPush(new TestMasterDetailController());
    }

    @Test
    public void testChildControllerActivityOnPush() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router router = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
        testChildControllerActivityOnPush(router, child);
    }

    @Test
    public void testMasterControllerActivityOnPush() {
        TestMasterDetailController parent = new TestMasterDetailController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));
        TestController child = new TestController();
        Router router = parent.getMasterRouter();
        testChildControllerActivityOnPush(router, child);
    }

    @Test
    public void testDetailControllerActivityOnPush() {
        TestMasterDetailController parent = new TestMasterDetailController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));
        TestController child = new TestController();
        Router router = parent.getDetailRouter();
        testChildControllerActivityOnPush(router, child);
    }

    @Test
    public void testSingleControllerActivityOnPop() {
        testSingleControllerActivityOnPop(new TestController());
    }

    @Test
    public void testMasterDetailControllerActivityOnPop() {
        testSingleControllerActivityOnPop(new TestMasterDetailController());
    }

    @Test
    public void testChildControllerActivityOnPop() {
        Controller parent = new TestController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router router = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
        testChildControllerActivityOnPop(router, child);
    }

    @Test
    public void testMasterControllerActivityOnPop() {
        TestMasterDetailController parent = new TestMasterDetailController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router router = parent.getMasterRouter();
        testChildControllerActivityOnPop(router, child);
    }

    @Test
    public void testDetailControllerActivityOnPop() {
        TestMasterDetailController parent = new TestMasterDetailController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router router = parent.getDetailRouter();
        testChildControllerActivityOnPop(router, child);
    }

    @Test
    public void testChildControllerActivityOnParentPop() {
        Controller parent = new TestController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
        testChildControllerActivityOnParentPop(childRouter, child);
    }

    @Test
    public void testMasterControllerActivityOnParentPop() {
        TestMasterDetailController parent = new TestMasterDetailController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router childRouter = parent.getMasterRouter();
        testChildControllerActivityOnParentPop(childRouter, child);
    }

    @Test
    public void testDetailControllerActivityOnParentPop() {
        TestMasterDetailController parent = new TestMasterDetailController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router childRouter = parent.getDetailRouter();
        testChildControllerActivityOnParentPop(childRouter, child);
    }

    @Test
    public void testSingleControllerActivityOnDestroy() {
        testSingleControllerActivityOnDestroy(new TestController());
    }

    @Test
    public void testMasterDetailControllerActivityOnDestroy() {
        testSingleControllerActivityOnDestroy(new TestMasterDetailController());
    }

    @Test
    public void testChildControllerActivityOnDestroy() {
        Controller parent = new TestController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID));
        testChildControllerActivityOnDestroy(childRouter, child);
    }

    @Test
    public void testMasterControllerActivityOnDestroy() {
        TestMasterDetailController parent = new TestMasterDetailController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router childRouter = parent.getMasterRouter();
        testChildControllerActivityOnDestroy(childRouter, child);
    }

    @Test
    public void testDetailControllerActivityOnDestroy() {
        TestMasterDetailController parent = new TestMasterDetailController();

        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        TestController child = new TestController();
        Router childRouter = parent.getDetailRouter();
        testChildControllerActivityOnDestroy(childRouter, child);
    }

    private void testSingleControllerActivityOnPush(Controller controller) {
        assertNull(controller.getActivity());

        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        controller.addLifecycleListener(listener);

        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        assertEquals(Collections.singletonList(true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.emptyList(), listener.postDetachReferences);
        assertEquals(Collections.emptyList(), listener.postDestroyViewReferences);
        assertEquals(Collections.emptyList(), listener.postDestroyReferences);
    }

    private void testChildControllerActivityOnPush(Router childRouter, Controller child) {
        assertNull(child.getActivity());

        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        child.addLifecycleListener(listener);

        childRouter.pushController(RouterTransaction.with(child)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        assertEquals(Collections.singletonList(true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.emptyList(), listener.postDetachReferences);
        assertEquals(Collections.emptyList(), listener.postDestroyViewReferences);
        assertEquals(Collections.emptyList(), listener.postDestroyReferences);
    }

    private void testSingleControllerActivityOnPop(Controller controller) {
        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        controller.addLifecycleListener(listener);

        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        router.popCurrentController();

        assertEquals(Arrays.asList(true, true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.singletonList(true), listener.postDetachReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyViewReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyReferences);
    }

    private void testChildControllerActivityOnPop(Router childRouter, Controller child) {
        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        child.addLifecycleListener(listener);

        childRouter.setPopsLastView(true);
        childRouter.pushController(RouterTransaction.with(child)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        childRouter.popCurrentController();

        assertEquals(Arrays.asList(true, true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.singletonList(true), listener.postDetachReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyViewReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyReferences);
    }

    private void testChildControllerActivityOnParentPop(Router childRouter, Controller child) {
        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        child.addLifecycleListener(listener);

        childRouter.setPopsLastView(true);
        childRouter.pushController(RouterTransaction.with(child)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        router.popCurrentController();

        assertEquals(Collections.singletonList(true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.singletonList(true), listener.postDetachReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyViewReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyReferences);
    }

    private void testSingleControllerActivityOnDestroy(Controller controller) {
        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        controller.addLifecycleListener(listener);

        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        activityProxy.pause().stop(false).destroy();

        assertEquals(Collections.singletonList(true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.singletonList(true), listener.postDetachReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyViewReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyReferences);
    }

    private void testChildControllerActivityOnDestroy(Router childRouter, Controller child) {
        ActivityReferencingLifecycleListener listener = new ActivityReferencingLifecycleListener();
        child.addLifecycleListener(listener);

        childRouter.setPopsLastView(true);
        childRouter.pushController(RouterTransaction.with(child)
                .pushChangeHandler(MockChangeHandler.defaultHandler())
                .popChangeHandler(MockChangeHandler.defaultHandler()));

        activityProxy.pause().stop(false).destroy();

        assertEquals(Collections.singletonList(true), listener.changeEndReferences);
        assertEquals(Collections.singletonList(true), listener.postCreateViewReferences);
        assertEquals(Collections.singletonList(true), listener.postAttachReferences);
        assertEquals(Collections.singletonList(true), listener.postDetachReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyViewReferences);
        assertEquals(Collections.singletonList(true), listener.postDestroyReferences);
    }


    static class ActivityReferencingLifecycleListener implements Controller.LifecycleListener {
        final List<Boolean> changeEndReferences = new ArrayList<>();
        final List<Boolean> postCreateViewReferences = new ArrayList<>();
        final List<Boolean> postAttachReferences = new ArrayList<>();
        final List<Boolean> postDetachReferences = new ArrayList<>();
        final List<Boolean> postDestroyViewReferences = new ArrayList<>();
        final List<Boolean> postDestroyReferences = new ArrayList<>();

        @Override
        public void onChangeEnd(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
            changeEndReferences.add(controller.getActivity() != null);
        }

        @Override
        public void postCreateView(@NonNull Controller controller, @NonNull View view) {
            postCreateViewReferences.add(controller.getActivity() != null);
        }

        @Override
        public void postAttach(@NonNull Controller controller, @NonNull View view) {
            postAttachReferences.add(controller.getActivity() != null);
        }

        @Override
        public void postDetach(@NonNull Controller controller, @NonNull View view) {
            postDetachReferences.add(controller.getActivity() != null);
        }

        @Override
        public void postDestroyView(@NonNull Controller controller) {
            postDestroyViewReferences.add(controller.getActivity() != null);
        }

        @Override
        public void postDestroy(@NonNull Controller controller) {
            postDestroyReferences.add(controller.getActivity() != null);
        }
    }

}
