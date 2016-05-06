package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;

/**
 * A {@link ControllerTransaction} implementation used for adding child {@link Controller}s.
 */
public class ChildControllerTransaction extends ControllerTransaction {

    private static final String KEY_CONTAINER_ID = "ChildControllerTransaction.containerId";
    private static final String KEY_ADD_TO_LOCAL_BACKSTACK = "ChildControllerTransaction.addToLocalBackstack";

    /** The ID of the ViewGroup that the child {@link Controller} will be added to */
    public final int containerId;

    /** If true, the hosting {@link Controller} will be responsible for reversing this transaction if the user presses the back button */
    public final boolean addToLocalBackstack;

    ChildControllerTransaction(Builder builder) {
        super(builder);
        containerId = builder.containerId;
        addToLocalBackstack = builder.addToLocalBackstack;
    }

    ChildControllerTransaction(@NonNull Bundle bundle) {
        super(bundle);
        containerId = bundle.getInt(KEY_CONTAINER_ID);
        addToLocalBackstack = bundle.getBoolean(KEY_ADD_TO_LOCAL_BACKSTACK);
    }

    @Override
    public Bundle saveInstanceState() {
        Bundle bundle = super.saveInstanceState();
        bundle.putInt(KEY_CONTAINER_ID, containerId);
        bundle.putBoolean(KEY_ADD_TO_LOCAL_BACKSTACK, addToLocalBackstack);
        return bundle;
    }

    /**
     * Creates a new Builder
     *
     * @param controller The Controller to add as a child
     * @param containerId The ID of the ViewGroup to which the controller's view should be added
     */
    public static Builder builder(@NonNull Controller controller, @IdRes int containerId) {
        return new Builder(controller, containerId);
    }

    /**
     * A {@link ControllerTransaction.Builder} implementation used for adding child {@link Controller}s.
     */
    public static class Builder extends ControllerTransaction.Builder<Builder> {

        @IdRes final int containerId;

        boolean addToLocalBackstack;

        Builder(@NonNull Controller controller, @IdRes int containerId) {
            super(controller);
            this.containerId = containerId;
        }

        /**
         * If true, the hosting {@link Controller} will be responsible for reversing this transaction if the user presses the back button.
         */
        public Builder addToLocalBackstack(boolean addToLocalBackstack) {
            this.addToLocalBackstack = addToLocalBackstack;
            return this;
        }

        /** Creates the transaction */
        public ChildControllerTransaction build() {
            return new ChildControllerTransaction(this);
        }

    }
}
