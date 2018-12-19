package com.bluelinelabs.conductor.changehandler;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;

/**
 * An {@link AnimatorChangeHandler} that will cross fade two views
 */
public class FadeChangeHandler extends AnimatorChangeHandler {

    public FadeChangeHandler() { }

    public FadeChangeHandler(boolean removesFromViewOnPush) {
        super(removesFromViewOnPush);
    }

    public FadeChangeHandler(long duration) {
        super(duration);
    }

    public FadeChangeHandler(long duration, boolean removesFromViewOnPush) {
        super(duration, removesFromViewOnPush);
    }

    @Override @NonNull
    protected Animator getAnimator(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, boolean toAddedToContainer) {
        AnimatorSet animator = new AnimatorSet();
        if (to != null) {
            float start = toAddedToContainer ? 0 : to.getAlpha();
            animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, start, 1));
        }

        if (from != null && (!isPush || removesFromViewOnPush())) {
            animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0));
        }

        return animator;
    }

    @Override
    protected void resetFromView(@NonNull View from) {
        from.setAlpha(1);
    }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        return new FadeChangeHandler(getAnimationDuration(), removesFromViewOnPush());
    }

}
