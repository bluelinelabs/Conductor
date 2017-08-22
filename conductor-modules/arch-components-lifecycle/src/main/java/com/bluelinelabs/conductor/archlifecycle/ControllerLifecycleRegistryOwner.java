package com.bluelinelabs.conductor.archlifecycle;

import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.Lifecycle.State;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.support.annotation.NonNull;
import android.view.View;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Controller.LifecycleListener;

public class ControllerLifecycleRegistryOwner extends LifecycleListener implements LifecycleRegistryOwner {

    final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    public ControllerLifecycleRegistryOwner(Controller controller) {
        lifecycleRegistry.handleLifecycleEvent(Event.ON_CREATE);
        lifecycleRegistry.markState(State.CREATED);

        controller.addLifecycleListener(new LifecycleListener() {
            @Override
            public void preCreateView(@NonNull Controller controller) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_START);
            }

            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.markState(State.STARTED);
            }

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_RESUME);
            }

            @Override
            public void postAttach(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.markState(State.RESUMED);
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_PAUSE);
            }

            @Override
            public void postDetach(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.markState(State.STARTED);
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_STOP);
            }

            @Override
            public void postDestroyView(@NonNull Controller controller) {
                lifecycleRegistry.markState(State.CREATED);
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                lifecycleRegistry.handleLifecycleEvent(Event.ON_DESTROY);
            }

            @Override
            public void postDestroy(@NonNull Controller controller) {
                lifecycleRegistry.markState(State.DESTROYED);
            }
        });
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

}
