package com.bluelinelabs.conductor.demo.controllers;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.rxlifecycle.ControllerEvent;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

// Shamelessly borrowed from the official RxLifecycle demo by Trello and adapted for Conductor Controllers
// instead of Activities or Fragments.
public class RxLifecycleController extends BaseController {

    private static final String TAG = "RxLifecycleController";

    @BindView(R.id.tv_title) TextView tvTitle;

    public RxLifecycleController() {

        Observable.interval(1, TimeUnit.SECONDS)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "Unsubscribing from constructor");
                    }
                })
                .compose(this.<Long>bindUntilEvent(ControllerEvent.DESTROY))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long num) {
                        Log.i(TAG, "Started in constructor, running until onDestroy(): " + num);
                    }
                });
    }

    @Override
    public void onViewBound(@NonNull View view) {
        Log.i(TAG, "onViewBound() called");

        tvTitle.setText(getResources().getString(R.string.rxlifecycle_title, TAG));

        Observable.interval(1, TimeUnit.SECONDS)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "Unsubscribing from onViewBound)");
                    }
                })
                .compose(this.<Long>bindUntilEvent(ControllerEvent.DESTROY_VIEW))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long num) {
                        Log.i(TAG, "Started in onViewBound(), running until onDestroyView(): " + num);
                    }
                });
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);

        Log.i(TAG, "onAttach() called");

        Observable.interval(1, TimeUnit.SECONDS)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "Unsubscribing from onAttach()");
                    }
                })
                .compose(this.<Long>bindUntilEvent(ControllerEvent.DETACH))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long num) {
                        Log.i(TAG, "Started in onAttach(), running until onDetach(): " + num);
                    }
                });
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);

        Log.i(TAG, "onDestroyView() called");
    }

    @Override
    protected void onDetach(@NonNull View view) {
        super.onDetach(view);

        Log.i(TAG, "onDetach() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy() called");
    }

    @Override
    protected String getTitle() {
        return "RxLifecycle Demo";
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_rxlifecycle, container, false);
    }

    @OnClick(R.id.btn_next_release_view) void onNextWithReleaseClicked() {
        setRetainViewMode(RetainViewMode.RELEASE_DETACH);

        getRouter().pushController(RouterTransaction.with(new TextController("Logcat should now report that the observables from onAttach() and onViewBound() have been unsubscribed from, while the constructor observable is still running."))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
    }

    @OnClick(R.id.btn_next_retain_view) void onNextWithRetainClicked() {
        setRetainViewMode(RetainViewMode.RETAIN_DETACH);

        getRouter().pushController(RouterTransaction.with(new TextController("Logcat should now report that the observables from onAttach() has been unsubscribed from, while the constructor and onViewBound() observables are still running."))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
    }
}
