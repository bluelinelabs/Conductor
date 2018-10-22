package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.bluelinelabs.conductor.ControllerChangeHandler.ChangeTransaction;
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.internal.NoOpControllerChangeHandler;
import com.bluelinelabs.conductor.internal.ThreadUtils;
import com.bluelinelabs.conductor.internal.TransactionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A Router implements navigation and backstack handling for {@link Controller}s. Router objects are attached
 * to Activity/containing ViewGroup pairs. Routers do not directly render or push Views to the container ViewGroup,
 * but instead defer this responsibility to the {@link ControllerChangeHandler} specified in a given transaction.
 */
public abstract class Router {

    private static final String KEY_BACKSTACK = "Router.backstack";
    private static final String KEY_POPS_LAST_VIEW = "Router.popsLastView";

    final Backstack backstack = new Backstack();
    private final List<ControllerChangeListener> changeListeners = new ArrayList<>();
    private final List<ChangeTransaction> pendingControllerChanges = new ArrayList<>();
    final List<Controller> destroyingControllers = new ArrayList<>();

    private boolean popsLastView = false;
    boolean containerFullyAttached = false;
    boolean isActivityStopped = false;

    ViewGroup container;

    /**
     * Returns this Router's host Activity or {@code null} if it has either not yet been attached to
     * an Activity or if the Activity has been destroyed.
     */
    @Nullable
    public abstract Activity getActivity();

