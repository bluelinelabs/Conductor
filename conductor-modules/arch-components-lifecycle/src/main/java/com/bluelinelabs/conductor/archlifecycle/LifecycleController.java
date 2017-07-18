package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.os.Bundle;

import com.bluelinelabs.conductor.Controller;

public abstract class LifecycleController extends Controller implements LifecycleRegistryOwner {

    private final ControllerLifecycleRegistryOwner lifecycleRegistryOwner;

    public LifecycleController() {
        this(null);
    }

    public LifecycleController(Bundle args) {
        super(args);
        lifecycleRegistryOwner = new ControllerLifecycleRegistryOwner(this);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistryOwner.getLifecycle();
    }

}
