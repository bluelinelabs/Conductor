package com.bluelinelabs.conductor.archlifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.Controller;

public abstract class LifecycleController extends Controller implements LifecycleOwner {

    private final ControllerLifecycleOwner lifecycleOwner = new ControllerLifecycleOwner(this);

    public LifecycleController() {
        super();
    }

    public LifecycleController(@Nullable Bundle args) {
        super(args);
    }

    @Override @NonNull
    public Lifecycle getLifecycle() {
        return lifecycleOwner.getLifecycle();
    }

}
