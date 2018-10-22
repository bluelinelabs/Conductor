package com.bluelinelabs.conductor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.TestController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewLeakTests {

    private ActivityProxy activityProxy;
    private Router router;

    public void createActivityController(Bundle savedInstanceState) {
        activityProxy = new ActivityProxy().create(savedInstanceState).start().resume();
        router = Conductor.attachRouter(activityProxy.getActivity(), activityProxy.getView(), savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new TestController()));
        }
    }

    @Before
    public void setup() {
        createActivityController(null);
    }

    @Test
    public void testPop() {
        Controller controller = new TestController();
        router.pushController(RouterTransaction.with(controller));

        assertNotNull(controller.getView());

        router.popCurrentController();

        assertNull(controller.getView());
    }

    @Test
    public void testPopWhenPushNeverAdded() {
        Controller controller = new TestController();
        router.pushController(RouterTransaction.with(controller).pushChangeHandler(new NeverAddChangeHandler()));

        assertNotNull(controller.getView());

        router.popCurrentController();

        assertNull(controller.getView());
    }

    @Test
    public void testPopWhenPushNeverCompleted() {
        Controller controller = new TestController();
        router.pushController(RouterTransaction.with(controller).pushChangeHandler(new NeverCompleteChangeHandler()));

        assertNotNull(controller.getView());

        router.popCurrentController();

        assertNull(controller.getView());
    }

    @Test
    public void testActivityStop() {
        Controller controller = new TestController();
        router.pushController(RouterTransaction.with(controller));

        assertNotNull(controller.getView());

        activityProxy.stop(true);

        assertNull(controller.getView());
    }

    @Test
    public void testActivityStopWhenPushNeverCompleted() {
        Controller controller = new TestController();
        router.pushController(RouterTransaction.with(controller).pushChangeHandler(new NeverCompleteChangeHandler()));

        assertNotNull(controller.getView());

        activityProxy.stop(true);

        assertNull(controller.getView());
    }

    @Test
    public void testActivityDestroyWhenPushNeverAdded() {
        Controller controller = new TestController();
        router.pushController(RouterTransaction.with(controller).pushChangeHandler(new NeverAddChangeHandler()));

        assertNotNull(controller.getView());

        activityProxy.stop(true).destroy();

        assertNull(controller.getView());
    }

    public static class NeverAddChangeHandler extends ControllerChangeHandler {
        @Override
        public void performChange(@NonNull final ViewGroup container, @Nullable View from, @Nullable final View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
            if (from != null) {
                container.removeView(from);
            }
        }
    }

    public static class NeverCompleteChangeHandler extends ControllerChangeHandler {
        @Override
        public void performChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
            if (from != null) {
                container.removeView(from);
            }
            container.addView(to);
        }
    }

}
