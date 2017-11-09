package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.conductor.Controller;

public abstract class LifecycleController extends Controller implements LifecycleOwner {

    private final ControllerLifecycleRegistryOwner lifecycleRegistryOwner = new ControllerLifecycleRegistryOwner(this);

    public LifecycleController() {
        super();
    }

    public LifecycleController(@Nullable Bundle args) {
        super(args);
    }

    @Override
    @NonNull
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistryOwner.getLifecycle();
    }

}
