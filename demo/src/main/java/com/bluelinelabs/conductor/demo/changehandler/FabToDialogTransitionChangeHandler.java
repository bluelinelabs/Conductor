package com.bluelinelabs.conductor.demo.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.transitions.FabTransform;
import com.bluelinelabs.conductor.demo.util.AnimUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FabToDialogTransitionChangeHandler extends CustomTransitionChangeHandler {

    @NonNull
    @Override
    protected Transition getTransition(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, boolean isPush) {
        return new FabTransform(ContextCompat.getColor(container.getContext(), R.color.colorAccent), R.drawable.ic_add_dark);
    }

    /*
     * Container => ChangeHandlerFrameLayout of CustomTransitionDemoController
     * if push (fab to dialog) => from == container and to == DialogController::getView (ChangeHandlerFrameLayout which contains dialog)
     * if pop (dialog to fab) => from == DialogController::getView and to == container
     */
    @Override
    protected void viewChange(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, @NonNull final Transition transition, boolean isPush) {
        final View fab;
        if (isPush) {
            fab = from.findViewById(R.id.fab);
        } else {
            fab = to.findViewById(R.id.fab);
        }
        final ViewGroup fabParent = (ViewGroup)fab.getParent();

        if (isPush) {
            TransitionManager.beginDelayedTransition(container, transition);
            fabParent.removeView(fab);
            container.addView(to);

            /*
             * After the transition is finished we have to add the fab back to the original container.
             * Because otherwise we will be lost when trying to transition back.
             * Set it to invisible because we don't want it to jump back after the transition
             */
            transition.addListener(new AnimUtils.TransitionListenerWrapper() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    fab.setVisibility(View.GONE);
                    fabParent.addView(fab);
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    super.onTransitionCancel(transition);
                    fab.setVisibility(View.GONE);
                    fabParent.addView(fab);
                }
            });
        } else {
            /*
             * Before we transition back we want to remove the fab
             * in order to add it again for the TransitionManager to be able to detect the change
             */
            fabParent.removeView(fab);
            fab.setVisibility(View.VISIBLE);

            TransitionManager.beginDelayedTransition(container, transition);
            fabParent.addView(fab);
            container.removeView(from);
        }
    }
}
