package com.bluelinelabs.conductor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.internal.TransactionIndexer;

/**
 * Metadata used for adding {@link Controller}s to a {@link Router}.
 */
public class RouterTransaction {

    private static int INVALID_INDEX = -1;

    private static final String KEY_VIEW_CONTROLLER_BUNDLE = "RouterTransaction.controller.bundle";
    private static final String KEY_PUSH_TRANSITION = "RouterTransaction.pushControllerChangeHandler";
    private static final String KEY_POP_TRANSITION = "RouterTransaction.popControllerChangeHandler";
    private static final String KEY_TAG = "RouterTransaction.tag";
    private static final String KEY_INDEX = "RouterTransaction.transactionIndex";
    private static final String KEY_ATTACHED_TO_ROUTER = "RouterTransaction.attachedToRouter";

    @NonNull final Controller controller;
    private String tag;

    private ControllerChangeHandler pushControllerChangeHandler;
    private ControllerChangeHandler popControllerChangeHandler;
    private boolean attachedToRouter;
    int transactionIndex = INVALID_INDEX;

    @NonNull
    public static RouterTransaction with(@NonNull Controller controller) {
        return new RouterTransaction(controller);
    }

    private RouterTransaction(@NonNull Controller controller) {
        this.controller = controller;
    }

    RouterTransaction(@NonNull Bundle bundle) {
        controller = Controller.newInstance(bundle.getBundle(KEY_VIEW_CONTROLLER_BUNDLE));
        pushControllerChangeHandler = ControllerChangeHandler.fromBundle(bundle.getBundle(KEY_PUSH_TRANSITION));
        popControllerChangeHandler = ControllerChangeHandler.fromBundle(bundle.getBundle(KEY_POP_TRANSITION));
        tag = bundle.getString(KEY_TAG);
        transactionIndex = bundle.getInt(KEY_INDEX);
        attachedToRouter = bundle.getBoolean(KEY_ATTACHED_TO_ROUTER);
    }

    void onAttachedToRouter() {
        attachedToRouter = true;
    }

    @NonNull
    public Controller controller() {
        return controller;
    }

    @Nullable
    public String tag() {
        return tag;
    }

    @NonNull
    public RouterTransaction tag(@Nullable String tag) {
        if (!attachedToRouter) {
            this.tag = tag;
            return this;
        } else {
            throw new RuntimeException(getClass().getSimpleName() + "s can not be modified after being added to a Router.");
        }
    }

    @Nullable
    public ControllerChangeHandler pushChangeHandler() {
        ControllerChangeHandler handler = controller.getOverriddenPushHandler();
        if (handler == null) {
            handler = pushControllerChangeHandler;
        }
        return handler;
    }

    @NonNull
    public RouterTransaction pushChangeHandler(@Nullable ControllerChangeHandler handler) {
        if (!attachedToRouter) {
            pushControllerChangeHandler = handler;
            return this;
        } else {
            throw new RuntimeException(getClass().getSimpleName() + "s can not be modified after being added to a Router.");
        }
    }

    @Nullable
    public ControllerChangeHandler popChangeHandler() {
        ControllerChangeHandler handler = controller.getOverriddenPopHandler();
        if (handler == null) {
            handler = popControllerChangeHandler;
        }
        return handler;
    }

    @NonNull
    public RouterTransaction popChangeHandler(@Nullable ControllerChangeHandler handler) {
        if (!attachedToRouter) {
            popControllerChangeHandler = handler;
            return this;
        } else {
            throw new RuntimeException(getClass().getSimpleName() + "s can not be modified after being added to a Router.");
        }
    }

    void ensureValidIndex(@NonNull TransactionIndexer indexer) {
        if (transactionIndex == INVALID_INDEX) {
            transactionIndex = indexer.nextIndex();
        }
    }

    /**
     * Used to serialize this transaction into a Bundle
     */
    @NonNull
    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putBundle(KEY_VIEW_CONTROLLER_BUNDLE, controller.saveInstanceState());

        if (pushControllerChangeHandler != null) {
            bundle.putBundle(KEY_PUSH_TRANSITION, pushControllerChangeHandler.toBundle());
        }
        if (popControllerChangeHandler != null) {
            bundle.putBundle(KEY_POP_TRANSITION, popControllerChangeHandler.toBundle());
        }

        bundle.putString(KEY_TAG, tag);
        bundle.putInt(KEY_INDEX, transactionIndex);
        bundle.putBoolean(KEY_ATTACHED_TO_ROUTER, attachedToRouter);

        return bundle;
    }

}
