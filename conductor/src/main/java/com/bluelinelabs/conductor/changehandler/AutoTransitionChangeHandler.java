package com.bluelinelabs.conductor.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;

/**
 * A change handler that will use an AutoTransition.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AutoTransitionChangeHandler extends TransitionChangeHandler {

    @Override @NonNull
    protected Transition getTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        return new AutoTransition();
    }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        return new AutoTransitionChangeHandler();
    }

}
