package com.bluelinelabs.conductor.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bluelinelabs.conductor.Router;

import static com.bluelinelabs.conductor.internal.LifecycleHandler.KEY_ROUTER_STATE_PREFIX;

/**
 * Life cycle internal handler for Activity's lifecycle, extract from {@link LifecycleHandler}
 * to reduce complexity.
 *
 * Created by desmond on 10/9/16.
 */
class LifecycleReceiver {

    private LifecycleHandler handler;

    LifecycleReceiver(LifecycleHandler handler) {
        this.handler = handler;
    }

    void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    void onActivityStarted(Activity activity) {
        for (Router router : handler.routerMap.values()) {
            router.onActivityStarted(activity);
        }
    }

    void onActivityResumed(Activity activity) {
        for (Router router : handler.routerMap.values()) {
            router.onActivityResumed(activity);
        }
    }

    void onActivityPaused(Activity activity) {
        for (Router router : handler.routerMap.values()) {
            router.onActivityPaused(activity);
        }
    }

    void onActivityStopped(Activity activity) {
        for (Router router : handler.routerMap.values()) {
            router.onActivityStopped(activity);
        }
    }

    void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        for (Router router : handler.routerMap.values()) {
            Bundle bundle = new Bundle();
            router.saveInstanceState(bundle);
            outState.putBundle(KEY_ROUTER_STATE_PREFIX + router.getContainerId(), bundle);
        }
    }

    void onActivityDestroyed(Activity activity) { }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        String instanceId = handler.activityRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : handler.routerMap.values()) {
                router.onActivityResult(instanceId, requestCode, resultCode, data);
            }
        }
    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String instanceId = handler.permissionRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : handler.routerMap.values()) {
                router.onRequestPermissionsResult(instanceId, requestCode, permissions, grantResults);
            }
        }
    }

    void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        for (Router router : handler.routerMap.values()) {
            router.onCreateOptionsMenu(menu, inflater);
        }
    }

    void onPrepareOptionsMenu(Menu menu) {
        for (Router router : handler.routerMap.values()) {
            router.onPrepareOptionsMenu(menu);
        }
    }

    boolean onOptionsItemSelected(MenuItem item) {
        for (Router router : handler.routerMap.values()) {
            if (router.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return false;
    }
}
