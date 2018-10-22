package com.bluelinelabs.conductor.changehandler;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.internal.ClassUtils;

/**
 * A base {@link ControllerChangeHandler} that facilitates using {@link android.transition.Transition}s to replace Controller Views.
 * If the target device is running on a version of Android that doesn't support transitions, a fallback {@link ControllerChangeHandler} will be used.
 */
public class TransitionChangeHandlerCompat extends ControllerChangeHandler {

    private static final String KEY_CHANGE_HANDLER_CLASS = "TransitionChangeHandlerCompat.changeHandler.class";
    private static final String KEY_HANDLER_STATE = "TransitionChangeHandlerCompat.changeHandler.state";

    private ControllerChangeHandler changeHandler;

    public TransitionChangeHandlerCompat() { }

    /**
     * Constructor that takes a {@link TransitionChangeHandler} for use with compatible devices, as well as a fallback
     * {@link ControllerChangeHandler} for use with older devices.
     *
     * @param transitionChangeHandler The change handler that will be used on API 21 and above
     * @param fallbackChangeHandler The change handler that will be used on APIs below 21
     */
    public TransitionChangeHandlerCompat(@NonNull TransitionChangeHandler transitionChangeHandler, @NonNull ControllerChangeHandler fallbackChangeHandler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            changeHandler = transitionChangeHandler;
        } else {
            changeHandler = fallbackChangeHandler;
        }
    }

    @Override
    public void performChange(@NonNull final ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull final ControllerChangeCompletedListener changeListener) {
        changeHandler.performChange(container, from, to, isPush, changeListener);
    }

    @Override
    public void saveToBundle(@NonNull Bundle bundle) {
        super.saveToBundle(bundle);

        bundle.putString(KEY_CHANGE_HANDLER_CLASS, changeHandler.getClass().getName());

        Bundle stateBundle = new Bundle();
        changeHandler.saveToBundle(stateBundle);
        bundle.putBundle(KEY_HANDLER_STATE, stateBundle);
    }

    @Override
    public void restoreFromBundle(@NonNull Bundle bundle) {
        super.restoreFromBundle(bundle);

        String className = bundle.getString(KEY_CHANGE_HANDLER_CLASS);
        changeHandler = ClassUtils.newInstance(className);
        //noinspection ConstantConditions
        changeHandler.restoreFromBundle(bundle.getBundle(KEY_HANDLER_STATE));
    }

    @Override
    public boolean removesFromViewOnPush() {
        return changeHandler.removesFromViewOnPush();
    }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new TransitionChangeHandlerCompat((TransitionChangeHandler)changeHandler.copy(), null);
        } else {
            return new TransitionChangeHandlerCompat(null, changeHandler.copy());
        }
    }

    @Override
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, @Nullable Controller newTop) {
        changeHandler.onAbortPush(newHandler, newTop);
    }

    @Override
    public void completeImmediately() {
        changeHandler.completeImmediately();
    }

    @Override
    public void setForceRemoveViewOnPush(boolean force) {
        changeHandler.setForceRemoveViewOnPush(force);
    }

}
