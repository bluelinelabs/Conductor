package com.bluelinelabs.conductor.demo.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.Bind;
import butterknife.OnClick;

public class TargetTitleEntryController extends BaseController {

    public interface TargetTitleEntryControllerListener {
        void onTitlePicked(String option);
    }

    @Bind(R.id.edit_text) EditText mEditText;

    public <T extends Controller & TargetTitleEntryControllerListener> TargetTitleEntryController(T targetController) {
        setTargetController(targetController);
    }

    public TargetTitleEntryController() {
    }

    @Override
    protected void onDetach(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_target_title_entry, container, false);
    }

    @Override
    protected String getTitle() {
        return "Target Controller Demo";
    }

    @OnClick(R.id.btn_use_title) void optionPicked() {
        Controller targetController = getTargetController();
        if (targetController != null) {
            ((TargetTitleEntryControllerListener)targetController).onTitlePicked(mEditText.getText().toString());
            getRouter().popController(this);
        }
    }
}
