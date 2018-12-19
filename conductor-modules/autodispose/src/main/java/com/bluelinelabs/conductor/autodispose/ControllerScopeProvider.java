package com.bluelinelabs.conductor.autodispose;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.uber.autodispose.OutsideScopeException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.LifecycleScopes;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;

import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class ControllerScopeProvider implements LifecycleScopeProvider<ControllerEvent> {
    private static final CorrespondingEventsFunction<ControllerEvent> CORRESPONDING_EVENTS =
            new CorrespondingEventsFunction<ControllerEvent>() {
                @Override
                public ControllerEvent apply(ControllerEvent lastEvent) throws OutsideScopeException {
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
                            throw new OutsideScopeException("Cannot bind to Controller lifecycle when outside of it.");
                    }
                }
            };

    @NonNull private final BehaviorSubject<ControllerEvent> lifecycleSubject;
    @NonNull private final CorrespondingEventsFunction<ControllerEvent> correspondingEventsFunction;

    public static ControllerScopeProvider from(@NonNull Controller controller) {
        return new ControllerScopeProvider(controller, CORRESPONDING_EVENTS);
    }

    public static ControllerScopeProvider from(@NonNull Controller controller, @NonNull final ControllerEvent untilEvent) {
        return new ControllerScopeProvider(controller, new CorrespondingEventsFunction<ControllerEvent>() {
            @Override
            public ControllerEvent apply(ControllerEvent controllerEvent) {
                return untilEvent;
            }
        });
    }

    public static ControllerScopeProvider from(@NonNull Controller controller, @NonNull final CorrespondingEventsFunction<ControllerEvent> correspondingEventsFunction) {
        return new ControllerScopeProvider(controller, correspondingEventsFunction);
    }

    private ControllerScopeProvider(@NonNull Controller controller, @NonNull CorrespondingEventsFunction<ControllerEvent> correspondingEventsFunction) {
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(controller);
        this.correspondingEventsFunction = correspondingEventsFunction;
    }

    @Override
    public Observable<ControllerEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    public CorrespondingEventsFunction<ControllerEvent> correspondingEvents() {
        return correspondingEventsFunction;
    }

    @Override
    public ControllerEvent peekLifecycle() {
        return lifecycleSubject.getValue();
    }

    @Override
    public CompletableSource requestScope() throws Exception {
        return LifecycleScopes.resolveScopeFromLifecycle(this);
    }
}
