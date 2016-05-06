package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Metadata used to transition between {@link Controller}s.
 */
public class ControllerTransaction {

    /**
     * All possible types of {@link Controller} changes to be used in {@link ControllerChangeHandler}s
     */
    public enum ControllerChangeType {
        /** The Controller is being pushed to the host container */
        PUSH_ENTER(true, true),

        /** The Controller is being pushed to the backstack as another Controller is pushed to the host container */
        PUSH_EXIT(true, false),

        /** The Controller is being popped from the backstack and placed in the host container as another Controller is popped */
        POP_ENTER(false, true),

        /** The Controller is being popped from the host container */
        POP_EXIT(false, false);

        public boolean isPush;
        public boolean isEnter;

        ControllerChangeType(boolean isPush, boolean isEnter) {
            this.isPush = isPush;
            this.isEnter = isEnter;
        }
    }

    private static final String KEY_VIEW_CONTROLLER_BUNDLE = "ControllerTransaction.controller.bundle";
    private static final String KEY_PUSH_TRANSITION = "ControllerTransaction.pushControllerChangeHandler";
    private static final String KEY_POP_TRANSITION = "ControllerTransaction.popControllerChangeHandler";
    private static final String KEY_TAG = "ControllerTransaction.tag";

    public final Controller controller;
    public final String tag;

    private final ControllerChangeHandler mPushControllerChangeHandler;
    private final ControllerChangeHandler mPopControllerChangeHandler;

    ControllerTransaction(Builder builder) {
        controller = builder.controller;
        tag = builder.tag;
        mPushControllerChangeHandler = builder.pushControllerChangeHandler;
        mPopControllerChangeHandler = builder.popControllerChangeHandler;
    }

    ControllerTransaction(@NonNull Bundle bundle) {
        controller = Controller.newInstance(bundle.getBundle(KEY_VIEW_CONTROLLER_BUNDLE));
        mPushControllerChangeHandler = ControllerChangeHandler.fromBundle(bundle.getBundle(KEY_PUSH_TRANSITION));
        mPopControllerChangeHandler = ControllerChangeHandler.fromBundle(bundle.getBundle(KEY_POP_TRANSITION));
        tag = bundle.getString(KEY_TAG);
    }

    public Controller getController() {
        return controller;
    }

    public String getTag() {
        return tag;
    }

    public ControllerChangeHandler getPushControllerChangeHandler() {
        ControllerChangeHandler handler = controller.getOverriddenPushHandler();
        if (handler == null) {
            handler = mPushControllerChangeHandler;
        }
        return handler;
    }

    public ControllerChangeHandler getPopControllerChangeHandler() {
        ControllerChangeHandler handler = controller.getOverriddenPopHandler();
        if (handler == null) {
            handler = mPopControllerChangeHandler;
        }
        return handler;
    }

    /**
     * Used to serialize this transaction into a Bundle
     */
    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putBundle(KEY_VIEW_CONTROLLER_BUNDLE, controller.saveInstanceState());

        if (mPushControllerChangeHandler != null) {
            bundle.putBundle(KEY_PUSH_TRANSITION, mPushControllerChangeHandler.toBundle());
        }
        if (mPopControllerChangeHandler != null) {
            bundle.putBundle(KEY_POP_TRANSITION, mPopControllerChangeHandler.toBundle());
        }

        bundle.putString(KEY_TAG, tag);

        return bundle;
    }

    /**
     * Builder used to create transactions.
     */
    public static class Builder<T extends Builder<T>> {

        final Controller controller;
        ControllerChangeHandler pushControllerChangeHandler;
        ControllerChangeHandler popControllerChangeHandler;
        String tag;

        public Builder(@NonNull Controller controller) {
            this.controller = controller;
        }

        /**
         * The {@link ControllerChangeHandler} that will be used when the {@link Controller} is pushed
         * to the screen.
         */
        @SuppressWarnings("unchecked")
        public T pushChangeHandler(ControllerChangeHandler pushControllerChangeHandler) {
            this.pushControllerChangeHandler = pushControllerChangeHandler;
            return (T)this;
        }

        /**
         * The {@link ControllerChangeHandler} that will be used when the {@link Controller} is popped
         * from the screen.
         */
        @SuppressWarnings("unchecked")
        public T popChangeHandler(ControllerChangeHandler popControllerChangeHandler) {
            this.popControllerChangeHandler = popControllerChangeHandler;
            return (T)this;
        }

        /**
         * The tag to use for this transaction. Tags can be used for finding transactions later on.
         */
        @SuppressWarnings("unchecked")
        public T tag(String tag) {
            this.tag = tag;
            return (T)this;
        }

        /**
         * Creates the transaction.
         */
        public ControllerTransaction build() {
            return new ControllerTransaction(this);
        }
    }

}
