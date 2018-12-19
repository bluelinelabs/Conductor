package com.bluelinelabs.conductor.support;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

/**
 * @deprecated Use RouterPagerAdapter instead! This implementation was too limited and had too many
 * gotchas associated with it.
 *
 * An adapter for ViewPagers that will handle adding and removing Controllers
 */
@Deprecated
public abstract class ControllerPagerAdapter extends PagerAdapter {

    private static final String KEY_SAVED_PAGES = "ControllerPagerAdapter.savedStates";
    private static final String KEY_SAVES_STATE = "ControllerPagerAdapter.savesState";
    private static final String KEY_VISIBLE_PAGE_IDS_KEYS = "ControllerPagerAdapter.visiblePageIds.keys";
    private static final String KEY_VISIBLE_PAGE_IDS_VALUES = "ControllerPagerAdapter.visiblePageIds.values";

    private final Controller host;
    private boolean savesState;
    private SparseArray<Bundle> savedPages = new SparseArray<>();
    private SparseArray<String> visiblePageIds = new SparseArray<>();

    /**
     * Creates a new ControllerPagerAdapter using the passed host.
     */
    public ControllerPagerAdapter(@NonNull Controller host, boolean saveControllerState) {
        this.host = host;
        savesState = saveControllerState;
    }

    /**
     * Return the Controller associated with a specified position.
     */
    @NonNull
    public abstract Controller getItem(int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final String name = makeControllerName(container.getId(), getItemId(position));

        Router router = host.getChildRouter(container, name);
        if (savesState && !router.hasRootController()) {
            Bundle routerSavedState = savedPages.get(position);

            if (routerSavedState != null) {
                router.restoreInstanceState(routerSavedState);
            }
        }

        final Controller controller;
        if (!router.hasRootController()) {
            controller = getItem(position);
            router.setRoot(RouterTransaction.with(controller).tag(name));
        } else {
            router.rebindIfNeeded();
            controller = router.getControllerWithTag(name);
        }

        if (controller != null) {
            visiblePageIds.put(position, controller.getInstanceId());
        }

        return router.getControllerWithTag(name);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Router router = ((Controller)object).getRouter();

        if (savesState) {
            Bundle savedState = new Bundle();
            router.saveInstanceState(savedState);
            savedPages.put(position, savedState);
        }

        visiblePageIds.remove(position);

        host.removeChildRouter(router);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Controller)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SAVES_STATE, savesState);
        bundle.putSparseParcelableArray(KEY_SAVED_PAGES, savedPages);

        int[] visiblePageIdsKeys = new int[visiblePageIds.size()];
        String[] visiblePageIdsValues = new String[visiblePageIds.size()];
        for (int i = 0; i < visiblePageIds.size(); i++) {
            visiblePageIdsKeys[i] = visiblePageIds.keyAt(i);
            visiblePageIdsValues[i] = visiblePageIds.valueAt(i);
        }
        bundle.putIntArray(KEY_VISIBLE_PAGE_IDS_KEYS, visiblePageIdsKeys);
        bundle.putStringArray(KEY_VISIBLE_PAGE_IDS_VALUES, visiblePageIdsValues);

        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle bundle = (Bundle)state;
        if (state != null) {
            savesState = bundle.getBoolean(KEY_SAVES_STATE, false);
            savedPages = bundle.getSparseParcelableArray(KEY_SAVED_PAGES);

            int[] visiblePageIdsKeys = bundle.getIntArray(KEY_VISIBLE_PAGE_IDS_KEYS);
            String[] visiblePageIdsValues = bundle.getStringArray(KEY_VISIBLE_PAGE_IDS_VALUES);
            visiblePageIds = new SparseArray<>(visiblePageIdsKeys.length);
            for (int i = 0; i < visiblePageIdsKeys.length; i++) {
                visiblePageIds.put(visiblePageIdsKeys[i], visiblePageIdsValues[i]);
            }
        }
    }

    /**
     * Returns the already instantiated Controller in the specified position or {@code null} if
     * this position does not yet have a controller.
     */
    @Nullable
    public Controller getController(int position) {
        String instanceId = visiblePageIds.get(position);
        if (instanceId != null) {
            return host.getRouter().getControllerWithInstanceId(instanceId);
        } else {
            return null;
        }
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeControllerName(int viewId, long id) {
        return viewId + ":" + id;
    }

}
