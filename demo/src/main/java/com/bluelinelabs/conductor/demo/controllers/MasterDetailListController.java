package com.bluelinelabs.conductor.demo.controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.listeners.BackstackChangeListener;
import com.bluelinelabs.conductor.demo.controllers.base.BaseMasterDetailController;

import butterknife.BindView;

public class MasterDetailListController extends BaseMasterDetailController {

    @Nullable @BindView(R.id.tv_detail_empty) TextView tvDetailEmpty;

    private BackstackChangeListener changeListener = new BackstackChangeListener(this::onDetailChange);

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_master_details, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        initRouters(view, R.id.master_container, R.id.detail_container);
        if (!getMasterRouter().hasRootController()) {
            getMasterRouter().setRoot(RouterTransaction.with(new ListController(1)));
        }
        if (isTwoPanesMode()) {
            getDetailRouter().setPopsLastView(true);
            getDetailRouter().addChangeListener(changeListener);
            onDetailChange();
        }
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (isTwoPanesMode()) {
            getDetailRouter().removeChangeListener(changeListener);
        }
        super.onDestroyView(view);
    }

    private void onDetailChange() {
        if (tvDetailEmpty != null) {
            tvDetailEmpty.setVisibility(!getDetailRouter().hasRootController() ? View.VISIBLE : View.GONE);
        }
    }
}
