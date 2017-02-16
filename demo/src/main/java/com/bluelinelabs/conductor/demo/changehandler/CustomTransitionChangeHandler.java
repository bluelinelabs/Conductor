package com.bluelinelabs.conductor.demo.changehandler;


import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;

/**
 * A base [ControllerChangeHandler] that facilitates using [android.transition.Transition]s to replace Controller Views.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
abstract class CustomTransitionChangeHandler extends ControllerChangeHandler {

    private boolean canceled;

    /**
     * Should be overridden to return the Transition to use while replacing Views.
     *
     * @param container The container these Views are hosted in
     * @param from      The previous View in the container or {@code null} if there was no Controller before this transition
     * @param to        The next View that should be put in the container or {@code null} if no Controller is being transitioned to
     * @param isPush    True if this is a push transaction, false if it's a pop
     */
    @NonNull
    protected abstract Transition getTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush);

    /**
     * Should be overridden to implement the change in the view hierarchy
     *
     * @param container The container these Views are hosted in
     * @param from      The previous View in the container or {@code null} if there was no Controller before this transition
     * @param to        The next View that should be put in the container or {@code null} if no Controller is being transitioned to
     * @param isPush    True if this is a push transaction, false if it's a pop
     */
    protected abstract void viewChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, @NonNull Transition transition, boolean isPush);

    @Override
    public void onAbortPush(@NonNull final ControllerChangeHandler newHandler, @Nullable final Controller newTop) {
        super.onAbortPush(newHandler, newTop);
        canceled = true;
    }

    @Override
    public void performChange(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, boolean isPush, @NonNull final ControllerChangeCompletedListener changeListener) {
        if (canceled) {
            changeListener.onChangeCompleted();
            return;
        }

        Transition transition = getTransition(container, from, to, isPush);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                changeListener.onChangeCompleted();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                changeListener.onChangeCompleted();
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        viewChange(container, from, to, transition, isPush);
    }
}
