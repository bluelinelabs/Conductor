package com.bluelinelabs.conductor.demo.controllers;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.OnClick;

public class DialogController extends BaseController {


    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_dialog, container, false);
    }

    @OnClick({R.id.dismiss, R.id.dialog_window})
    public void dimissDialog() {
        final Activity activity = getActivity();
        if (activity != null) getActivity().onBackPressed();
    }

    @Override
    public boolean handleBack() {
        return super.handleBack();
    }
}
