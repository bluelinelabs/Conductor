package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.internal.TransactionIndexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class ControllerHostedRouter extends Router {

    private final String KEY_HOST_ID = "ControllerHostedRouter.hostId";
    private final String KEY_TAG = "ControllerHostedRouter.tag";
    private final String KEY_BOUND_TO_CONTAINER = "ControllerHostedRouter.boundToContainer";

    private Controller hostController;

    @IdRes private int hostId;
    private String tag;
    private boolean isDetachFrozen;
    private boolean boundToContainer;

    ControllerHostedRouter() { }

    ControllerHostedRouter(int hostId, @Nullable String tag, boolean boundToContainer) {
        if (!boundToContainer && tag == null) {
            throw new IllegalStateException("ControllerHostedRouter can't be created without a tag if not bounded to its container");
        }
        this.hostId = hostId;
        this.tag = tag;
        this.boundToContainer = boundToContainer;
    }

    final void setHostController(@NonNull Controller controller) {
        if (hostController == null) {
            hostController = controller;
        }
    }

    final void setHostContainer(@NonNull Controller controller, @NonNull ViewGroup container) {
        if (hostController != controller || this.container != container) {
            removeHost();

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener) container);
            }

            hostController = controller;
            this.container = container;

            for (RouterTransaction transaction : backstack) {
                transaction.controller().setParentController(controller);
            }

            watchContainerAttach();
        }
    }

    final void removeHost() {
        if (container != null && container instanceof ControllerChangeListener) {
            removeChangeListener((ControllerChangeListener) container);
        }

        final List<Controller> controllersToDestroy = new ArrayList<>(destroyingControllers);
        for (Controller controller : controllersToDestroy) {
            if (controller.getView() != null) {
                controller.detach(controller.getView(), true, false);
            }
        }
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller().getView() != null) {
                transaction.controller().detach(transaction.controller().getView(), true, false);
            }
        }

        prepareForContainerRemoval();
        container = null;
    }

    final void setDetachFrozen(boolean frozen) {
        isDetachFrozen = frozen;
        for (RouterTransaction transaction : backstack) {
            transaction.controller().setDetachFrozen(frozen);
        }
    }

    @Override
    void destroy(boolean popViews) {
        setDetachFrozen(false);
        super.destroy(popViews);
    }

    @Override
    protected void pushToBackstack(@NonNull RouterTransaction entry) {
        if (isDetachFrozen) {
            entry.controller().setDetachFrozen(true);
        }
        super.pushToBackstack(entry);
    }

    @Override
    public void setBackstack(@NonNull List<RouterTransaction> newBackstack, @Nullable ControllerChangeHandler changeHandler) {
        if (isDetachFrozen) {
            for (RouterTransaction transaction : newBackstack) {
                transaction.controller().setDetachFrozen(true);
            }
        }
        super.setBackstack(newBackstack, changeHandler);
    }

    @Override
    void performControllerChange(@Nullable RouterTransaction to, @Nullable RouterTransaction from, boolean isPush) {
        super.performControllerChange(to, from, isPush);

        // If we're pushing a transaction that will detach controllers to an unattached child
        // router, we need mark all other controllers as NOT needing to be reattached.
        if (to != null && !hostController.isAttached()) {
            if (to.pushChangeHandler() == null || to.pushChangeHandler().removesFromViewOnPush()) {
                for (RouterTransaction transaction : backstack) {
                    transaction.controller().setNeedsAttach(false);
                }
            }
        }
    }

    @Override @Nullable
    public Activity getActivity() {
        return hostController != null ? hostController.getActivity() : null;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity, boolean isConfigurationChange) {
        super.onActivityDestroyed(activity, isConfigurationChange);

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
    void startIntentSenderForResult(@NonNull String instanceId, @NonNull IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws SendIntentException {
        if (hostController != null && hostController.getRouter() != null) {
            hostController.getRouter().startIntentSenderForResult(instanceId, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
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
        return hostController != null && container != null;
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt(KEY_HOST_ID, hostId);
        outState.putBoolean(KEY_BOUND_TO_CONTAINER, boundToContainer);
        outState.putString(KEY_TAG, tag);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        hostId = savedInstanceState.getInt(KEY_HOST_ID);
        boundToContainer = savedInstanceState.getBoolean(KEY_BOUND_TO_CONTAINER);
        tag = savedInstanceState.getString(KEY_TAG);
    }

    @Override
    void setRouterOnController(@NonNull Controller controller) {
        controller.setParentController(hostController);
        super.setRouterOnController(controller);
    }

    int getHostId() {
        return hostId;
    }

    boolean matches(int hostId, @Nullable String tag) {
        if (!boundToContainer && container == null) {
            if (this.tag == null) {
                throw new IllegalStateException("Host ID can't be variable with a null tag");
            }
            if (this.tag.equals(tag)) {
                this.hostId = hostId;
                return true;
            }
        }

        return this.hostId == hostId && TextUtils.equals(tag, this.tag);
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

    @Override @NonNull
    TransactionIndexer getTransactionIndexer() {
        Router rootRouter = getRootRouter();
        if (rootRouter == this) {
            String debugInfo;
            if (hostController != null) {
                debugInfo = String.format(Locale.ENGLISH, "%s (attached? %b, destroyed? %b, parent: %s)",
                        hostController.getClass().getSimpleName(), hostController.isAttached(), hostController.isBeingDestroyed, hostController.getParentController());
            } else {
                debugInfo = "null host controller";
            }
            throw new IllegalStateException("Unable to retrieve TransactionIndexer from " + debugInfo);
        } else {
            return getRootRouter().getTransactionIndexer();
        }
    }

}
