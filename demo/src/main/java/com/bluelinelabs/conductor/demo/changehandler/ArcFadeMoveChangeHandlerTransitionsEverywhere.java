package com.bluelinelabs.conductor.demo.changehandler;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.transitionseverywhere.TransitionsEverywhereChangeHandler;
import com.transitionseverywhere.ArcMotion;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.ChangeClipBounds;
import com.transitionseverywhere.ChangeTransform;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionSet;

public class ArcFadeMoveChangeHandlerTransitionsEverywhere extends TransitionsEverywhereChangeHandler {

    public ArcFadeMoveChangeHandlerTransitionsEverywhere() { }

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
