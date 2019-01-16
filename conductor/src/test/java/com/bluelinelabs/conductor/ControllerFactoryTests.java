package com.bluelinelabs.conductor;

import android.os.Bundle;
import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.TestController;
import com.bluelinelabs.conductor.util.TestControllerFactory;
import com.bluelinelabs.conductor.util.TestDependenciesController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ControllerFactoryTests {

    private Router router;

    private ActivityProxy activityProxy;

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
        Conductor.setControllerFactory(new TestControllerFactory());
        createActivityController(null, true);
    }

    @Test
    public void testControllerWithDependenciesRecreated() {
        Bundle args = new Bundle();
        args.putBoolean(TestDependenciesController.WAS_RECREATED_WITH_BUNDLE_ARGS, true);
        TestDependenciesController controller = new TestDependenciesController(args, false);
        router.pushController(RouterTransaction.with(controller)
                .tag("root"));
        activityProxy.getActivity().isChangingConfigurations = true;

        assertEquals(false, controller.wasCreatedByFactory);
        assertEquals(false, controller.wasInstanceStateRestored);

        Bundle bundle = new Bundle();
        activityProxy.saveInstanceState(bundle);
        activityProxy.pause();
        activityProxy.stop(true);
        activityProxy.destroy();

        createActivityController(bundle, false);
        controller = (TestDependenciesController)router.getControllerWithTag("root");
        assertEquals(true, controller.wasCreatedByFactory);
        assertEquals(true, controller.wasInstanceStateRestored);
        assertEquals(true, controller.wasRecreatedWithBundleArgs);
    }
}
