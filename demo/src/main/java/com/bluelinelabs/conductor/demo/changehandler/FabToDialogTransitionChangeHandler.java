package com.bluelinelabs.conductor.demo.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.transitions.FabTransform;
import com.bluelinelabs.conductor.demo.changehandler.transitions.GravityArcMotion;
import com.bluelinelabs.conductor.demo.util.AnimUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FabToDialogTransitionChangeHandler extends CustomTransitionChangeHandler {

    /*
     * There are 2 issues when using FabTransform as pop transition.
     * 1. The pop animation is not executed properly, if you use this Transform between activities like Plaid
     *    it will circular unreveal the content of the dialog and animate to the correct color of the fab.
     *
     * 2. The ImageButton is visible but is in a ghost state. When calling View::invalidate and View::requestLayout it disappears.
     *    A click event will stil get triggered but the transition will not take place. When debugging the FabTransform you will see that the ImageButton is not passed anymore to Transition::captureStartValues
     *
     *    I've tried setting following (original) parameters again, LayoutParams, Background, Animator, Drawable, x, y, bounds, enabled,
     *    and called following methods View::layout, View::getOverlay::clear, View::setVisibility, View::setHasTransientState (before and after transition), View::clearFocus, View::requestFocus, View::setTransitionName
     *
     *    => When doing a Activity transition the ActivityTransitionCoordinator (and subclasses) does some internal setting and resetting of state. I have however not been able to pinpoint any method calls that
     *    explain this behaviour. https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/ActivityTransitionCoordinator.java
     *
     *    => I've also tried this without Conductor, and with android.transition.Scene but both show same behaviour as described above. So the issue is not related to Conductor.
     *    It might however be fixed for Conductor by implementing a similar system as ActivityTransitionCoordinator for Conductor if the solution can be pinpointed to one of those classes.
     *    I suspect that the issue lies within the way the transition is done. Normally a Transition is from screen A to screen B. And not screen B on top of screen A, however when doing so that still does not solve the problem.
     */
    @NonNull
    @Override
    protected Transition getTransition(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, boolean isPush) {
        if (isPush) {
            return new FabTransform(ContextCompat.getColor(container.getContext(), R.color.colorAccent), R.drawable.ic_add_dark);
        } else {
            final ChangeBounds changeBounds = new ChangeBounds();
            changeBounds.setPathMotion(new GravityArcMotion());
            return changeBounds;
        }
    }

    /*
     * Container => ChangeHandlerFrameLayout of CustomTransitionDemoController
     * if push (fab to dialog) => from == container and to == DialogController::getView (ChangeHandlerFrameLayout which contains dialog)
     * if pop (dialog to fab) => from == DialogController::getView and to == container
     */
    @Override
    protected void viewChange(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, @NonNull final Transition transition, boolean isPush) {
        final ImageButton fab = (ImageButton) container.findViewById(R.id.fab);

        if (isPush) {
            TransitionManager.beginDelayedTransition(container, transition);
            container.removeView(fab);
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
                    fab.setVisibility(View.INVISIBLE);
                    container.addView(fab);
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    super.onTransitionCancel(transition);
                    fab.setVisibility(View.INVISIBLE);
                    container.addView(fab);
                }
            });
        } else {
            /*
             * Before we transition back we want to remove the fab
             * in order to add it again for the TransitionManager to be able to detect the change
             */
            container.removeView(fab);
            fab.setVisibility(View.VISIBLE);

            TransitionManager.beginDelayedTransition(container, transition);
            container.removeView(from);
            container.addView(fab);
        }
    }
}
