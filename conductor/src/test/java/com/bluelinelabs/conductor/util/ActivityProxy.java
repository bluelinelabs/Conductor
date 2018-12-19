package com.bluelinelabs.conductor.util;

import android.os.Bundle;
import androidx.annotation.IdRes;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class ActivityProxy {

    private ActivityController<TestActivity> activityController;
    private AttachFakingFrameLayout view;

    public ActivityProxy() {
        activityController = Robolectric.buildActivity(TestActivity.class);

        @IdRes int containerId = 4;
        view = new AttachFakingFrameLayout(activityController.get());
        view.setId(containerId);
    }

    public ActivityProxy create(Bundle savedInstanceState) {
        activityController.create(savedInstanceState);
        return this;
    }

    public ActivityProxy start() {
        activityController.start();
        view.setAttached(true);
        return this;
    }

    public ActivityProxy resume() {
        activityController.resume();
        return this;
    }

    public ActivityProxy pause() {
        activityController.pause();
        return this;
    }

    public ActivityProxy saveInstanceState(Bundle outState) {
        activityController.saveInstanceState(outState);
        return this;
    }

    public ActivityProxy stop(boolean detachView) {
        activityController.stop();

        if (detachView) {
            view.setAttached(false);
        }

        return this;
    }

    public ActivityProxy destroy() {
        activityController.destroy();
        view.setAttached(false);
        return this;
    }

    public ActivityProxy rotate() {
        getActivity().isChangingConfigurations = true;
        getActivity().recreate();
        return this;
    }

    public TestActivity getActivity() {
        return activityController.get();
    }

    public AttachFakingFrameLayout getView() {
        return view;
    }
}
