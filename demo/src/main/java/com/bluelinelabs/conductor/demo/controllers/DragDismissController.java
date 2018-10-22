package com.bluelinelabs.conductor.demo.controllers;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.ScaleFadeChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.widget.ElasticDragDismissFrameLayout;
import com.bluelinelabs.conductor.demo.widget.ElasticDragDismissFrameLayout.ElasticDragDismissCallback;

@TargetApi(VERSION_CODES.LOLLIPOP)
public class DragDismissController extends BaseController {

    private final ElasticDragDismissCallback dragDismissListener = new ElasticDragDismissCallback() {
        @Override
        public void onDragDismissed() {
            overridePopHandler(new ScaleFadeChangeHandler());
            getRouter().popController(DragDismissController.this);
        }
    };

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_drag_dismiss, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        ((ElasticDragDismissFrameLayout)view).addListener(dragDismissListener);
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);

        ((ElasticDragDismissFrameLayout)view).removeListener(dragDismissListener);
    }

    @Override
    protected String getTitle() {
        return "Drag to Dismiss";
    }
}
