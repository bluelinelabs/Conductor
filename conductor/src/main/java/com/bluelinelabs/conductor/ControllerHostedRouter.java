package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.internal.TransactionIndexer;

import java.util.ArrayList;
import java.util.List;

class ControllerHostedRouter extends Router {

    private final String KEY_HOST_ID = "ControllerHostedRouter.hostId";
    private final String KEY_TAG = "ControllerHostedRouter.tag";

    private Controller hostController;

    @IdRes private int hostId;
    private String tag;

    ControllerHostedRouter() { }

    ControllerHostedRouter(int hostId, @Nullable String tag) {
        this.hostId = hostId;
        this.tag = tag;
    }

    final void setHost(@NonNull Controller controller, @NonNull ViewGroup container) {
        if (hostController != controller || this.container != container) {
            removeHost();

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            hostController = controller;
            this.container = container;
        }
    }

    final void removeHost() {
        if (container != null && container instanceof ControllerChangeListener) {
            removeChangeListener((ControllerChangeListener)container);
        }

        final List<Controller> controllersToDestroy = new ArrayList<>(destroyingControllers);
        for (Controller controller : controllersToDestroy) {
            if (controller.getView() != null) {
                controller.detach(controller.getView(), true, false);
            }
        }
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller.getView() != null) {
                transaction.controller.detach(transaction.controller.getView(), true, false);
            }
        }

        prepareForContainerRemoval();
        hostController = null;
        container = null;
    }

    final void setDetachFrozen(boolean frozen) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.setDetachFrozen(frozen);
        }
    }

    @Override
    void destroy(boolean popViews) {
        setDetachFrozen(false);
        super.destroy(popViews);
    }

    @Override @Nullable
    public Activity getActivity() {
        return hostController != null ? hostController.getActivity() : null;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        super.onActivityDestroyed(activity);

        removeHost();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void invalidateOptionsMenu() {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().invalidateOptionsMenu();
        }
    }

    @Override
    void startActivity(@NonNull Intent intent) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivity(intent);
        }
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivityForResult(instanceId, intent, requestCode);
        }
    }

    @Override
    void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startActivityForResult(instanceId, intent, requestCode, options);
        }
    }

    @Override
    void registerForActivityResult(@NonNull String instanceId, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().registerForActivityResult(instanceId, requestCode);
        }
    }

    @Override
    void unregisterForActivityResults(@NonNull String instanceId) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().unregisterForActivityResults(instanceId);
        }
    }

    @Override
    void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode) {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().requestPermissions(instanceId, permissions, requestCode);
        }
    }

    @Override
    boolean hasHost() {
        return hostController != null;
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt(KEY_HOST_ID, hostId);
        outState.putString(KEY_TAG, tag);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        hostId = savedInstanceState.getInt(KEY_HOST_ID);
        tag = savedInstanceState.getString(KEY_TAG);
    }

    @Override
    void setControllerRouter(@NonNull Controller controller) {
        super.setControllerRouter(controller);
        controller.setParentController(hostController);
    }

    int getHostId() {
        return hostId;
    }

    @Nullable
    String getTag() {
        return tag;
    }

    @Override @NonNull
    List<Router> getSiblingRouters() {
        List<Router> list = new ArrayList<>();
        list.addAll(hostController.getChildRouters());
        list.addAll(hostController.getRouter().getSiblingRouters());
        return list;
    }

    @Override @NonNull
    Router getRootRouter() {
        if (hostController != null && hostController.getRouter() != null) {
            return hostController.getRouter().getRootRouter();
        } else {
            return this;
        }
    }

    @Override @Nullable
    TransactionIndexer getTransactionIndexer() {
        return getRootRouter().getTransactionIndexer();
    }
}
