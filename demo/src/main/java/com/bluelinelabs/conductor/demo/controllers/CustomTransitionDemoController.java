package com.bluelinelabs.conductor.demo.controllers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.FabToDialogAnimatorChangeHandler;
import com.bluelinelabs.conductor.demo.changehandler.FabToDialogTransitionChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;
import butterknife.OnClick;

import static com.bluelinelabs.conductor.demo.util.CustomTransitionCompatUtil.getTransitionCompat;

public class CustomTransitionDemoController extends BaseController {

    private static final String KEY_FAB_VISIBILITY = "CustomTransitionDemoController.fabVisibility";

    @BindView(R.id.fab) View fab;

    @Override
    protected View inflateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container) {
        return inflater.inflate(R.layout.controller_custom_transition, container, false);
    }

    @Override
    protected void onSaveViewState(@NonNull View view, @NonNull Bundle outState) {
        super.onSaveViewState(view, outState);
        outState.putInt(KEY_FAB_VISIBILITY, fab.getVisibility());
    }

    @Override
    protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {
        super.onRestoreViewState(view, savedViewState);

        //noinspection WrongConstant
        fab.setVisibility(savedViewState.getInt(KEY_FAB_VISIBILITY));
    }

    @OnClick(R.id.fab)
    public void showDialog() {
        getRouter()
                .pushController(RouterTransaction.with(new DialogController())
                        .pushChangeHandler(getTransitionCompat(new FabToDialogTransitionChangeHandler(), new FabToDialogAnimatorChangeHandler()))
                        .popChangeHandler(getTransitionCompat(new FabToDialogTransitionChangeHandler(), new FabToDialogAnimatorChangeHandler()))
                );
    }

}
