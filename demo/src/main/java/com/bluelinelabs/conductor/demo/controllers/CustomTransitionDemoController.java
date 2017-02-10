package com.bluelinelabs.conductor.demo.controllers;


import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bluelinelabs.conductor.ChangeHandlerFrameLayout;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.FabToDialogAnimatorChangeHandler;
import com.bluelinelabs.conductor.demo.changehandler.FabToDialogTransitionChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;
import butterknife.OnClick;

import static com.bluelinelabs.conductor.demo.util.CustomTransitionCompatUtil.getTransitionCompat;

public class CustomTransitionDemoController extends BaseController {

    @BindView(R.id.custom_transition_container)
    ChangeHandlerFrameLayout frameLayout;
    @BindView(R.id.fab)
    ImageButton fab;

    @Override
    protected View inflateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container) {
        return inflater.inflate(R.layout.controller_custom_transition, container, false);
    }

    @Override
    protected void onAttach(@NonNull final View view) {
        super.onAttach(view);
        //dialog is visible
        if (childRouter().getBackstackSize() > 0) {
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.fab)
    public void showDialog() {
        childRouter()
                .pushController(RouterTransaction.with(new DialogController())
                        .pushChangeHandler(getTransitionCompat(new FabToDialogTransitionChangeHandler(), new FabToDialogAnimatorChangeHandler()))
                        .popChangeHandler(getTransitionCompat(new FabToDialogTransitionChangeHandler(), new FabToDialogAnimatorChangeHandler()))
                );
    }

    @Override
    public boolean handleBack() {
        if (!childRouter().getBackstack().isEmpty()) {
            childRouter().popCurrentController();
            return true;
        } else {
            return super.handleBack();
        }
    }

    @NonNull
    private Router childRouter() {
        return getChildRouter(frameLayout).setPopsLastView(true);
    }
}
