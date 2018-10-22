package com.bluelinelabs.conductor.rxlifecycle;

import android.os.Bundle;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * A base {@link Controller} that can be used to expose lifecycle events using RxJava
 */
public abstract class RxController extends Controller implements LifecycleProvider<ControllerEvent> {

    private final BehaviorSubject<ControllerEvent> lifecycleSubject;

    public RxController() {
        this(null);
    }

    public RxController(Bundle args) {
        super(args);
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(this);
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<ControllerEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ControllerEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxControllerLifecycle.bindController(lifecycleSubject);
    }

}
