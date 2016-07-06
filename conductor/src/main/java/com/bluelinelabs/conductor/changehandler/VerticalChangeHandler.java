package com.bluelinelabs.conductor.changehandler;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AnimatorChangeHandler} that will slide either slide a new View up or slide an old View down,
 * depending on whether a push or pop change is happening.
 */
public class VerticalChangeHandler extends AnimatorChangeHandler {

    public VerticalChangeHandler() { }

    public VerticalChangeHandler(boolean removesFromViewOnPush) {
        super(removesFromViewOnPush);
    }

    public VerticalChangeHandler(long duration) {
        super(duration);
    }

    public VerticalChangeHandler(long duration, boolean removesFromViewOnPush) {
        super(duration, removesFromViewOnPush);
    }

    @Override
    protected Animator getAnimator(@NonNull ViewGroup container, View from, View to, boolean isPush, boolean toAddedToContainer) {
        AnimatorSet animator = new AnimatorSet();
        List<Animator> viewAnimators = new ArrayList<>();

        if (isPush) {
            if (from != null) {
                viewAnimators.add(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, -from.getHeight()));
            }
            if (to != null) {
                viewAnimators.add(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, to.getHeight(), 0));
            }
        } else {
            if (from != null) {
                viewAnimators.add(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, from.getHeight()));
            }
            if (to != null) {
                viewAnimators.add(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, -to.getHeight(), 0));
            }
        }

        animator.playTogether(viewAnimators);
        return animator;
    }

    @Override
    protected void resetFromView(@NonNull View from) {
        from.setTranslationY(0);
    }

}
