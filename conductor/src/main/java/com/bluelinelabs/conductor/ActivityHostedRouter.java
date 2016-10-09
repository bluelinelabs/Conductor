package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.internal.LifecycleHandler;

import java.util.List;

public class ActivityHostedRouter extends Router {

    private LifecycleHandler lifecycleHandler;

    public final void setHost(@NonNull LifecycleHandler lifecycleHandler, @NonNull ViewGroup container) {
        if (this.lifecycleHandler != lifecycleHandler || this.container != container) {
            if (this.container != null && this.container instanceof ControllerChangeListener) {
                removeChangeListener((ControllerChangeListener)this.container);
            }

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            this.lifecycleHandler = lifecycleHandler;
            this.container = container;
        }
    }

    @Override
    public Activity getActivity() {
        return lifecycleHandler != null ? lifecycleHandler.getActivity() : null;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);
        lifecycleHandler = null;
    }

    @Override
    public final void invalidateOptionsMenu() {
        if (lifecycleHandler != null && lifecycleHandler.getFragmentManager() != null) {
            lifecycleHandler.getFragmentManager().invalidateOptionsMenu();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        lifecycleHandler.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    void startActivity(Intent intent) {
        lifecycleHandler.startActivity(intent);
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode) {
        lifecycleHandler.startActivityForResult(instanceId, intent, requestCode);
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode, Bundle options) {
        lifecycleHandler.startActivityForResult(instanceId, intent, requestCode, options);
    }

    @Override
    void registerForActivityResult(String instanceId, int requestCode) {
        lifecycleHandler.registerForActivityResult(instanceId, requestCode);
    }

    @Override
    void unregisterForActivityResults(String instanceId) {
        lifecycleHandler.unregisterForActivityResults(instanceId);
    }

    @Override
    void requestPermissions(String instanceId, @NonNull String[] permissions, int requestCode) {
        lifecycleHandler.requestPermissions(instanceId, permissions, requestCode);
    }

    @Override
    boolean hasHost() {
        return lifecycleHandler != null;
    }

    @Override
    List<Router> getSiblingRouters() {
        return lifecycleHandler.getRouters();
    }

    @Override
    Router getRootRouter() {
        return this;
    }
}
