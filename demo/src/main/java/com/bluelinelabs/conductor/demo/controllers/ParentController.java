package com.bluelinelabs.conductor.demo.controllers;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.ColorUtil;

public class ParentController extends BaseController {

    private static final int NUMBER_OF_CHILDREN = 5;
    private boolean finishing;

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_parent, container, false);
    }

    @Override
    protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeEnded(changeHandler, changeType);

        if (changeType == ControllerChangeType.PUSH_ENTER) {
            addChild(0);
        }
    }

    private void addChild(final int index) {
        if (finishing) {
            return;
        }
        @IdRes final int frameId = getResources().getIdentifier("child_content_" + (index + 1), "id", getActivity().getPackageName());
        final ViewGroup container = (ViewGroup) getView().findViewById(frameId);
        final Router childRouter = getChildRouter(container, null).setPopsLastView(true);

        if (!childRouter.hasRootController()) {
            ChildController childController = new ChildController("Child Controller #" + index, ColorUtil.getMaterialColor(getResources(), index), false);

            childController.addLifecycleListener(new LifecycleListener() {
                @Override
                public void onChangeEnd(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
                    if (changeType == ControllerChangeType.PUSH_ENTER && index < NUMBER_OF_CHILDREN - 1) {
                        addChild(index + 1);
                    } else if (changeType == ControllerChangeType.POP_EXIT) {
                        if (index > 0) {
                            removeChild(index - 1);
                        } else {
                            getRouter().popController(ParentController.this);
                        }
                    }
                }
            });

            childRouter.setRoot(RouterTransaction.with(childController)
                    .pushChangeHandler(new FadeChangeHandler())
                    .popChangeHandler(new FadeChangeHandler()));
        }
    }

    private void removeChild(int index) {
        removeChildRouter(getChildRouters().get(index));
    }

    @Override
    public boolean handleBack() {
        if (!finishing) {
            finishing = true;
            return super.handleBack();
        }
        return true;
    }

    @Override
    protected String getTitle() {
        return "Parent/Child Demo";
    }

}
