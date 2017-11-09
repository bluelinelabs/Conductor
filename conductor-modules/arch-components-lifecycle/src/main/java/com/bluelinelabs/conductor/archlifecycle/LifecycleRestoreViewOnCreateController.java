package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.support.annotation.NonNull;

import com.bluelinelabs.conductor.RestoreViewOnCreateController;

public abstract class LifecycleRestoreViewOnCreateController extends RestoreViewOnCreateController implements LifecycleOwner {

    private final ControllerLifecycleRegistryOwner lifecycleRegistryOwner = new ControllerLifecycleRegistryOwner(this);

    @Override
    @NonNull
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistryOwner.getLifecycle();
    }

}