    /**
     * This should be called by the host Activity when its onActivityResult method is called if the instanceId
     * of the controller that called startActivityForResult is not known.
     *
     * @param requestCode The Activity's onActivityResult requestCode
     * @param resultCode  The Activity's onActivityResult resultCode
     * @param data        The Activity's onActivityResult data
     */
    public abstract void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    /**
     * This should be called by the host Activity when its onRequestPermissionsResult method is called. The call will be forwarded
     * to the {@link Controller} with the instanceId passed in.
     *
     * @param instanceId   The instanceId of the Controller to which this result should be forwarded
     * @param requestCode  The Activity's onRequestPermissionsResult requestCode
     * @param permissions  The Activity's onRequestPermissionsResult permissions
     * @param grantResults The Activity's onRequestPermissionsResult grantResults
     */
    public void onRequestPermissionsResult(@NonNull String instanceId, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Controller controller = getControllerWithInstanceId(instanceId);
        if (controller != null) {
            controller.requestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * This should be called by the host Activity when its onBackPressed method is called. The call will be forwarded
     * to its top {@link Controller}. If that controller doesn't handle it, then it will be popped.
     *
     * @return Whether or not a back action was handled by the Router
     */
    @UiThread
    public boolean handleBack() {
        ThreadUtils.ensureMainThread();

        if (!backstack.isEmpty()) {
            //noinspection ConstantConditions
            if (backstack.peek().controller.handleBack()) {
                return true;
            } else if (popCurrentController()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Pops the top {@link Controller} from the backstack
     *
     * @return Whether or not this Router still has controllers remaining on it after popping.
     */
    @SuppressWarnings("WeakerAccess")
    @UiThread
    public boolean popCurrentController() {
        ThreadUtils.ensureMainThread();

        RouterTransaction transaction = backstack.peek();
        if (transaction == null) {
            throw new IllegalStateException("Trying to pop the current controller when there are none on the backstack.");
        }
        return popController(transaction.controller);
    }

    /**
     * Pops the passed {@link Controller} from the backstack
     *
     * @param controller The controller that should be popped from this Router
     * @return Whether or not this Router still has controllers remaining on it after popping.
     */
    @UiThread
    public boolean popController(@NonNull Controller controller) {
        ThreadUtils.ensureMainThread();

        RouterTransaction topTransaction = backstack.peek();
        boolean poppingTopController = topTransaction != null && topTransaction.controller == controller;

        if (poppingTopController) {
            trackDestroyingController(backstack.pop());
            performControllerChange(backstack.peek(), topTransaction, false);
        } else {
            RouterTransaction removedTransaction = null;
            RouterTransaction nextTransaction = null;
            Iterator<RouterTransaction> iterator = backstack.iterator();
            ControllerChangeHandler topPushHandler = topTransaction != null ? topTransaction.pushChangeHandler() : null;
            final boolean needsNextTransactionAttach = topPushHandler != null ? !topPushHandler.removesFromViewOnPush() : false;

            while (iterator.hasNext()) {
                RouterTransaction transaction = iterator.next();
                if (transaction.controller == controller) {
                    if (controller.isAttached()) {
                        trackDestroyingController(transaction);
                    }
                    iterator.remove();
                    removedTransaction = transaction;
                } else if (removedTransaction != null) {
                    if (needsNextTransactionAttach && !transaction.controller.isAttached()) {
                        nextTransaction = transaction;
                    }
                    break;
                }
            }

            if (removedTransaction != null) {
                performControllerChange(nextTransaction, removedTransaction, false);
            }
        }

        if (popsLastView) {
            return topTransaction != null;
        } else {
            return !backstack.isEmpty();
        }
    }

    /**
     * Pushes a new {@link Controller} to the backstack
     *
     * @param transaction The transaction detailing what should be pushed, including the {@link Controller},
     *                    and its push and pop {@link ControllerChangeHandler}, and its tag.
     */
    @UiThread
    public void pushController(@NonNull RouterTransaction transaction) {
        ThreadUtils.ensureMainThread();

        RouterTransaction from = backstack.peek();
        pushToBackstack(transaction);
        performControllerChange(transaction, from, true);
    }

    /**
     * Replaces this Router's top {@link Controller} with a new {@link Controller}
     *
     * @param transaction The transaction detailing what should be pushed, including the {@link Controller},
     *                    and its push and pop {@link ControllerChangeHandler}, and its tag.
     */
    @SuppressWarnings("WeakerAccess")
    @UiThread
    public void replaceTopController(@NonNull RouterTransaction transaction) {
        ThreadUtils.ensureMainThread();

        RouterTransaction topTransaction = backstack.peek();
        if (!backstack.isEmpty()) {
            trackDestroyingController(backstack.pop());
        }

        final ControllerChangeHandler handler = transaction.pushChangeHandler();
        if (topTransaction != null) {
            //noinspection ConstantConditions
            final boolean oldHandlerRemovedViews = topTransaction.pushChangeHandler() == null || topTransaction.pushChangeHandler().removesFromViewOnPush();
            final boolean newHandlerRemovesViews = handler == null || handler.removesFromViewOnPush();
            if (!oldHandlerRemovedViews && newHandlerRemovesViews) {
                for (RouterTransaction visibleTransaction : getVisibleTransactions(backstack.iterator())) {
                    performControllerChange(null, visibleTransaction, true, handler);
                }
            }
        }

        pushToBackstack(transaction);

        if (handler != null) {
            handler.setForceRemoveViewOnPush(true);
        }
        performControllerChange(transaction.pushChangeHandler(handler), topTransaction, true);
    }

    void destroy(boolean popViews) {
        popsLastView = true;
        final List<RouterTransaction> poppedControllers = backstack.popAll();
        trackDestroyingControllers(poppedControllers);

        if (popViews && poppedControllers.size() > 0) {
            RouterTransaction topTransaction = poppedControllers.get(0);
            topTransaction.controller().addLifecycleListener(new LifecycleListener() {
                @Override
                public void onChangeEnd(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
                    if (changeType == ControllerChangeType.POP_EXIT) {
                        for (int i = poppedControllers.size() - 1; i > 0; i--) {
                            RouterTransaction transaction = poppedControllers.get(i);
                            performControllerChange(null, transaction, true, new SimpleSwapChangeHandler());
                        }
                    }
                }
            });

            performControllerChange(null, topTransaction, false, topTransaction.popChangeHandler());
        }
    }

    public int getContainerId() {
        return container != null ? container.getId() : 0;
    }

    /**
     * If set to true, this router will handle back presses by performing a change handler on the last controller and view
     * in the stack. This defaults to false so that the developer can either finish its containing Activity or otherwise
     * hide its parent view without any strange artifacting.
     */
    @NonNull
    public Router setPopsLastView(boolean popsLastView) {
        this.popsLastView = popsLastView;
        return this;
    }

    /**
     * Pops all {@link Controller}s until only the root is left
     *
     * @return Whether or not any {@link Controller}s were popped in order to get to the root transaction
     */
    @UiThread
    public boolean popToRoot() {
        ThreadUtils.ensureMainThread();

        return popToRoot(null);
    }

    /**
     * Pops all {@link Controller} until only the root is left
     *
     * @param changeHandler The {@link ControllerChangeHandler} to handle this transaction
     * @return Whether or not any {@link Controller}s were popped in order to get to the root transaction
     */
    @SuppressWarnings("WeakerAccess")
    @UiThread
    public boolean popToRoot(@Nullable ControllerChangeHandler changeHandler) {
        ThreadUtils.ensureMainThread();

        if (backstack.size() > 1) {
            //noinspection ConstantConditions
            popToTransaction(backstack.root(), changeHandler);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pops all {@link Controller}s until the Controller with the passed tag is at the top
     *
     * @param tag The tag being popped to
     * @return Whether or not any {@link Controller}s were popped in order to get to the transaction with the passed tag
     */
    @UiThread
    public boolean popToTag(@NonNull String tag) {
        ThreadUtils.ensureMainThread();

        return popToTag(tag, null);
    }

    /**
     * Pops all {@link Controller}s until the {@link Controller} with the passed tag is at the top
     *
     * @param tag           The tag being popped to
     * @param changeHandler The {@link ControllerChangeHandler} to handle this transaction
     * @return Whether or not the {@link Controller} with the passed tag is now at the top
     */
    @SuppressWarnings("WeakerAccess")
    @UiThread
    public boolean popToTag(@NonNull String tag, @Nullable ControllerChangeHandler changeHandler) {
        ThreadUtils.ensureMainThread();

        for (RouterTransaction transaction : backstack) {
            if (tag.equals(transaction.tag())) {
                popToTransaction(transaction, changeHandler);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the root Controller. If any {@link Controller}s are currently in the backstack, they will be removed.
     *
     * @param transaction The transaction detailing what should be pushed, including the {@link Controller},
     *                    and its push and pop {@link ControllerChangeHandler}, and its tag.
     */
    @UiThread
    public void setRoot(@NonNull RouterTransaction transaction) {
        ThreadUtils.ensureMainThread();

        List<RouterTransaction> transactions = Collections.singletonList(transaction);
        setBackstack(transactions, transaction.pushChangeHandler());
    }

    /**
     * Returns the hosted Controller with the given instance id or {@code null} if no such
     * Controller exists in this Router.
     *
     * @param instanceId The instance ID being searched for
     */
    @Nullable
    public Controller getControllerWithInstanceId(@NonNull String instanceId) {
        for (RouterTransaction transaction : backstack) {
            Controller controllerWithId = transaction.controller.findController(instanceId);
            if (controllerWithId != null) {
                return controllerWithId;
            }
        }
        return null;
    }

    /**
     * Returns the hosted Controller that was pushed with the given tag or {@code null} if no
     * such Controller exists in this Router.
     *
     * @param tag The tag being searched for
     */
    @Nullable
    public Controller getControllerWithTag(@NonNull String tag) {
        for (RouterTransaction transaction : backstack) {
            if (tag.equals(transaction.tag())) {
                return transaction.controller;
            }
        }
        return null;
    }

    /**
     * Returns the number of {@link Controller}s currently in the backstack
     */
    @SuppressWarnings("WeakerAccess")
    public int getBackstackSize() {
        return backstack.size();
    }

    /**
     * Returns the current backstack, ordered from root to most recently pushed.
     */
    @NonNull
    public List<RouterTransaction> getBackstack() {
        List<RouterTransaction> list = new ArrayList<>(backstack.size());
        Iterator<RouterTransaction> backstackIterator = backstack.reverseIterator();
        while (backstackIterator.hasNext()) {
            list.add(backstackIterator.next());
        }
        return list;
    }

    /**
     * Sets the backstack, transitioning from the current top controller to the top of the new stack (if different)
     * using the passed {@link ControllerChangeHandler}
     *
     * @param newBackstack  The new backstack
     * @param changeHandler An optional change handler to be used to handle the root view of transition
     */
    @SuppressWarnings("WeakerAccess")
    @UiThread
    public void setBackstack(@NonNull List<RouterTransaction> newBackstack, @Nullable ControllerChangeHandler changeHandler) {
        ThreadUtils.ensureMainThread();

        List<RouterTransaction> oldTransactions = getBackstack();
        List<RouterTransaction> oldVisibleTransactions = getVisibleTransactions(backstack.iterator());

        removeAllExceptVisibleAndUnowned();
        ensureOrderedTransactionIndices(newBackstack);
        ensureNoDuplicateControllers(newBackstack);

        backstack.setBackstack(newBackstack);

        List<RouterTransaction> transactionsToBeRemoved = new ArrayList<>();
        for (RouterTransaction oldTransaction : oldTransactions) {
            boolean contains = false;
            for (RouterTransaction newTransaction : newBackstack) {
                if (oldTransaction.controller == newTransaction.controller) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                // Inform the controller that it will be destroyed soon
                oldTransaction.controller.isBeingDestroyed = true;
                transactionsToBeRemoved.add(oldTransaction);
            }
        }

        // Ensure all new controllers have a valid router set
        Iterator<RouterTransaction> backstackIterator = backstack.reverseIterator();
        while (backstackIterator.hasNext()) {
            RouterTransaction transaction = backstackIterator.next();
            transaction.onAttachedToRouter();
            setControllerRouter(transaction.controller);
        }

        if (newBackstack.size() > 0) {
            List<RouterTransaction> reverseNewBackstack = new ArrayList<>(newBackstack);
            Collections.reverse(reverseNewBackstack);
            List<RouterTransaction> newVisibleTransactions = getVisibleTransactions(reverseNewBackstack.iterator());
            boolean newRootRequiresPush = !(newVisibleTransactions.size() > 0 && oldTransactions.contains(newVisibleTransactions.get(0)));

            boolean visibleTransactionsChanged = !backstacksAreEqual(newVisibleTransactions, oldVisibleTransactions);
            if (visibleTransactionsChanged) {
                RouterTransaction oldRootTransaction = oldVisibleTransactions.size() > 0 ? oldVisibleTransactions.get(0) : null;
                RouterTransaction newRootTransaction = newVisibleTransactions.get(0);

                // Replace the old root with the new one
                if (oldRootTransaction == null || oldRootTransaction.controller != newRootTransaction.controller) {
                    // Ensure the existing root controller is fully pushed to the view hierarchy
                    if (oldRootTransaction != null) {
                        ControllerChangeHandler.completeHandlerImmediately(oldRootTransaction.controller.getInstanceId());
                    }
                    performControllerChange(newRootTransaction, oldRootTransaction, newRootRequiresPush, changeHandler);
                }

                // Remove all visible controllers that were previously on the backstack
                for (int i = oldVisibleTransactions.size() - 1; i > 0; i--) {
                    RouterTransaction transaction = oldVisibleTransactions.get(i);
                    if (!newVisibleTransactions.contains(transaction)) {
                        ControllerChangeHandler localHandler = changeHandler != null ? changeHandler.copy() : new SimpleSwapChangeHandler();
                        localHandler.setForceRemoveViewOnPush(true);
                        ControllerChangeHandler.completeHandlerImmediately(transaction.controller.getInstanceId());
                        performControllerChange(null, transaction, newRootRequiresPush, localHandler);
                    }
                }

                // Add any new controllers to the backstack
                for (int i = 1; i < newVisibleTransactions.size(); i++) {
                    RouterTransaction transaction = newVisibleTransactions.get(i);
                    if (!oldVisibleTransactions.contains(transaction)) {
                        performControllerChange(transaction, newVisibleTransactions.get(i - 1), true, transaction.pushChangeHandler());
                    }
                }
            }

        } else {
            // Remove all visible controllers that were previously on the backstack
            for (int i = oldVisibleTransactions.size() - 1; i >= 0; i--) {
                RouterTransaction transaction = oldVisibleTransactions.get(i);
                ControllerChangeHandler localHandler = changeHandler != null ? changeHandler.copy() : new SimpleSwapChangeHandler();
                ControllerChangeHandler.completeHandlerImmediately(transaction.controller.getInstanceId());
                performControllerChange(null, transaction, false, localHandler);
            }
        }

        // Destroy all old controllers that are no longer on the backstack. We don't do this when we initially
        // set the backstack to prevent the possibility that they'll be destroyed before the controller
        // change handler runs.
        for (RouterTransaction removedTransaction : transactionsToBeRemoved) {
            removedTransaction.controller.destroy();
        }
    }

    /**
     * Returns whether or not this Router has a root {@link Controller}
     */
    public boolean hasRootController() {
        return getBackstackSize() > 0;
    }

    /**
     * Adds a listener for all of this Router's {@link Controller} change events
     *
     * @param changeListener The listener
     */
    @SuppressWarnings("WeakerAccess")
    public void addChangeListener(@NonNull ControllerChangeListener changeListener) {
        if (!changeListeners.contains(changeListener)) {
            changeListeners.add(changeListener);
        }
    }

    /**
     * Removes a previously added listener
     *
     * @param changeListener The listener to be removed
     */
    @SuppressWarnings("WeakerAccess")
    public void removeChangeListener(@NonNull ControllerChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    /**
     * Attaches this Router's existing backstack to its container if one exists.
     */
    @UiThread
    public void rebindIfNeeded() {
        ThreadUtils.ensureMainThread();

        Iterator<RouterTransaction> backstackIterator = backstack.reverseIterator();
        while (backstackIterator.hasNext()) {
            RouterTransaction transaction = backstackIterator.next();

            if (transaction.controller.getNeedsAttach()) {
                performControllerChange(transaction, null, true, new SimpleSwapChangeHandler(false));
            }
        }
    }

    public final void onActivityResult(@NonNull String instanceId, int requestCode, int resultCode, @Nullable Intent data) {
        Controller controller = getControllerWithInstanceId(instanceId);
        if (controller != null) {
            controller.onActivityResult(requestCode, resultCode, data);
        }
    }

    public final void onActivityStarted(@NonNull Activity activity) {
        isActivityStopped = false;

        for (RouterTransaction transaction : backstack) {
            transaction.controller.activityStarted(activity);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onActivityStarted(activity);
            }
        }
    }

    public final void onActivityResumed(@NonNull Activity activity) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.activityResumed(activity);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onActivityResumed(activity);
            }
        }
    }

    public final void onActivityPaused(@NonNull Activity activity) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.activityPaused(activity);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onActivityPaused(activity);
            }
        }
    }

    public final void onActivityStopped(@NonNull Activity activity) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.activityStopped(activity);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onActivityStopped(activity);
            }
        }

        isActivityStopped = true;
    }

    public void onActivityDestroyed(@NonNull Activity activity) {
        prepareForContainerRemoval();
        changeListeners.clear();

        for (RouterTransaction transaction : backstack) {
            transaction.controller.activityDestroyed(activity);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onActivityDestroyed(activity);
            }
        }

        for (int index = destroyingControllers.size() - 1; index >= 0; index--) {
            Controller controller = destroyingControllers.get(index);
            controller.activityDestroyed(activity);

            for (Router childRouter : controller.getChildRouters()) {
                childRouter.onActivityDestroyed(activity);
            }
        }

        container = null;
    }

