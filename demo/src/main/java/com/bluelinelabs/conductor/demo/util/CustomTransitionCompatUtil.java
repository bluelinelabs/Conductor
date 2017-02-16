package com.bluelinelabs.conductor.demo.util;


import android.os.Build;
import android.support.annotation.NonNull;

import com.bluelinelabs.conductor.ControllerChangeHandler;

public final class CustomTransitionCompatUtil {

    private CustomTransitionCompatUtil() {
    }

    public static ControllerChangeHandler getTransitionCompat(@NonNull final ControllerChangeHandler transitionChangeHandler, @NonNull final ControllerChangeHandler animatorChangeHandler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return transitionChangeHandler;
        } else {
            return animatorChangeHandler;
        }
    }
}
