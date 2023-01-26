package com.bluelinelabs.conductor.viewpager;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An ViewPager adapter that uses Routers as pages
 */
public abstract class RouterPagerAdapter extends PagerAdapter {

    private static final String KEY_SAVED_PAGES = "RouterPagerAdapter.savedStates";
    private static final String KEY_TAGS_KEYS = "RouterPagerAdapter.tags.keys";
    private static final String KEY_TAGS_VALUES = "RouterPagerAdapter.tags.values";
    private static final String KEY_MAX_PAGES_TO_STATE_SAVE = "RouterPagerAdapter.maxPagesToStateSave";
    private static final String KEY_SAVE_PAGE_HISTORY = "RouterPagerAdapter.savedPageHistory";

    private final Controller host;
    private int maxPagesToStateSave = Integer.MAX_VALUE;
    private final Map<Integer, String> tags = new HashMap<>();
    private SparseArray<Bundle> savedPages = new SparseArray<>();
    private final SparseArray<Router> visibleRouters = new SparseArray<>();
    private ArrayList<Integer> savedPageHistory = new ArrayList<>();
    private Router currentPrimaryRouter;

    /**
     * Creates a new RouterPagerAdapter using the passed host.
     */
    public RouterPagerAdapter(@NonNull Controller host) {
        this.host = host;
    }

    /**
     * Called when a router is instantiated. Here the router's root should be set if needed.
     *
     * @param router   The router used for the page
     * @param position The page position to be instantiated.
     */
    public abstract void configureRouter(@NonNull Router router, int position);

    /**
     * Sets the maximum number of pages that will have their states saved. When this number is exceeded,
     * the page that was state saved least recently will have its state removed from the save data.
     */
    public void setMaxPagesToStateSave(int maxPagesToStateSave) {
        if (maxPagesToStateSave < 0) {
            throw new IllegalArgumentException("Only positive integers may be passed for maxPagesToStateSave.");
        }

        this.maxPagesToStateSave = maxPagesToStateSave;

        ensurePagesSaved();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final String name = makeRouterName(container.getId(), getItemId(position));

        // Ensure we don't try to restore state for a router with a different ID just because
        // the position was reused. Fixes https://github.com/bluelinelabs/Conductor/issues/582
        if (tags.get(position) != null && !tags.get(position).equals(name)) {
            savedPages.remove(position);
        }

        Router router = host.getChildRouter(container, name)
                .setPopRootControllerMode(Router.PopRootControllerMode.NEVER);
        if (!router.hasRootController()) {
            Bundle routerSavedState = savedPages.get(position);

            if (routerSavedState != null) {
                router.restoreInstanceState(routerSavedState);
                savedPages.remove(position);
                savedPageHistory.remove((Integer) position);
            }
        }

        router.rebindIfNeeded();
        configureRouter(router, position);

        if (router != currentPrimaryRouter) {
            for (RouterTransaction transaction : router.getBackstack()) {
                transaction.controller().setOptionsMenuHidden(true);
            }
        }

        tags.put(position, name);
        visibleRouters.put(position, router);
        return router;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Router router = (Router)object;

        Bundle savedState = new Bundle();
        router.saveInstanceState(savedState);
        savedPages.put(position, savedState);

        savedPageHistory.remove((Integer) position);
        savedPageHistory.add(position);

        ensurePagesSaved();

        host.removeChildRouter(router);

        visibleRouters.remove(position);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Router router = (Router) object;
        if (router != currentPrimaryRouter) {
            if (currentPrimaryRouter != null) {
                for (RouterTransaction transaction : currentPrimaryRouter.getBackstack()) {
                    transaction.controller().setOptionsMenuHidden(true);
                }
            }
            if (router != null) {
                for (RouterTransaction transaction : router.getBackstack()) {
                    transaction.controller().setOptionsMenuHidden(false);
                }
            }
            currentPrimaryRouter = router;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        Router router = (Router)object;
        final List<RouterTransaction> backstack = router.getBackstack();
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller().getView() == view) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(KEY_SAVED_PAGES, savedPages);
        bundle.putIntegerArrayList(KEY_TAGS_KEYS, new ArrayList<>(tags.keySet()));
        bundle.putStringArrayList(KEY_TAGS_VALUES, new ArrayList<>(tags.values()));
        bundle.putInt(KEY_MAX_PAGES_TO_STATE_SAVE, maxPagesToStateSave);
        bundle.putIntegerArrayList(KEY_SAVE_PAGE_HISTORY, savedPageHistory);
        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle bundle = (Bundle)state;
        if (state != null) {
            savedPages = bundle.getSparseParcelableArray(KEY_SAVED_PAGES);
            maxPagesToStateSave = bundle.getInt(KEY_MAX_PAGES_TO_STATE_SAVE);
            savedPageHistory = bundle.getIntegerArrayList(KEY_SAVE_PAGE_HISTORY);

            List<Integer> tagsKeys = bundle.getIntegerArrayList(KEY_TAGS_KEYS);
            List<String> tagsValues = bundle.getStringArrayList(KEY_TAGS_VALUES);
            if (tagsKeys != null && tagsValues != null && tagsKeys.size() == tagsValues.size()) {
                for (int i = 0; i < tagsKeys.size(); i++) {
                    tags.put(tagsKeys.get(i), tagsValues.get(i));
                }
            }
        }
    }

    /**
     * Returns the already instantiated Router in the specified position or {@code null} if there
     * is no router associated with this position.
     */
    @Nullable
    public Router getRouter(int position) {
        return visibleRouters.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    SparseArray<Bundle> getSavedPages() {
        return savedPages;
    }

    private void ensurePagesSaved() {
        while (savedPages.size() > maxPagesToStateSave) {
            int positionToRemove = savedPageHistory.remove(0);
            savedPages.remove(positionToRemove);
        }
    }

    private static String makeRouterName(int viewId, long id) {
        return viewId + ":" + id;
    }

}
