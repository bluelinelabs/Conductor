package com.bluelinelabs.conductor;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.internal.LifecycleHandler;
import com.bluelinelabs.conductor.internal.ThreadUtils;

/**
 * Point of initial interaction with Conductor. Used to attach a {@link Router} to your Activity.
 */
public final class Conductor {

    private Conductor() {}

    /**
     * Conductor will create a {@link Router} that has been initialized for your Activity and containing ViewGroup.
     * If an existing {@link Router} is already associated with this Activity/ViewGroup pair, either in memory
     * or in the savedInstanceState, that router will be used and rebound instead of creating a new one with
     * an empty backstack.
     *
     * @param activity The Activity that will host the {@link Router} being attached.
     * @param container The ViewGroup in which the {@link Router}'s {@link Controller} views will be hosted
     * @param savedInstanceState The savedInstanceState passed into the hosting Activity's onCreate method. Used
     *                           for restoring the Router's state if possible.
     * @return A fully configured {@link Router} instance for use with this Activity/ViewGroup pair.
     */
    @NonNull @UiThread
    public static Router attachRouter(@NonNull Activity activity, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        ThreadUtils.ensureMainThread();

        LifecycleHandler lifecycleHandler = LifecycleHandler.install(activity);

        Router router = lifecycleHandler.getRouter(container, savedInstanceState);
        router.rebindIfNeeded();

        return router;
    }

}
