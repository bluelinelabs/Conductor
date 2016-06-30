package com.bluelinelabs.conductor.demo.controllers;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.ColorUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ParentController extends BaseController {

    private static final int NUMBER_OF_CHILDREN = 5;
    private static final int ANIMATION_DURATION_MS = 300;
    private Subscription sub;

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_parent, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        sub = Observable.interval(ANIMATION_DURATION_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).take(NUMBER_OF_CHILDREN).subscribe(new Action1<Long>() {
            @Override
            public void call(Long index) {
                addChild(index.intValue());
            }
        });
    }

    @Override
    protected void onDestroyView(View view) {
        super.onDestroyView(view);
        sub.unsubscribe();
        sub = null;
    }

    private void addChild(final int index) {
        @IdRes final int frameId = getResources().getIdentifier("child_content_" + (index + 1), "id", getActivity().getPackageName());
        final ViewGroup container = (ViewGroup) getView().findViewById(frameId);
        final Router childRouter = getChildRouter(container, null);

        if (!childRouter.hasRootController()) {
            ChildController childController = new ChildController("Child Controller #" + index, ColorUtil.getMaterialColor(getResources(), index), false);
            childRouter.setRoot(RouterTransaction.with(childController)
                    .pushChangeHandler(new FadeChangeHandler(ANIMATION_DURATION_MS)));
        }
    }

    @Override
    protected String getTitle() {
        return "Parent/Child Demo";
    }

}
