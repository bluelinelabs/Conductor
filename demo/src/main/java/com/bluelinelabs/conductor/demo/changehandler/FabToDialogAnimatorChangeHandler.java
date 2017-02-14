package com.bluelinelabs.conductor.demo.changehandler;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler;
import com.bluelinelabs.conductor.demo.R;

public class FabToDialogAnimatorChangeHandler extends AnimatorChangeHandler {

    public FabToDialogAnimatorChangeHandler() {
        super(false);
    }

    @NonNull @Override
    protected Animator getAnimator(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, boolean isPush, boolean toAddedToContainer) {
        final AnimatorSet animator = new AnimatorSet();
        final View fab = container.findViewById(R.id.fab);

        if (isPush) {
            if (fab != null) {
                animator.play(ObjectAnimator.ofFloat(fab, View.ALPHA, 0f));
            }
            if (to != null && toAddedToContainer) {
                animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, 0f, 1f));
            }
        } else {
            if (from != null) {
                animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0f));
            }
            if (fab != null) {
                animator.play(ObjectAnimator.ofFloat(fab, View.ALPHA, 0f, 1f));
            }
        }
        return animator;
    }

    @Override
    protected void resetFromView(@NonNull View from) {

    }
}
