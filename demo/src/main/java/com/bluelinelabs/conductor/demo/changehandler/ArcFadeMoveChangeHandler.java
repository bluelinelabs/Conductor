package com.bluelinelabs.conductor.demo.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.changehandler.SharedElementTransitionChangeHandler;
import com.bluelinelabs.conductor.internal.TransitionUtils;

import java.util.ArrayList;
import java.util.Collections;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ArcFadeMoveChangeHandler extends SharedElementTransitionChangeHandler {

    private static final String KEY_SHARED_ELEMENT_NAMES = "ArcFadeMoveChangeHandler.sharedElementNames";

    private final ArrayList<String> sharedElementNames = new ArrayList<>();

    public ArcFadeMoveChangeHandler() { }

    public ArcFadeMoveChangeHandler(String... sharedElementNames) {
        Collections.addAll(this.sharedElementNames, sharedElementNames);
    }

    @Override
    public void saveToBundle(@NonNull Bundle bundle) {
        super.saveToBundle(bundle);

        bundle.putStringArrayList(KEY_SHARED_ELEMENT_NAMES, sharedElementNames);
    }

    @Override
    public void restoreFromBundle(@NonNull Bundle bundle) {
        super.restoreFromBundle(bundle);

        sharedElementNames.addAll(bundle.getStringArrayList(KEY_SHARED_ELEMENT_NAMES));
    }

    @Nullable
    @Override
    public Transition getExitTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        return new Fade(Fade.OUT);
    }

    @Nullable
    @Override
    public Transition getSharedElementTransition(@NonNull ViewGroup container, @Nullable final View from, @Nullable View to, boolean isPush) {
        Transition transition = new TransitionSet().addTransition(new ChangeBounds()).addTransition(new ChangeClipBounds()).addTransition(new ChangeTransform());
        transition.setPathMotion(new ArcMotion());

        // The framework doesn't totally fade out the "from" shared element, so we'll hide it manually once it's safe.
        transition.addListener(new TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                if (from != null) {
                    for (String name : sharedElementNames) {
                        View namedView = TransitionUtils.findNamedView(from, name);
                        if (namedView != null) {
                            namedView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onTransitionEnd(Transition transition) { }

            @Override
            public void onTransitionCancel(Transition transition) { }

            @Override
            public void onTransitionPause(Transition transition) { }

            @Override
            public void onTransitionResume(Transition transition) { }
        });

        return transition;
    }

    @Nullable
    @Override
    public Transition getEnterTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        return new Fade(Fade.IN);
    }

    @Override
    public void configureSharedElements(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        for (String name : sharedElementNames) {
            addSharedElement(name);
        }
    }

    @Override
    public boolean allowTransitionOverlap(boolean isPush) {
        return false;
    }
}
