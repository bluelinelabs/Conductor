package com.bluelinelabs.conductor;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.rule.ActivityTestRule;
import com.bluelinelabs.conductor.util.EmptyTestController;
import com.bluelinelabs.conductor.util.TestActivity;
import com.bluelinelabs.conductor.util.TestTransitionChangeHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransitionChangeHandlerTests {

    @Rule
    public ActivityTestRule<TestActivity> activityTestRule = new ActivityTestRule<>(TestActivity.class);
    private Router router;
    private int runningTransactions = 0;

    @Before
    public void setup() {
        router = activityTestRule.getActivity().router;
        router.addChangeListener(new ControllerChangeHandler.ControllerChangeListener() {
            @Override
            public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
                runningTransactions++;
            }

            @Override
            public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
                runningTransactions--;
            }
        });
    }

    @Test
    public void testPushPop() throws Throwable {
        final EmptyTestController rootController = new EmptyTestController();
        IdlingRegistry.getInstance().register(idlingResourceUntilControllerAttachedDelayed(rootController));
        activityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                router.setRoot(RouterTransaction.with(rootController));

                router.pushController(RouterTransaction.with(new EmptyTestController())
                        .pushChangeHandler(new TestTransitionChangeHandler())
                        .popChangeHandler(new TestTransitionChangeHandler()));
                router.popCurrentController();
            }
        });

        Espresso.onIdle();
        assertEquals(0, runningTransactions);
    }

    @Test
    public void testDoublePush() throws Throwable {
        final EmptyTestController topController = new EmptyTestController();
        IdlingRegistry.getInstance().register(idlingResourceUntilControllerAttachedDelayed(topController));
        activityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                router.setRoot(RouterTransaction.with(new EmptyTestController()));

                router.pushController(RouterTransaction.with(new EmptyTestController())
                        .pushChangeHandler(new TestTransitionChangeHandler())
                        .popChangeHandler(new TestTransitionChangeHandler()));
                router.pushController(RouterTransaction.with(topController)
                        .pushChangeHandler(new TestTransitionChangeHandler())
                        .popChangeHandler(new TestTransitionChangeHandler()));
            }
        });

        Espresso.onIdle();
        assertEquals(0, runningTransactions);
    }

    private IdlingResource idlingResourceUntilControllerAttachedDelayed(Controller controller) {
        final CountingIdlingResource idlingResource = new CountingIdlingResource("attached_" + controller.instanceId);
        idlingResource.increment();
        controller.addLifecycleListener(new Controller.LifecycleListener() {
            @Override
            public void postAttach(@NonNull final Controller controller, @NonNull View view) {
                controller.removeLifecycleListener(this);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        idlingResource.decrement();
                    }
                }, 3000L);
            }
        });

        return idlingResource;
    }

}
