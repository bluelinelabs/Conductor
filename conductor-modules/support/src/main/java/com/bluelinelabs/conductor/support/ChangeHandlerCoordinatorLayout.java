package com.bluelinelabs.conductor.support;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;

/**
 * A CoordinatorLayout implementation that can be used to block user interactions while
 * {@link ControllerChangeHandler}s are performing changes. It also propagates WindowInset changes
 * to its children to allow the creation of immersive layouts using fitsSystemWindows
 */
public class ChangeHandlerCoordinatorLayout extends CoordinatorLayout implements ControllerChangeListener {

    private int inProgressTransactionCount;

    public ChangeHandlerCoordinatorLayout(@NonNull Context context) {
        super(context);
    }

    public ChangeHandlerCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeHandlerCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (inProgressTransactionCount > 0) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        inProgressTransactionCount++;
    }

    @Override
    public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        inProgressTransactionCount--;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).dispatchApplyWindowInsets(insets);
        }

        return super.onApplyWindowInsets(insets);
    }
}
