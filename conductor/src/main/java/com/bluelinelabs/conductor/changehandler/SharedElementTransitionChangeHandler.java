package com.bluelinelabs.conductor.changehandler;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionSet;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.internal.TransitionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A TransitionChangeHandler that facilitates using different Transitions for the entering view, the exiting view,
 * and shared elements between the two.
 */
// Much of this class is based on FragmentTransition.java and FragmentTransitionCompat21.java from the Android support library
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class SharedElementTransitionChangeHandler extends TransitionChangeHandler {

    // A map of from -> to names. Generally these will be the same.
    @NonNull  final ArrayMap<String, String> sharedElementNames = new ArrayMap<>();

    @NonNull  final List<String> waitForTransitionNames = new ArrayList<>();
    @NonNull  final List<ViewParentPair> removedViews = new ArrayList<>();

    @Nullable Transition exitTransition;
    @Nullable Transition enterTransition;
    @Nullable Transition sharedElementTransition;
    @Nullable private SharedElementCallback exitTransitionCallback;
    @Nullable private SharedElementCallback enterTransitionCallback;

    @NonNull
    @Override
    protected final Transition getTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        exitTransition = getExitTransition(container, from, to, isPush);
        enterTransition = getEnterTransition(container, from, to, isPush);
        sharedElementTransition = getSharedElementTransition(container, from, to, isPush);
        exitTransitionCallback = getExitTransitionCallback(container, from, to, isPush);
        enterTransitionCallback = getEnterTransitionCallback(container, from, to, isPush);

        if (enterTransition == null && sharedElementTransition == null && exitTransition == null) {
            throw new IllegalStateException("SharedElementTransitionChangeHandler must have at least one transaction.");
        }

        return mergeTransitions(isPush);
    }

    @Override
    public void prepareForTransition(@NonNull final ViewGroup container, @Nullable final View from, @Nullable final View to, @NonNull final Transition transition, final boolean isPush, @NonNull final OnTransitionPreparedListener onTransitionPreparedListener) {
        OnTransitionPreparedListener listener = new OnTransitionPreparedListener() {
            @Override
            public void onPrepared() {
                configureTransition(container, from, to, transition, isPush);
                onTransitionPreparedListener.onPrepared();
            }
        };

        configureSharedElements(container, from, to, isPush);

        if (to != null && to.getParent() == null && waitForTransitionNames.size() > 0) {
            waitOnAllTransitionNames(to, listener);
            container.addView(to);
        } else {
            listener.onPrepared();
        }
    }

    @Override
    public final void executePropertyChanges(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, @Nullable Transition transition, boolean isPush) {
        if (to != null && removedViews.size() > 0) {
            to.setVisibility(View.VISIBLE);

            for (ViewParentPair removedView : removedViews) {
                removedView.parent.addView(removedView.view);
            }

            removedViews.clear();
        }

        super.executePropertyChanges(container, from, to, transition, isPush);
    }

    @Override
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, @Nullable Controller newTop) {
        super.onAbortPush(newHandler, newTop);

        removedViews.clear();
    }

    void configureTransition(@NonNull final ViewGroup container, @Nullable View from, @Nullable View to, @NonNull final Transition transition, boolean isPush) {
        final View nonExistentView = new View(container.getContext());

        List<View> fromSharedElements = new ArrayList<>();
        List<View> toSharedElements = new ArrayList<>();

        configureSharedElements(container, nonExistentView, to, from, isPush, fromSharedElements, toSharedElements);

        List<View> exitingViews = exitTransition != null ? configureEnteringExitingViews(exitTransition, from, fromSharedElements, nonExistentView) : null;
        if (exitingViews == null || exitingViews.isEmpty()) {
            exitTransition = null;
        }

        if (enterTransition != null) {
            enterTransition.addTarget(nonExistentView);
        }

        final List<View> enteringViews = new ArrayList<>();
        scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, toSharedElements);
        scheduleTargetChange(container, to, nonExistentView, toSharedElements, enteringViews, exitingViews);

        setNameOverrides(container, toSharedElements);
        scheduleNameReset(container, toSharedElements);
    }

    private void waitOnAllTransitionNames(@NonNull final View to, @NonNull final OnTransitionPreparedListener onTransitionPreparedListener) {
        OnPreDrawListener onPreDrawListener = new OnPreDrawListener() {
            boolean addedSubviewListeners;

            @Override
            public boolean onPreDraw() {
                List<View> foundViews = new ArrayList<>();
                boolean allViewsFound = true;
                for (String transitionName : waitForTransitionNames) {
                    View namedView = TransitionUtils.findNamedView(to, transitionName);
                    if (namedView != null) {
                        foundViews.add(TransitionUtils.findNamedView(to, transitionName));
                    } else {
                        allViewsFound = false;
                        break;
                    }
                }

                if (allViewsFound && !addedSubviewListeners) {
                    addedSubviewListeners = true;
                    waitOnChildTransitionNames(to, foundViews, this, onTransitionPreparedListener);
                }

                return false;
            }
        };

        to.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    void waitOnChildTransitionNames(@NonNull final View to, @NonNull List<View> foundViews, @NonNull final OnPreDrawListener parentPreDrawListener, @NonNull final OnTransitionPreparedListener onTransitionPreparedListener) {
        for (final View view : foundViews) {
            OneShotPreDrawListener.add(true, view, new Runnable() {
                @Override
                public void run() {
                    waitForTransitionNames.remove(view.getTransitionName());

                    removedViews.add(new ViewParentPair(view, (ViewGroup)view.getParent()));
                    ((ViewGroup)view.getParent()).removeView(view);

                    if (waitForTransitionNames.size() == 0) {
                        to.getViewTreeObserver().removeOnPreDrawListener(parentPreDrawListener);
                        to.setVisibility(View.INVISIBLE);
                        onTransitionPreparedListener.onPrepared();
                    }
                }
            });
        }
    }

    private void scheduleTargetChange(@NonNull final ViewGroup container, @Nullable final View to, @NonNull final View nonExistentView,
                                      @NonNull final List<View> toSharedElements, @NonNull final List<View> enteringViews, @Nullable final List<View> exitingViews) {
        OneShotPreDrawListener.add(true, container, new Runnable() {
            @Override
            public void run() {
                if (enterTransition != null) {
                    enterTransition.removeTarget(nonExistentView);
                    List<View> views = configureEnteringExitingViews(enterTransition, to, toSharedElements, nonExistentView);
                    enteringViews.addAll(views);
                }

                if (exitingViews != null) {
                    if (exitTransition != null) {
                        List<View> tempExiting = new ArrayList<>();
                        tempExiting.add(nonExistentView);
                        TransitionUtils.replaceTargets(exitTransition, exitingViews, tempExiting);
                    }
                    exitingViews.clear();
                    exitingViews.add(nonExistentView);
                }
            }
        });
    }

    private Transition mergeTransitions(boolean isPush) {
        boolean overlap = enterTransition == null || exitTransition == null || allowTransitionOverlap(isPush);

        if (overlap) {
            return TransitionUtils.mergeTransitions(TransitionSet.ORDERING_TOGETHER, exitTransition, enterTransition, sharedElementTransition);
        } else {
            Transition staggered = TransitionUtils.mergeTransitions(TransitionSet.ORDERING_SEQUENTIAL, exitTransition, enterTransition);
            return TransitionUtils.mergeTransitions(TransitionSet.ORDERING_TOGETHER, staggered, sharedElementTransition);
        }
    }

    @NonNull List<View> configureEnteringExitingViews(@NonNull Transition transition, @Nullable View view, @NonNull List<View> sharedElements, @NonNull View nonExistentView) {
        List<View> viewList = new ArrayList<>();
        if (view != null) {
            captureTransitioningViews(viewList, view);
        }
        viewList.removeAll(sharedElements);
        if (!viewList.isEmpty()) {
            viewList.add(nonExistentView);
            TransitionUtils.addTargets(transition, viewList);
        }
        return viewList;
    }

    private void configureSharedElements(@NonNull ViewGroup container, @NonNull final View nonExistentView, @Nullable final View to, @Nullable View from,
                                         final boolean isPush, @NonNull final List<View> fromSharedElements, @NonNull final List<View> toSharedElements) {

        if (to == null || from == null) {
            return;
        }

        ArrayMap<String, View> capturedFromSharedElements = captureFromSharedElements(from);

        if (sharedElementNames.isEmpty()) {
            sharedElementTransition = null;
        } else if (capturedFromSharedElements != null) {
            fromSharedElements.addAll(capturedFromSharedElements.values());
        }

        if (enterTransition == null && exitTransition == null && sharedElementTransition == null) {
            return;
        }

        callSharedElementStartEnd(capturedFromSharedElements, true);

        final Rect toEpicenter;
        if (sharedElementTransition != null) {
            toEpicenter = new Rect();
            TransitionUtils.setTargets(sharedElementTransition, nonExistentView, fromSharedElements);
            setFromEpicenter(capturedFromSharedElements);
            if (enterTransition != null) {
                enterTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition) {
                        if (toEpicenter.isEmpty()) {
                            return null;
                        }
                        return toEpicenter;
                    }
                });
            }
        } else {
            toEpicenter = null;
        }

        OneShotPreDrawListener.add(true, container, new Runnable() {
            @Override
            public void run() {
                ArrayMap<String, View> capturedToSharedElements = captureToSharedElements(to, isPush);

                if (capturedToSharedElements != null) {
                    toSharedElements.addAll(capturedToSharedElements.values());
                    toSharedElements.add(nonExistentView);
                }

                callSharedElementStartEnd(capturedToSharedElements, false);
                if (sharedElementTransition != null) {
                    sharedElementTransition.getTargets().clear();
                    sharedElementTransition.getTargets().addAll(toSharedElements);
                    TransitionUtils.replaceTargets(sharedElementTransition, fromSharedElements, toSharedElements);

                    final View toEpicenterView = getToEpicenterView(capturedToSharedElements);
                    if (toEpicenterView != null && toEpicenter != null) {
                        TransitionUtils.getBoundsOnScreen(toEpicenterView, toEpicenter);
                    }
                }
            }
        });
    }

    @Nullable View getToEpicenterView(@Nullable ArrayMap<String, View> toSharedElements) {
        if (enterTransition != null && sharedElementNames.size() > 0 && toSharedElements != null) {
            return toSharedElements.get(sharedElementNames.valueAt(0));
        }
        return null;
    }

    private void setFromEpicenter(@Nullable ArrayMap<String, View> fromSharedElements) {
        if (sharedElementNames.size() > 0 && fromSharedElements != null) {
            final View fromEpicenterView = fromSharedElements.get(sharedElementNames.keyAt(0));

            if (sharedElementTransition != null) {
                TransitionUtils.setEpicenter(sharedElementTransition, fromEpicenterView);
            }

            if (exitTransition != null) {
                TransitionUtils.setEpicenter(exitTransition, fromEpicenterView);
            }
        }
    }

    @Nullable ArrayMap<String, View> captureToSharedElements(@Nullable final View to, boolean isPush) {
        if (sharedElementNames.isEmpty() || sharedElementTransition == null || to == null) {
            sharedElementNames.clear();
            return null;
        }

        final ArrayMap<String, View> toSharedElements = new ArrayMap<>();
        TransitionUtils.findNamedViews(toSharedElements, to);
        for (ViewParentPair removedView : removedViews) {
            toSharedElements.put(removedView.view.getTransitionName(), removedView.view);
        }

        final List<String> names = new ArrayList<>(sharedElementNames.values());

        toSharedElements.retainAll(names);
        if (enterTransitionCallback != null) {
            enterTransitionCallback.onMapSharedElements(names, toSharedElements);
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                View view = toSharedElements.get(name);
                if (view == null) {
                    String key = findKeyForValue(sharedElementNames, name);
                    if (key != null) {
                        sharedElementNames.remove(key);
                    }
                } else if (!name.equals(view.getTransitionName())) {
                    String key = findKeyForValue(sharedElementNames, name);
                    if (key != null) {
                        sharedElementNames.put(key, view.getTransitionName());
                    }
                }
            }
        } else {
            for (int i = sharedElementNames.size() - 1; i >= 0; i--) {
                final String targetName = sharedElementNames.valueAt(i);
                if (!toSharedElements.containsKey(targetName)) {
                    sharedElementNames.removeAt(i);
                }
            }
        }
        return toSharedElements;
    }

    @Nullable String findKeyForValue(@NonNull ArrayMap<String, String> map, @NonNull String value) {
        final int numElements = map.size();
        for (int i = 0; i < numElements; i++) {
            if (value.equals(map.valueAt(i))) {
                return map.keyAt(i);
            }
        }
        return null;
    }

    @Nullable
    private ArrayMap<String, View> captureFromSharedElements(@NonNull View from) {
        if (sharedElementNames.isEmpty() || sharedElementTransition == null) {
            sharedElementNames.clear();
            return null;
        }

        final ArrayMap<String, View> fromSharedElements = new ArrayMap<>();
        TransitionUtils.findNamedViews(fromSharedElements, from);

        final List<String> names = new ArrayList<>(sharedElementNames.keySet());

        fromSharedElements.retainAll(names);
        if (exitTransitionCallback != null) {
            exitTransitionCallback.onMapSharedElements(names, fromSharedElements);
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                View view = fromSharedElements.get(name);
                if (view == null) {
                    sharedElementNames.remove(name);
                } else if (!name.equals(view.getTransitionName())) {
                    String targetValue = sharedElementNames.remove(name);
                    sharedElementNames.put(view.getTransitionName(), targetValue);
                }
            }
        } else {
            sharedElementNames.retainAll(fromSharedElements.keySet());
        }
        return fromSharedElements;
    }

    void callSharedElementStartEnd(@Nullable ArrayMap<String, View> sharedElements, boolean isStart) {
        if (enterTransitionCallback != null) {
            final int count = sharedElements == null ? 0 : sharedElements.size();
            List<View> views = new ArrayList<>(count);
            List<String> names = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                names.add(sharedElements.keyAt(i));
                views.add(sharedElements.valueAt(i));
            }
            if (isStart) {
                enterTransitionCallback.onSharedElementStart(names, views, null);
            } else {
                enterTransitionCallback.onSharedElementEnd(names, views, null);
            }
        }
    }

    private void captureTransitioningViews(@NonNull List<View> transitioningViews, @NonNull View view) {
        if (view.getVisibility() == View.VISIBLE) {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                if (viewGroup.isTransitionGroup()) {
                    transitioningViews.add(viewGroup);
                } else {
                    int count = viewGroup.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = viewGroup.getChildAt(i);
                        captureTransitioningViews(transitioningViews, child);
                    }
                }
            } else {
                transitioningViews.add(view);
            }
        }
    }

    private void scheduleRemoveTargets(@NonNull final Transition overallTransition,
                                       @Nullable final Transition enterTransition, @Nullable final List<View> enteringViews,
                                       @Nullable final Transition exitTransition, @Nullable final List<View> exitingViews,
                                       @Nullable final Transition sharedElementTransition, @Nullable final List<View> toSharedElements) {
        overallTransition.addListener(new TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                if (enterTransition != null && enteringViews != null) {
                    TransitionUtils.replaceTargets(enterTransition, enteringViews, null);
                }
                if (exitTransition != null && exitingViews != null) {
                    TransitionUtils.replaceTargets(exitTransition, exitingViews, null);
                }
                if (sharedElementTransition != null && toSharedElements != null) {
                    TransitionUtils.replaceTargets(sharedElementTransition, toSharedElements, null);
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
    }

    private void setNameOverrides(@NonNull final View container, @NonNull final List<View> toSharedElements) {
        OneShotPreDrawListener.add(true, container, new Runnable() {
            @Override
            public void run() {
                final int numSharedElements = toSharedElements.size();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = toSharedElements.get(i);
                    String name = view.getTransitionName();
                    if (name != null) {
                        String inName = findKeyForValue(sharedElementNames, name);
                        view.setTransitionName(inName);
                    }
                }
            }
        });
    }

    private void scheduleNameReset(@NonNull final ViewGroup container, @NonNull final List<View> toSharedElements) {
        OneShotPreDrawListener.add(true, container, new Runnable() {
            @Override
            public void run() {
                final int numSharedElements = toSharedElements.size();
                for (int i = 0; i < numSharedElements; i++) {
                    final View view = toSharedElements.get(i);
                    final String name = view.getTransitionName();
                    final String inName = sharedElementNames.get(name);
                    view.setTransitionName(inName);
                }
            }
        });
    }

    /**
     * Will be called when views are ready to have their shared elements configured. Within this method one of the addSharedElement methods
     * should be called for each shared element that will be used. If one or more of these shared elements will not instantly be available in
     * the incoming view (for ex, in a RecyclerView), waitOnSharedElementNamed can be called to delay the transition until everything is available.
     */
    public abstract void configureSharedElements(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush);

    /**
     * Should return the transition that will be used on the exiting ("from") view, if one is desired.
     */
    @Nullable
    public abstract Transition getExitTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush);

    /**
     * Should return the transition that will be used on shared elements between the from and to views.
     */
    @Nullable
    public abstract Transition getSharedElementTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush);

    /**
     * Should return the transition that will be used on the entering ("to") view, if one is desired.
     */
    @Nullable
    public abstract Transition getEnterTransition(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush);

    /**
     * Should return a callback that can be used to customize transition behavior of the shared element transition for the "from" view.
     */
    @Nullable
    public SharedElementCallback getExitTransitionCallback(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        return null;
    }

    /**
     * Should return a callback that can be used to customize transition behavior of the shared element transition for the "to" view.
     */
    @Nullable
    public SharedElementCallback getEnterTransitionCallback(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush) {
        return null;
    }

    /**
     * Should return whether or not the the exit transition and enter transition should overlap. If true,
     * the enter transition will start as soon as possible. Otherwise, the enter transition will wait until the
     * completion of the exit transition. Defaults to true.
     */
    public boolean allowTransitionOverlap(boolean isPush) {
        return true;
    }

    /**
     * Used to register an element that will take part in the shared element transition.
     *
     * @param name The transition name that is used for both the entering and exiting views.
     */
    protected final void addSharedElement(@NonNull String name) {
        sharedElementNames.put(name, name);
    }

    /**
     * Used to register an element that will take part in the shared element transition. Maps the name used in the
     * "from" view to the name used in the "to" view if they are not the same.
     *
     * @param fromName The transition name used in the "from" view
     * @param toName The transition name used in the "to" view
     */
    protected final void addSharedElement(@NonNull String fromName, @NonNull String toName) {
        sharedElementNames.put(fromName, toName);
    }

    /**
     * Used to register an element that will take part in the shared element transition. Maps the name used in the
     * "from" view to the name used in the "to" view if they are not the same.
     *
     * @param sharedElement The view from the "from" view that will take part in the shared element transition
     * @param toName The transition name used in the "to" view
     */
    protected final void addSharedElement(@NonNull View sharedElement, @NonNull String toName) {
        String transitionName = sharedElement.getTransitionName();
        if (transitionName == null) {
            throw new IllegalArgumentException("Unique transitionNames are required for all sharedElements");
        }
        sharedElementNames.put(transitionName, toName);
    }

    /**
     * The transition will be delayed until the view with the name passed in is available in the "to" hierarchy. This is
     * particularly useful for views that don't load instantly, like RecyclerViews. Note that using this method can
     * potentially lock up your app indefinitely if the view never loads!
     */
    protected final void waitOnSharedElementNamed(@NonNull String name) {
        if (!sharedElementNames.values().contains(name)) {
            throw new IllegalStateException("Can't wait on a shared element that hasn't been registered using addSharedElement");
        }
        waitForTransitionNames.add(name);
    }

    private static class OneShotPreDrawListener implements OnPreDrawListener, View.OnAttachStateChangeListener {

        private final View view;
        private ViewTreeObserver viewTreeObserver;
        private final Runnable runnable;
        private final boolean preDrawReturnValue;

        private OneShotPreDrawListener(boolean preDrawReturnValue, @NonNull View view, @NonNull Runnable runnable) {
            this.preDrawReturnValue = preDrawReturnValue;
            this.view = view;
            viewTreeObserver = view.getViewTreeObserver();
            this.runnable = runnable;
        }

        @NonNull
        public static OneShotPreDrawListener add(boolean preDrawReturnValue, @NonNull View view, @NonNull Runnable runnable) {
            OneShotPreDrawListener listener = new OneShotPreDrawListener(preDrawReturnValue, view, runnable);
            view.getViewTreeObserver().addOnPreDrawListener(listener);
            view.addOnAttachStateChangeListener(listener);
            return listener;
        }

        @Override
        public boolean onPreDraw() {
            removeListener();
            runnable.run();
            return preDrawReturnValue;
        }

        private void removeListener() {
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnPreDrawListener(this);
            } else {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            view.removeOnAttachStateChangeListener(this);
        }

        @Override
        public void onViewAttachedToWindow(View v) {
            viewTreeObserver = v.getViewTreeObserver();
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            removeListener();
        }

    }

    private static class ViewParentPair {
        @NonNull final View view;
        @NonNull final ViewGroup parent;

        ViewParentPair(@NonNull View view, ViewGroup parent) {
            this.view = view;
            this.parent = parent;
        }
    }

}
