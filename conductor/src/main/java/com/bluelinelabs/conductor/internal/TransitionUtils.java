package com.bluelinelabs.conductor.internal;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TransitionUtils {

    public static void findNamedViews(@NonNull Map<String, View> namedViews, View view) {
        if (view.getVisibility() == View.VISIBLE) {
            String transitionName = view.getTransitionName();
            if (transitionName != null) {
                namedViews.put(transitionName, view);
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = viewGroup.getChildAt(i);
                    findNamedViews(namedViews, child);
                }
            }
        }
    }

    @Nullable
    public static View findNamedView(@NonNull View view, @NonNull String transitionName) {
        if (transitionName.equals(view.getTransitionName())) {
            return view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View viewWithTransitionName = findNamedView(viewGroup.getChildAt(i), transitionName);
                if (viewWithTransitionName != null) {
                    return viewWithTransitionName;
                }
            }
        }

        return null;
    }

    public static void setEpicenter(@NonNull Transition transition, @Nullable View view) {
        if (view != null) {
            final Rect epicenter = new Rect();
            getBoundsOnScreen(view, epicenter);
            transition.setEpicenterCallback(new Transition.EpicenterCallback() {
                @Override
                public Rect onGetEpicenter(Transition transition) {
                    return epicenter;
                }
            });
        }
    }

    public static void getBoundsOnScreen(@NonNull View view, @NonNull Rect epicenter) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        epicenter.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
    }

    public static void setTargets(@NonNull Transition transition, @NonNull View nonExistentView, @NonNull List<View> sharedViews) {
        final List<View> views = transition.getTargets();
        views.clear();
        final int count = sharedViews.size();
        for (int i = 0; i < count; i++) {
            final View view = sharedViews.get(i);
            bfsAddViewChildren(views, view);
        }
        views.add(nonExistentView);
        sharedViews.add(nonExistentView);
        addTargets(transition, sharedViews);
    }

    public static void addTargets(@Nullable Transition transition, @NonNull List<View> views) {
        if (transition == null) {
            return;
        }
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (int i = 0; i < numTransitions; i++) {
                Transition child = set.getTransitionAt(i);
                addTargets(child, views);
            }
        } else if (!hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (isNullOrEmpty(targets)) {
                int numViews = views.size();
                for (int i = 0; i < numViews; i++) {
                    transition.addTarget(views.get(i));
                }
            }
        }
    }

    public static void replaceTargets(@NonNull Transition transition, @NonNull List<View> oldTargets, @Nullable List<View> newTargets) {
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (int i = 0; i < numTransitions; i++) {
                Transition child = set.getTransitionAt(i);
                replaceTargets(child, oldTargets, newTargets);
            }
        } else if (!TransitionUtils.hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (targets != null && targets.size() == oldTargets.size() && targets.containsAll(oldTargets)) {
                final int targetCount = newTargets == null ? 0 : newTargets.size();
                for (int i = 0; i < targetCount; i++) {
                    transition.addTarget(newTargets.get(i));
                }
                for (int i = oldTargets.size() - 1; i >= 0; i--) {
                    transition.removeTarget(oldTargets.get(i));
                }
            }
        }
    }

    private static void bfsAddViewChildren(@NonNull final List<View> views, @NonNull final View startView) {
        final int startIndex = views.size();
        if (containedBeforeIndex(views, startView, startIndex)) {
            return; // This child is already in the list, so all its children are also.
        }
        views.add(startView);
        for (int index = startIndex; index < views.size(); index++) {
            final View view = views.get(index);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                final int childCount = viewGroup.getChildCount();
                for (int childIndex = 0; childIndex < childCount; childIndex++) {
                    final View child = viewGroup.getChildAt(childIndex);
                    if (!containedBeforeIndex(views, child, startIndex)) {
                        views.add(child);
                    }
                }
            }
        }
    }

    private static boolean containedBeforeIndex(@NonNull List<View> views, View view, int maxIndex) {
        for (int i = 0; i < maxIndex; i++) {
            if (views.get(i) == view) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSimpleTarget(@NonNull Transition transition) {
        return !isNullOrEmpty(transition.getTargetIds())
                || !isNullOrEmpty(transition.getTargetNames())
                || !isNullOrEmpty(transition.getTargetTypes());
    }

    private static boolean isNullOrEmpty(@Nullable List list) {
        return list == null || list.isEmpty();
    }

    @NonNull
    public static TransitionSet mergeTransitions(int ordering, Transition... transitions) {
        TransitionSet transitionSet = new TransitionSet();
        for (Transition transition : transitions) {
            if (transition != null) {
                transitionSet.addTransition(transition);
            }
        }
        transitionSet.setOrdering(ordering);
        return transitionSet;
    }

}
