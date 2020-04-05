package com.bluelinelabs.conductor.demo.changehandler.listeners;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;

public class BackstackChangeListener implements ControllerChangeHandler.ControllerChangeListener {

    private final Runnable runnable;

    public BackstackChangeListener(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {

    }

    @Override
    public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        runnable.run();
    }
}
