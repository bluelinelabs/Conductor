package com.bluelinelabs.conductor.internal;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;

public class ViewAttachHandler implements OnAttachStateChangeListener {

    private enum ReportedState {
        VIEW_DETACHED,
        ACTIVITY_STOPPED,
        ATTACHED
    }

    public interface ViewAttachListener {
        void onAttached();
        void onDetached(boolean fromActivityStop);
        void onViewDetachAfterStop();
    }

    private interface ChildAttachListener {
        void onAttached();
    }

    private boolean rootAttached = false;
    boolean childrenAttached = false;
    private boolean activityStopped = false;
    private ReportedState reportedState = ReportedState.VIEW_DETACHED;
    private ViewAttachListener attachListener;
    OnAttachStateChangeListener childOnAttachStateChangeListener;

    public ViewAttachHandler(ViewAttachListener attachListener) {
        this.attachListener = attachListener;
    }

    @Override
    public void onViewAttachedToWindow(final View v) {
        if (rootAttached) {
            return;
        }

        rootAttached = true;
        listenForDeepestChildAttach(v, new ChildAttachListener() {
            @Override
            public void onAttached() {
                childrenAttached = true;
                reportAttached();

            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        rootAttached = false;
        if (childrenAttached) {
            childrenAttached = false;
            reportDetached(false);
        }
    }

    public void listenForAttach(final View view) {
        view.addOnAttachStateChangeListener(this);
    }

    public void unregisterAttachListener(View view) {
        view.removeOnAttachStateChangeListener(this);

        if (childOnAttachStateChangeListener != null && view instanceof ViewGroup) {
            findDeepestChild((ViewGroup)view).removeOnAttachStateChangeListener(childOnAttachStateChangeListener);
        }
    }

    public void onActivityStarted() {
        activityStopped = false;
        reportAttached();
    }

    public void onActivityStopped() {
        activityStopped = true;
        reportDetached(true);
    }

    void reportAttached() {
        if (rootAttached && childrenAttached && !activityStopped && reportedState != ReportedState.ATTACHED) {
            reportedState = ReportedState.ATTACHED;
            attachListener.onAttached();
        }
    }

    private void reportDetached(boolean detachedForActivity) {
        boolean wasDetachedForActivity = reportedState == ReportedState.ACTIVITY_STOPPED;

        if (detachedForActivity) {
            reportedState = ReportedState.ACTIVITY_STOPPED;
        } else {
            reportedState = ReportedState.VIEW_DETACHED;
        }

        if (wasDetachedForActivity && !detachedForActivity) {
            attachListener.onViewDetachAfterStop();
        } else {
            attachListener.onDetached(detachedForActivity);
        }
    }

    private void listenForDeepestChildAttach(final View view, final ChildAttachListener attachListener) {
        if (!(view instanceof ViewGroup)) {
            attachListener.onAttached();
            return;
        }

        ViewGroup viewGroup = (ViewGroup)view;
        if (viewGroup.getChildCount() == 0) {
            attachListener.onAttached();
            return;
        }

        childOnAttachStateChangeListener = new OnAttachStateChangeListener() {
            boolean attached = false;

            @Override
            public void onViewAttachedToWindow(View v) {
                if (!attached) {
                    attached = true;
                    attachListener.onAttached();
                    v.removeOnAttachStateChangeListener(this);
                    childOnAttachStateChangeListener = null;
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) { }
        };
        findDeepestChild(viewGroup).addOnAttachStateChangeListener(childOnAttachStateChangeListener);
    }

    private View findDeepestChild(ViewGroup viewGroup) {
        if (viewGroup.getChildCount() == 0) {
            return viewGroup;
        }

        View lastChild = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
        if (lastChild instanceof ViewGroup) {
            return findDeepestChild((ViewGroup)lastChild);
        } else {
            return lastChild;
        }
    }

}
