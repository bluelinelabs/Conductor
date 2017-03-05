package com.bluelinelabs.conductor.demo.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ArcFadeMoveChangeHandler extends TransitionChangeHandler {

    public ArcFadeMoveChangeHandler() { }

    @Override
    @NonNull
    protected Transition getTransition(@NonNull ViewGroup container, View from, View to, boolean isPush) {
        TransitionSet transition = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                .addTransition(new Fade(Fade.OUT))
                .addTransition(new TransitionSet().addTransition(new ChangeBounds()).addTransition(new ChangeClipBounds()).addTransition(new ChangeTransform()))
                .addTransition(new Fade(Fade.IN));

        transition.setPathMotion(new ArcMotion());

        return transition;
    }

}
