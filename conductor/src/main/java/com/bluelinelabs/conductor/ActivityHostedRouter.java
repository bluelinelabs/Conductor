package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.internal.LifecycleHandler;
import com.bluelinelabs.conductor.internal.TransactionIndexer;

import java.util.List;

public class ActivityHostedRouter extends Router {

    private LifecycleHandler lifecycleHandler;
    private final TransactionIndexer transactionIndexer = new TransactionIndexer();

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

            watchContainerAttach();
        }
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        super.saveInstanceState(outState);

        transactionIndexer.saveInstanceState(outState);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        transactionIndexer.restoreInstanceState(savedInstanceState);
    }

    @Override @Nullable
    public Activity getActivity() {
        return lifecycleHandler != null ? lifecycleHandler.getLifecycleActivity() : null;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        lifecycleHandler.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    void startActivity(@NonNull Intent intent) {
        lifecycleHandler.startActivity(intent);
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode) {
        lifecycleHandler.startActivityForResult(instanceId, intent, requestCode);
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options) {
        lifecycleHandler.startActivityForResult(instanceId, intent, requestCode, options);
    }

    @Override
    void startIntentSenderForResult(@NonNull String instanceId, @NonNull IntentSender intent, int requestCode, @Nullable Intent fillInIntent,
                                    int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws SendIntentException {
        lifecycleHandler.startIntentSenderForResult(instanceId, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    void registerForActivityResult(@NonNull String instanceId, int requestCode) {
        lifecycleHandler.registerForActivityResult(instanceId, requestCode);
    }

    @Override
    void unregisterForActivityResults(@NonNull String instanceId) {
        lifecycleHandler.unregisterForActivityResults(instanceId);
    }

    @Override
    void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode) {
        lifecycleHandler.requestPermissions(instanceId, permissions, requestCode);
    }

    @Override
    boolean hasHost() {
        return lifecycleHandler != null;
    }

    @Override @NonNull
    List<Router> getSiblingRouters() {
        return lifecycleHandler.getRouters();
    }

    @Override @NonNull
    Router getRootRouter() {
        return this;
    }

    @Override @NonNull
    TransactionIndexer getTransactionIndexer() {
        return transactionIndexer;
    }

    @Override
    public void onContextAvailable() {
        super.onContextAvailable();
    }

}
