package com.bluelinelabs.conductor.autodispose;

import android.support.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.uber.autodispose.LifecycleScopeProvider;
import com.uber.autodispose.OutsideLifecycleException;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

public class ControllerScopeProvider implements LifecycleScopeProvider<ControllerEvent> {
    private static final Function<ControllerEvent, ControllerEvent> CORRESPONDING_EVENTS =
            new Function<ControllerEvent, ControllerEvent>() {
                @Override
                public ControllerEvent apply(ControllerEvent lastEvent) throws Exception {
                    switch (lastEvent) {
                        case CREATE:
                            return ControllerEvent.DESTROY;
                        case CONTEXT_AVAILABLE:
                            return ControllerEvent.CONTEXT_UNAVAILABLE;
                        case CREATE_VIEW:
                            return ControllerEvent.DESTROY_VIEW;
                        case ATTACH:
                            return ControllerEvent.DETACH;
                        case DETACH:
                            return ControllerEvent.DESTROY;
                        default:
                            throw new OutsideLifecycleException("Cannot bind to Controller lifecycle when outside of it.");
                    }
                }
            };

    @NonNull private final BehaviorSubject<ControllerEvent> lifecycleSubject;

    public static ControllerScopeProvider from(@NonNull Controller controller) {
        return new ControllerScopeProvider(controller);
    }

    private ControllerScopeProvider(@NonNull Controller controller) {
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(controller);
    }

    @Override
    public Observable<ControllerEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    public Function<ControllerEvent, ControllerEvent> correspondingEvents() {
        return CORRESPONDING_EVENTS;
    }

    @Override
    public ControllerEvent peekLifecycle() {
        return lifecycleSubject.getValue();
    }
}
