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

    @Override @NonNull
    protected Animator getAnimator(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, boolean toAddedToContainer) {
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
                // Allow this to have a nice transition when coming off an aborted push animation
                float fromLeft = from != null ? from.getTranslationX() : 0;
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, fromLeft - to.getWidth(), 0));
            }
        }

        return animatorSet;
    }

    @Override
    protected void resetFromView(@NonNull View from) {
        from.setTranslationX(0);
    }

    @Override @NonNull
    public ControllerChangeHandler copy() {
        return new HorizontalChangeHandler(getAnimationDuration(), removesFromViewOnPush());
    }

}
