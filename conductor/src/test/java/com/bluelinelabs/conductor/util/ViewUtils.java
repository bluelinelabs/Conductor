package com.bluelinelabs.conductor.util;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;

import org.robolectric.util.ReflectionHelpers;

import java.util.List;

public class ViewUtils {

    public static void reportAttached(View view, boolean attached) {
        reportAttached(view, attached, true);
    }

    public static void reportAttached(View view, boolean attached, boolean propogateToChildren) {
        if (view instanceof AttachFakingFrameLayout) {
            ((AttachFakingFrameLayout)view).setAttached(attached, false);
        }

        List<OnAttachStateChangeListener> listeners = getAttachStateListeners(view);

        // Add, then remove an OnAttachStateChangeListener to initialize the attachStateListeners variable inside a view
        if (listeners == null) {
            OnAttachStateChangeListener tmpListener = new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) { }

                @Override
                public void onViewDetachedFromWindow(View v) { }
            };
            view.addOnAttachStateChangeListener(tmpListener);
            view.removeOnAttachStateChangeListener(tmpListener);
            listeners = getAttachStateListeners(view);
        }

        for (OnAttachStateChangeListener listener : listeners) {
            if (attached) {
                listener.onViewAttachedToWindow(view);
            } else {
                listener.onViewDetachedFromWindow(view);
            }
        }

        if (propogateToChildren && view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                reportAttached(viewGroup.getChildAt(i), attached, true);
            }
        }

    }

    private static List<OnAttachStateChangeListener> getAttachStateListeners(View view) {
        Object listenerInfo = ReflectionHelpers.callInstanceMethod(view, "getListenerInfo");
        return ReflectionHelpers.getField(listenerInfo, "mOnAttachStateChangeListeners");
    }

}
