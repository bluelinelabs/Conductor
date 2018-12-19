package com.bluelinelabs.conductor.rxlifecycle2;

import android.os.Bundle;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bluelinelabs.conductor.Controller;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * A base {@link Controller} that can be used to expose lifecycle events using RxJava
 */
public abstract class RxController extends Controller implements LifecycleProvider<ControllerEvent> {
    private final BehaviorSubject<ControllerEvent> lifecycleSubject;

    public RxController(){
        this(null);
    }

    public RxController(@Nullable Bundle args) {
        super(args);
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(this);
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<ControllerEvent> lifecycle() {
        return lifecycleSubject.hide();
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
