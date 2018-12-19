package com.bluelinelabs.conductor.archlifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.RestoreViewOnCreateController;

public abstract class LifecycleRestoreViewOnCreateController extends RestoreViewOnCreateController implements LifecycleOwner {

    private final ControllerLifecycleOwner mLifecycleOwner = new ControllerLifecycleOwner(this);

    public LifecycleRestoreViewOnCreateController() {
        super();
    }

    public LifecycleRestoreViewOnCreateController(@Nullable Bundle args) {
        super(args);
    }

    @Override @NonNull
    public Lifecycle getLifecycle() {
        return mLifecycleOwner.getLifecycle();
    }

}
