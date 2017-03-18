package com.bluelinelabs.conductor.transitionseverywhere;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

/**
 * A base {@link ControllerChangeHandler} that facilitates using {@link Transition}s to replace Controller Views.
 * This is a copy of {@link com.bluelinelabs.conductor.changehandler.TransitionChangeHandler}
 * which uses <a href="https://github.com/andkulikov/Transitions-Everywhere">Transitions-Everywhere</a>
 * transitions instead of native Transitions to support pre-Lollipop devices.
 */
public abstract class TransitionsEverywhereChangeHandler extends ControllerChangeHandler {

    public interface OnTransitionPreparedListener {
        void onPrepared();
    }

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

    @Override
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, @Nullable Controller newTop) {
        super.onAbortPush(newHandler, newTop);

        canceled = true;
    }

    @Override
    public void performChange(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, final boolean isPush, @NonNull final ControllerChangeCompletedListener changeListener) {
        if (canceled) {
            changeListener.onChangeCompleted();
            return;
        }

        final Transition transition = getTransition(container, from, to, isPush);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) { }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                changeListener.onChangeCompleted();
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
                changeListener.onChangeCompleted();
            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) { }

            @Override
            public void onTransitionResume(@NonNull Transition transition) { }
        });

        prepareForTransition(container, from, to, transition, isPush, new OnTransitionPreparedListener() {
            @Override
            public void onPrepared() {
                if (!canceled) {
                    TransitionManager.beginDelayedTransition(container, transition);
                    executePropertyChanges(container, from, to, transition, isPush);
                }
            }
        });
    }

    @Override
    public boolean removesFromViewOnPush() {
        return true;
    }

    /**
     * Called before a transition occurs. This can be used to reorder views, set their transition names, etc. The transition will begin
     * when {@code onTransitionPreparedListener} is called.
     *
     * @param container  The container these Views are hosted in
     * @param from       The previous View in the container or {@code null} if there was no Controller before this transition
     * @param to         The next View that should be put in the container or {@code null} if no Controller is being transitioned to
     * @param transition The transition that is being prepared for
     * @param isPush     True if this is a push transaction, false if it's a pop
     */
    public void prepareForTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, @NonNull Transition transition, boolean isPush, @NonNull OnTransitionPreparedListener onTransitionPreparedListener) {
        onTransitionPreparedListener.onPrepared();
    }

    /**
     * This should set all view properties needed for the transition to work properly. By default it removes the "from" view
     * and adds the "to" view.
     *
     * @param container  The container these Views are hosted in
     * @param from       The previous View in the container or {@code null} if there was no Controller before this transition
     * @param to         The next View that should be put in the container or {@code null} if no Controller is being transitioned to
     * @param transition The transition with which {@code TransitionManager.beginDelayedTransition} has been called
     * @param isPush     True if this is a push transaction, false if it's a pop
     */
    public void executePropertyChanges(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, @NonNull Transition transition, boolean isPush) {
        if (from != null && (removesFromViewOnPush() || !isPush) && from.getParent() == container) {
            container.removeView(from);
        }
        if (to != null && to.getParent() == null) {
            container.addView(to);
        }
    }

}
