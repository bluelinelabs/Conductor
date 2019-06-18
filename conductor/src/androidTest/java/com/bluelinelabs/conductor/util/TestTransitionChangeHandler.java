package com.bluelinelabs.conductor.util;

import android.transition.Slide;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler;

public class TestTransitionChangeHandler extends TransitionChangeHandler {
    @NonNull
    @Override
    protected Transition getTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        return new Slide();
    }
}
