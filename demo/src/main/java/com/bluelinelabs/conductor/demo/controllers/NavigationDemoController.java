package com.bluelinelabs.conductor.demo.controllers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;
import com.bluelinelabs.conductor.demo.util.ColorUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class NavigationDemoController extends BaseController {

    public enum DisplayUpMode {
        SHOW,
        SHOW_FOR_CHILDREN_ONLY,
        HIDE;

        private DisplayUpMode getDisplayUpModeForChild() {
            switch (this) {
                case HIDE:
                    return HIDE;
                default:
                    return SHOW;
            }
        }
    }

    public static final String TAG_UP_TRANSACTION = "NavigationDemoController.up";

    private static final String KEY_INDEX = "NavigationDemoController.index";
    private static final String KEY_DISPLAY_UP_MODE = "NavigationDemoController.displayUpMode";

    @BindView(R.id.tv_title) TextView tvTitle;

    private int index;
    private DisplayUpMode displayUpMode;

    public NavigationDemoController(int index, DisplayUpMode displayUpMode) {
        this(new BundleBuilder(new Bundle())
                .putInt(KEY_INDEX, index)
                .putInt(KEY_DISPLAY_UP_MODE, displayUpMode.ordinal())
                .build());
    }

    public NavigationDemoController(Bundle args) {
        super(args);
        index = args.getInt(KEY_INDEX);
        displayUpMode = DisplayUpMode.values()[args.getInt(KEY_DISPLAY_UP_MODE)];
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_navigation_demo, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        if (displayUpMode != DisplayUpMode.SHOW) {
            view.findViewById(R.id.btn_up).setVisibility(View.GONE);
        }

        view.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));
        tvTitle.setText(getResources().getString(R.string.navigation_title, index));
    }

    @Override
    protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeEnded(changeHandler, changeType);

        setButtonsEnabled(true);
    }

    @Override
    protected void onChangeStarted(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeStarted(changeHandler, changeType);

        setButtonsEnabled(false);
    }

    @Override
    protected String getTitle() {
        return "Navigation Demos";
    }

    private void setButtonsEnabled(boolean enabled) {
        final View view = getView();
        if (view != null) {
            view.findViewById(R.id.btn_next).setEnabled(enabled);
            view.findViewById(R.id.btn_up).setEnabled(enabled);
            view.findViewById(R.id.btn_pop_to_root).setEnabled(enabled);
        }
    }

    @OnClick(R.id.btn_next) void onNextClicked() {
        getRouter().pushController(RouterTransaction.with(new NavigationDemoController(index + 1, displayUpMode.getDisplayUpModeForChild()))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
    }

    @OnClick(R.id.btn_up) void onUpClicked() {
        getRouter().popToTag(TAG_UP_TRANSACTION);
    }

    @OnClick(R.id.btn_pop_to_root) void onPopToRootClicked() {
        getRouter().popToRoot();
    }
}
