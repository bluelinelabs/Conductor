package com.bluelinelabs.conductor.changehandler;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;

/**
 * A {@link ControllerChangeHandler} that will instantly swap Views with no animations or transitions.
 */
public class SimpleSwapChangeHandler extends ControllerChangeHandler implements OnAttachStateChangeListener {

    private static final String KEY_REMOVES_FROM_ON_PUSH = "SimpleSwapChangeHandler.removesFromViewOnPush";

    private boolean removesFromViewOnPush;

    private boolean canceled;

    private ViewGroup container;
    private ControllerChangeCompletedListener changeListener;

    public SimpleSwapChangeHandler() {
        this(true);
    }

    public SimpleSwapChangeHandler(boolean removesFromViewOnPush) {
        this.removesFromViewOnPush = removesFromViewOnPush;
    }

    @Override
    public void saveToBundle(@NonNull Bundle bundle) {
        super.saveToBundle(bundle);
        bundle.putBoolean(KEY_REMOVES_FROM_ON_PUSH, removesFromViewOnPush);
    }

    @Override
    public void restoreFromBundle(@NonNull Bundle bundle) {
        super.restoreFromBundle(bundle);
        removesFromViewOnPush = bundle.getBoolean(KEY_REMOVES_FROM_ON_PUSH);
    }

    @Override
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, @Nullable Controller newTop) {
        super.onAbortPush(newHandler, newTop);

        canceled = true;
    }

    @Override
    public void completeImmediately() {
        if (changeListener != null) {
            changeListener.onChangeCompleted();
            changeListener = null;

            container.removeOnAttachStateChangeListener(this);
            container = null;
        }
    }

    @Override
    public void performChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
        if (!canceled) {
            if (from != null && (!isPush || removesFromViewOnPush)) {
                container.removeView(from);
            }

            if (to != null && to.getParent() == null) {
                container.addView(to);
            }
        }

        if (container.getWindowToken() != null) {
            changeListener.onChangeCompleted();
        } else {
            this.changeListener = changeListener;
            this.container = container;
            container.addOnAttachStateChangeListener(this);
        }

    }

    @Override
    public boolean removesFromViewOnPush() {
        return removesFromViewOnPush;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull View v) {
        v.removeOnAttachStateChangeListener(this);

        if (changeListener != null) {
            changeListener.onChangeCompleted();
            changeListener = null;
            container = null;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull View v) { }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        return new SimpleSwapChangeHandler(removesFromViewOnPush());
    }

    @Override
    public boolean isReusable() {
        return true;
    }
}
