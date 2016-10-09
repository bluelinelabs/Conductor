package com.bluelinelabs.conductor.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ActivityHostedRouter;
import com.bluelinelabs.conductor.Router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LifecycleHandler extends Fragment {

    private static final String FRAGMENT_TAG = "LifecycleHandler";

    private static final String KEY_PERMISSION_REQUEST_CODES = "LifecycleHandler.permissionRequests";
    private static final String KEY_ACTIVITY_REQUEST_CODES = "LifecycleHandler.activityRequests";
    protected static final String KEY_ROUTER_STATE_PREFIX = "LifecycleHandler.routerState";

    private boolean destroyed;

    SparseArray<String> permissionRequestMap = new SparseArray<>();
    SparseArray<String> activityRequestMap = new SparseArray<>();

    final Map<Integer, ActivityHostedRouter> routerMap = new HashMap<>();
    LifecycleReceiver receiver;

    public LifecycleHandler() {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        receiver = new LifecycleReceiver(this);
    }

    private static LifecycleHandler findInActivity(Activity activity) {
        return (LifecycleHandler)activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    public static LifecycleHandler install(Activity activity) {
        LifecycleHandler lifecycleHandler = findInActivity(activity);
        if (lifecycleHandler == null) {
            lifecycleHandler = new LifecycleHandler();
            activity.getFragmentManager().beginTransaction().add(lifecycleHandler, FRAGMENT_TAG).commit();
        }
        return lifecycleHandler;
    }

    public Router getRouter(ViewGroup container, Bundle savedInstanceState) {
        ActivityHostedRouter router = routerMap.get(getRouterHashKey(container));
        if (router == null) {
            router = new ActivityHostedRouter();
            router.setHost(this, container);

            if (savedInstanceState != null) {
                Bundle routerSavedState = savedInstanceState.getBundle(KEY_ROUTER_STATE_PREFIX + router.getContainerId());
                if (routerSavedState != null) {
                    router.restoreInstanceState(routerSavedState);
                }
            }
            routerMap.put(getRouterHashKey(container), router);
        } else {
            router.setHost(this, container);
        }

        return router;
    }

    public List<Router> getRouters() {
        return new ArrayList<Router>(routerMap.values());
    }

    private static int getRouterHashKey(ViewGroup viewGroup) {
        return viewGroup.getId();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver.onActivityCreated(getActivity(), savedInstanceState);

        if (savedInstanceState != null) {
            StringSparseArrayParceler permissionParcel = savedInstanceState.getParcelable(KEY_PERMISSION_REQUEST_CODES);
            permissionRequestMap = permissionParcel != null ? permissionParcel.getStringSparseArray() : null;

            StringSparseArrayParceler activityParcel = savedInstanceState.getParcelable(KEY_ACTIVITY_REQUEST_CODES);
            activityRequestMap = activityParcel != null ? activityParcel.getStringSparseArray() : null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        receiver.onActivityStarted(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver.onActivityResumed(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        receiver.onActivityPaused(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        receiver.onActivityStopped(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        receiver.onActivitySaveInstanceState(getActivity(), outState);

        outState.putParcelable(KEY_PERMISSION_REQUEST_CODES, new StringSparseArrayParceler(permissionRequestMap));
        outState.putParcelable(KEY_ACTIVITY_REQUEST_CODES, new StringSparseArrayParceler(activityRequestMap));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        receiver.onActivityDestroyed(getActivity());
        destroyRouters();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        destroyed = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        destroyed = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        destroyRouters();
    }

    private void destroyRouters() {
        if (!destroyed) {
            destroyed = true;

            if (getActivity() != null) {
                for (Router router : routerMap.values()) {
                    router.onActivityDestroyed(getActivity());
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        receiver.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        receiver.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        for (Router router : routerMap.values()) {
            Boolean handled = router.handleRequestedPermission(permission);
            if (handled != null) {
                return handled;
            }
        }
        return super.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        receiver.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        receiver.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return receiver.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void registerForActivityResult(String instanceId, int requestCode) {
        activityRequestMap.put(requestCode, instanceId);
    }

    public void unregisterForActivityResults(String instanceId) {
        for (int i = activityRequestMap.size() - 1; i >= 0; i--) {
            if (instanceId.equals(activityRequestMap.get(activityRequestMap.keyAt(i)))) {
                activityRequestMap.removeAt(i);
            }
        }
    }

    public void startActivityForResult(String instanceId, Intent intent, int requestCode) {
        registerForActivityResult(instanceId, requestCode);
        startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(String instanceId, Intent intent, int requestCode, Bundle options) {
        registerForActivityResult(instanceId, requestCode);
        startActivityForResult(intent, requestCode, options);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String instanceId, String[] permissions, int requestCode) {
        permissionRequestMap.put(requestCode, instanceId);
        requestPermissions(permissions, requestCode);
    }


}
