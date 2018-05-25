package com.bluelinelabs.conductor.demo.controllers.viewmodel;

import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.viewmodel.ViewModelController;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.ActionBarProvider;
import com.bluelinelabs.conductor.demo.DemoApplication;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.TextController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ViewModelDemoController extends ViewModelController {

    private static final String TAG = "ViewModelDemoController";

    @BindView(R.id.tv_title)
    TextView tvTitle;

    private Unbinder unbinder;
    private boolean hasExited;

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        Log.i(TAG, "onCreateView() called");

        View view = inflater.inflate(R.layout.controller_lifecycle, container, false);
        view.setBackgroundColor(ContextCompat.getColor(container.getContext(), R.color.green_300));
        unbinder = ButterKnife.bind(this, view);

        tvTitle.setText(getResources().getString(R.string.rxlifecycle_title, TAG));

        ViewModelDemo viewModelDemo = viewModelProvider().get(ViewModelDemo.class);
        viewModelDemo.getLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                tvTitle.setText(getResources().getString(R.string.viewmodel_title, s));
            }
        });

        return view;
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);

        Log.i(TAG, "onAttach() called");

        (((ActionBarProvider) getActivity()).getSupportActionBar()).setTitle("Arch Components ViewModel Demo");
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        unbinder.unbind();
        super.onDestroyView(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy() called");

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
