package com.bluelinelabs.conductor.changehandler;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * An {@link AnimatorChangeHandler} that will slide the views left or right, depending on if it's a push or pop.
 */
public class HorizontalChangeHandler extends AnimatorChangeHandler {

    public HorizontalChangeHandler() { }

    public HorizontalChangeHandler(boolean removesFromViewOnPush) {
        super(removesFromViewOnPush);
    }

    public HorizontalChangeHandler(long duration) {
        super(duration);
    }

    public HorizontalChangeHandler(long duration, boolean removesFromViewOnPush) {
        super(duration, removesFromViewOnPush);
    }

    @Override
    protected Animator getAnimator(@NonNull ViewGroup container, View from, View to, boolean isPush, boolean toAddedToContainer) {
        AnimatorSet animatorSet = new AnimatorSet();

        if (isPush) {
            if (from != null) {
                animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, -from.getWidth()));
            }
            if (to != null) {
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, to.getWidth(), 0));
            }
        } else {
            if (from != null) {
                animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, from.getWidth()));
            }
            if (to != null) {
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, -to.getWidth(), 0));
            }
        }

        return animatorSet;
    }

    @Override
    protected void resetFromView(@NonNull View from) {
        from.setTranslationX(0);
    }
}
