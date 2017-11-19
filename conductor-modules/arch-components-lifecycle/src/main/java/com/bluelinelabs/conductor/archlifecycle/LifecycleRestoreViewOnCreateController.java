package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.conductor.RestoreViewOnCreateController;

public abstract class LifecycleRestoreViewOnCreateController extends RestoreViewOnCreateController implements LifecycleOwner {

    private final ControllerLifecycleOwner mLifecycleOwner = new ControllerLifecycleOwner(this);

    public LifecycleRestoreViewOnCreateController() {
        super();
    }

    public LifecycleRestoreViewOnCreateController(@Nullable Bundle args) {
        super(args);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleOwner.getLifecycle();
    }

}
