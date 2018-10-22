package com.bluelinelabs.conductor.demo.controllers;

import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.archlifecycle.LifecycleController;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.ActionBarProvider;
import com.bluelinelabs.conductor.demo.DemoApplication;
import com.bluelinelabs.conductor.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ArchLifecycleController extends LifecycleController {

    private static final String TAG = "ArchLifecycleController";

    @BindView(R.id.tv_title) TextView tvTitle;

    private Unbinder unbinder;
    private boolean hasExited;

    public ArchLifecycleController() {
        Log.i(TAG, "Conductor: Constructor called");

        getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Event.ON_ANY)
            void onLifecycleEvent(@NonNull LifecycleOwner source, @NonNull Event event) {
                Log.d(TAG, "Lifecycle: " + source.getClass().getSimpleName() + " emitted event " + event + " and is now in state " + source.getLifecycle().getCurrentState());
            }
        });

        Log.d(TAG, "Lifecycle: " + getClass().getSimpleName() + " is now in state " + getLifecycle().getCurrentState());
    }

    @Override
    protected void onContextAvailable(@NonNull Context context) {
        Log.i(TAG, "Conductor: onContextAvailable() called");
        super.onContextAvailable(context);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        Log.i(TAG, "Conductor: onCreateView() called");

        View view = inflater.inflate(R.layout.controller_lifecycle, container, false);
        view.setBackgroundColor(ContextCompat.getColor(container.getContext(), R.color.orange_300));
        unbinder = ButterKnife.bind(this, view);

        tvTitle.setText(getResources().getString(R.string.rxlifecycle_title, TAG));

        return view;
    }

    @Override
    protected void onAttach(@NonNull View view) {
        Log.i(TAG, "Conductor: onAttach() called");
        super.onAttach(view);

        (((ActionBarProvider) getActivity()).getSupportActionBar()).setTitle("Arch Components Lifecycle Demo");
    }

    @Override
    protected void onDetach(@NonNull View view) {
        Log.i(TAG, "Conductor: onDetach() called");
        super.onDetach(view);
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        Log.i(TAG, "Conductor: onDestroyView() called");
        super.onDestroyView(view);

        unbinder.unbind();
        unbinder = null;
    }

    @Override
    protected void onContextUnavailable() {
        Log.i(TAG, "Conductor: onContextUnavailable() called");
        super.onContextUnavailable();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Conductor: onDestroy() called");
        super.onDestroy();

        if (hasExited) {
            DemoApplication.refWatcher.watch(this);
        }
    }

    @Override
    protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeEnded(changeHandler, changeType);

        hasExited = !changeType.isEnter;
        if (isDestroyed()) {
            DemoApplication.refWatcher.watch(this);
        }
    }

    @OnClick(R.id.btn_next_release_view) void onNextWithReleaseClicked() {
        setRetainViewMode(RetainViewMode.RELEASE_DETACH);

        getRouter().pushController(RouterTransaction.with(new TextController("Logcat should now report that the Controller's onDetach() and LifecycleObserver's onPause() methods were called, followed by the Controller's onDestroyView() and LifecycleObserver's onStop()."))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
    }

    @OnClick(R.id.btn_next_retain_view) void onNextWithRetainClicked() {
        setRetainViewMode(RetainViewMode.RETAIN_DETACH);

        getRouter().pushController(RouterTransaction.with(new TextController("Logcat should now report that the Controller's onDetach() and LifecycleObserver's onPause() methods were called."))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
    }

}
