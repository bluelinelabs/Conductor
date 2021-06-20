package com.bluelinelabs.conductor.util;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;

public class EmptyChangeListener implements ControllerChangeListener {
    @Override
    public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull
        ViewGroup container, @NonNull ControllerChangeHandler handler) {
    }

    @Override
    public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
    }
}