    public void prepareForHostDetach() {
        for (RouterTransaction transaction : backstack) {
            if (ControllerChangeHandler.completeHandlerImmediately(transaction.controller.getInstanceId())) {
                transaction.controller.setNeedsAttach(true);
            }
            transaction.controller.prepareForHostDetach();
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        Bundle backstackState = new Bundle();
        backstack.saveInstanceState(backstackState);

        outState.putParcelable(KEY_BACKSTACK, backstackState);
        outState.putBoolean(KEY_POPS_LAST_VIEW, popsLastView);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Bundle backstackBundle = savedInstanceState.getParcelable(KEY_BACKSTACK);
        //noinspection ConstantConditions
        backstack.restoreInstanceState(backstackBundle);
        popsLastView = savedInstanceState.getBoolean(KEY_POPS_LAST_VIEW);

        Iterator<RouterTransaction> backstackIterator = backstack.reverseIterator();
        while (backstackIterator.hasNext()) {
            setControllerRouter(backstackIterator.next().controller);
        }
    }

    public final void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.createOptionsMenu(menu, inflater);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onCreateOptionsMenu(menu, inflater);
            }
        }
    }

    public final void onPrepareOptionsMenu(@NonNull Menu menu) {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.prepareOptionsMenu(menu);

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onPrepareOptionsMenu(menu);
            }
        }
    }

    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller.optionsItemSelected(item)) {
                return true;
            }

            for (Router childRouter : transaction.controller.getChildRouters()) {
                if (childRouter.onOptionsItemSelected(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void popToTransaction(@NonNull RouterTransaction transaction, @Nullable ControllerChangeHandler changeHandler) {
        if (backstack.size() > 0) {
            RouterTransaction topTransaction = backstack.peek();

            List<RouterTransaction> updatedBackstack = new ArrayList<>();
            Iterator<RouterTransaction> backstackIterator = backstack.reverseIterator();
            while (backstackIterator.hasNext()) {
                RouterTransaction existingTransaction = backstackIterator.next();
                updatedBackstack.add(existingTransaction);
                if (existingTransaction == transaction) {
                    break;
                }
            }

            if (changeHandler == null) {
                //noinspection ConstantConditions
                changeHandler = topTransaction.popChangeHandler();
            }

            setBackstack(updatedBackstack, changeHandler);
        }
    }

    void watchContainerAttach() {
        container.post(new Runnable() {
            @Override
            public void run() {
                containerFullyAttached = true;
            }
        });
    }

    void prepareForContainerRemoval() {
        containerFullyAttached = false;

        if (container != null) {
            container.setOnHierarchyChangeListener(null);
        }
    }

    void onContextAvailable() {
        for (RouterTransaction transaction : backstack) {
            transaction.controller.onContextAvailable();
        }
    }

    @NonNull
    final List<Controller> getControllers() {
        List<Controller> controllers = new ArrayList<>(backstack.size());

        Iterator<RouterTransaction> backstackIterator = backstack.reverseIterator();
        while (backstackIterator.hasNext()) {
            controllers.add(backstackIterator.next().controller);
        }

        return controllers;
    }

    @Nullable
    public final Boolean handleRequestedPermission(@NonNull String permission) {
        for (RouterTransaction transaction : backstack) {
            if (transaction.controller.didRequestPermission(permission)) {
                return transaction.controller.shouldShowRequestPermissionRationale(permission);
            }
        }
        return null;
    }

    private void performControllerChange(@Nullable RouterTransaction to, @Nullable RouterTransaction from, boolean isPush) {
        if (isPush && to != null) {
            to.onAttachedToRouter();
        }

        ControllerChangeHandler changeHandler;
        if (isPush) {
            //noinspection ConstantConditions
            changeHandler = to.pushChangeHandler();
        } else if (from != null) {
            changeHandler = from.popChangeHandler();
        } else {
            changeHandler = null;
        }

        performControllerChange(to, from, isPush, changeHandler);
    }

    void performControllerChange(@Nullable RouterTransaction to, @Nullable RouterTransaction from, boolean isPush, @Nullable ControllerChangeHandler changeHandler) {
        Controller toController = to != null ? to.controller : null;
        Controller fromController = from != null ? from.controller : null;
        boolean forceDetachDestroy = false;

        if (to != null) {
            to.ensureValidIndex(getTransactionIndexer());
            setControllerRouter(toController);
        } else if (backstack.size() == 0 && !popsLastView) {
            // We're emptying out the backstack. Views get weird if you transition them out, so just no-op it. The host
            // Activity or controller should be handling this by finishing or at least hiding this view.
            changeHandler = new NoOpControllerChangeHandler();
            forceDetachDestroy = true;
        }

        performControllerChange(toController, fromController, isPush, changeHandler);

        if (forceDetachDestroy && fromController != null && fromController.getView() != null) {
            fromController.detach(fromController.getView(), true, false);
        }
    }

    private void performControllerChange(@Nullable final Controller to, @Nullable final Controller from, final boolean isPush, @Nullable final ControllerChangeHandler changeHandler) {
        if (isPush && to != null && to.isDestroyed()) {
            throw new IllegalStateException("Trying to push a controller that has already been destroyed. (" + to.getClass().getSimpleName() + ")");
        }

        final ChangeTransaction transaction = new ChangeTransaction(to, from, isPush, container, changeHandler, new ArrayList<>(changeListeners));

        if (pendingControllerChanges.size() > 0) {
            // If we already have changes queued up (awaiting full container attach), queue this one up as well so they don't happen
            // out of order.
            pendingControllerChanges.add(transaction);
        } else if (from != null && (changeHandler == null || changeHandler.removesFromViewOnPush()) && !containerFullyAttached) {
            // If the change handler will remove the from view, we have to make sure the container is fully attached first so we avoid NPEs
            // within ViewGroup (details on issue #287). Post this to the container to ensure the attach is complete before we try to remove
            // anything.
            pendingControllerChanges.add(transaction);
            container.post(new Runnable() {
                @Override
                public void run() {
                    performPendingControllerChanges();
                }
            });
        } else {
            ControllerChangeHandler.executeChange(transaction);
        }
    }

    void performPendingControllerChanges() {
        // We're intentionally using dynamic size checking (list.size()) here so we can account for changes
        // that occur during this loop (ex: if a controller is popped from within onAttach)
        for (int i = 0; i < pendingControllerChanges.size(); i++) {
            ControllerChangeHandler.executeChange(pendingControllerChanges.get(i));
        }
        pendingControllerChanges.clear();
    }

    protected void pushToBackstack(@NonNull RouterTransaction entry) {
        if (backstack.contains(entry.controller)) {
            throw new IllegalStateException("Trying to push a controller that already exists on the backstack.");
        }
        backstack.push(entry);
    }

    private void trackDestroyingController(@NonNull RouterTransaction transaction) {
        if (!transaction.controller.isDestroyed()) {
            destroyingControllers.add(transaction.controller);

            transaction.controller.addLifecycleListener(new LifecycleListener() {
                @Override
                public void postDestroy(@NonNull Controller controller) {
                    destroyingControllers.remove(controller);
                }
            });
        }
    }

    private void trackDestroyingControllers(@NonNull List<RouterTransaction> transactions) {
        for (RouterTransaction transaction : transactions) {
            trackDestroyingController(transaction);
        }
    }

    private void removeAllExceptVisibleAndUnowned() {
        List<View> views = new ArrayList<>();

        for (RouterTransaction transaction : getVisibleTransactions(backstack.iterator())) {
            if (transaction.controller.getView() != null) {
                views.add(transaction.controller.getView());
            }
        }

        for (Router router : getSiblingRouters()) {
            if (router.container == container) {
                addRouterViewsToList(router, views);
            }
        }

        final int childCount = container.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = container.getChildAt(i);
            if (!views.contains(child)) {
                container.removeView(child);
            }
        }
    }

    // Swap around transaction indices to ensure they don't get thrown out of order by the
    // developer rearranging the backstack at runtime.
    private void ensureOrderedTransactionIndices(List<RouterTransaction> backstack) {
        List<Integer> indices = new ArrayList<>(backstack.size());
        for (RouterTransaction transaction : backstack) {
            transaction.ensureValidIndex(getTransactionIndexer());
            indices.add(transaction.transactionIndex);
        }

        Collections.sort(indices);

        for (int i = 0; i < backstack.size(); i++) {
            backstack.get(i).transactionIndex = indices.get(i);
        }
    }

    private void ensureNoDuplicateControllers(List<RouterTransaction> backstack) {
        for (int i = 0; i < backstack.size(); i++) {
            Controller controller = backstack.get(i).controller;
            for (int j = i + 1; j < backstack.size(); j++) {
                if (backstack.get(j).controller == controller) {
                    throw new IllegalStateException("Trying to push the same controller to the backstack more than once.");
                }
            }
        }
    }

    private void addRouterViewsToList(@NonNull Router router, @NonNull List<View> list) {
        for (Controller controller : router.getControllers()) {
            if (controller.getView() != null) {
                list.add(controller.getView());
            }

            for (Router child : controller.getChildRouters()) {
                addRouterViewsToList(child, list);
            }
        }
    }

    private List<RouterTransaction> getVisibleTransactions(@NonNull Iterator<RouterTransaction> backstackIterator) {
        List<RouterTransaction> transactions = new ArrayList<>();
        while (backstackIterator.hasNext()) {
            RouterTransaction transaction = backstackIterator.next();
            transactions.add(transaction);

            //noinspection ConstantConditions
            if (transaction.pushChangeHandler() == null || transaction.pushChangeHandler().removesFromViewOnPush()) {
                break;
            }
        }

        Collections.reverse(transactions);
        return transactions;
    }

    private boolean backstacksAreEqual(List<RouterTransaction> lhs, List<RouterTransaction> rhs) {
        if (lhs.size() != rhs.size()) {
            return false;
        }

        for (int i = 0; i < rhs.size(); i++) {
            if (rhs.get(i).controller() != lhs.get(i).controller()) {
                return false;
            }
        }

        return true;
    }

    void setControllerRouter(@NonNull Controller controller) {
        controller.setRouter(this);
        controller.onContextAvailable();
    }

    abstract void invalidateOptionsMenu();
    abstract void startActivity(@NonNull Intent intent);
    abstract void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode);
    abstract void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options);
    abstract void startIntentSenderForResult(@NonNull String instanceId, @NonNull IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask,
                                             int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException;
    abstract void registerForActivityResult(@NonNull String instanceId, int requestCode);
    abstract void unregisterForActivityResults(@NonNull String instanceId);
    abstract void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode);
    abstract boolean hasHost();
    @NonNull abstract List<Router> getSiblingRouters();
    @NonNull abstract Router getRootRouter();
    @NonNull abstract TransactionIndexer getTransactionIndexer();

}
