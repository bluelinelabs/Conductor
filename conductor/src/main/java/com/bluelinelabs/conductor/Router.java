package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.internal.NoOpControllerChangeHandler;
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

    protected final Backstack backstack = new Backstack();
    private final List<ControllerChangeListener> changeListeners = new ArrayList<>();
    final List<Controller> destroyingControllers = new ArrayList<>();

    private boolean popsLastView = false;

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
    @UiThread
    public boolean popCurrentController() {
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
        RouterTransaction topController = backstack.peek();
        boolean poppingTopController = topController != null && topController.controller == controller;

        if (poppingTopController) {
            trackDestroyingController(backstack.pop());
        } else {
            for (RouterTransaction transaction : backstack) {
                if (transaction.controller == controller) {
                    backstack.remove(transaction);
                    break;
                }
            }
        }

        if (poppingTopController) {
            performControllerChange(backstack.peek(), topController, false);
        }

        if (popsLastView) {
            return topController != null;
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
    @UiThread
    public void replaceTopController(@NonNull RouterTransaction transaction) {
        RouterTransaction topTransaction = backstack.peek();
        if (!backstack.isEmpty()) {
            trackDestroyingController(backstack.pop());
        }

        final ControllerChangeHandler handler = transaction.pushChangeHandler();
        if (topTransaction != null) {
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
        return popToRoot(null);
    }

    /**
     * Pops all {@link Controller} until only the root is left
     *
     * @param changeHandler The {@link ControllerChangeHandler} to handle this transaction
     * @return Whether or not any {@link Controller}s were popped in order to get to the root transaction
     */
    @UiThread
    public boolean popToRoot(@Nullable ControllerChangeHandler changeHandler) {
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
        return popToTag(tag, null);
    }

    /**
     * Pops all {@link Controller}s until the {@link Controller} with the passed tag is at the top
     *
     * @param tag           The tag being popped to
     * @param changeHandler The {@link ControllerChangeHandler} to handle this transaction
     * @return Whether or not the {@link Controller} with the passed tag is now at the top
     */
    @UiThread
    public boolean popToTag(@NonNull String tag, @Nullable ControllerChangeHandler changeHandler) {
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
    public int getBackstackSize() {
        return backstack.size();
    }

    /**
     * Returns the current backstack, ordered from root to most recently pushed.
     */
    @NonNull
    public List<RouterTransaction> getBackstack() {
        List<RouterTransaction> list = new ArrayList<>();
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
    @UiThread
    public void setBackstack(@NonNull List<RouterTransaction> newBackstack, @Nullable ControllerChangeHandler changeHandler) {
        List<RouterTransaction> oldVisibleTransactions = getVisibleTransactions(backstack.iterator());

        removeAllExceptVisibleAndUnowned();
        ensureOrderedTransactionIndices(newBackstack);

        backstack.setBackstack(newBackstack);
        for (RouterTransaction transaction : backstack) {
            transaction.onAttachedToRouter();
        }

        if (newBackstack.size() > 0) {
            List<RouterTransaction> reverseNewBackstack = new ArrayList<>(newBackstack);
            Collections.reverse(reverseNewBackstack);
            List<RouterTransaction> newVisibleTransactions = getVisibleTransactions(reverseNewBackstack.iterator());

            boolean visibleTransactionsChanged = !backstacksAreEqual(newVisibleTransactions, oldVisibleTransactions);
            if (visibleTransactionsChanged) {
                RouterTransaction rootTransaction = oldVisibleTransactions.size() > 0 ? oldVisibleTransactions.get(0) : null;
                performControllerChange(newVisibleTransactions.get(0), rootTransaction, true, changeHandler);

                for (int i = oldVisibleTransactions.size() - 1; i > 0; i--) {
                    RouterTransaction transaction = oldVisibleTransactions.get(i);
                    ControllerChangeHandler localHandler = changeHandler != null ? changeHandler.copy() : new SimpleSwapChangeHandler();
                    localHandler.setForceRemoveViewOnPush(true);
                    performControllerChange(null, transaction, true, localHandler);
                }

                for (int i = 1; i < newVisibleTransactions.size(); i++) {
                    RouterTransaction transaction = newVisibleTransactions.get(i);
                    performControllerChange(transaction, newVisibleTransactions.get(i - 1), true, transaction.pushChangeHandler());
                }
            }

            // Ensure all new controllers have a valid router set
            for (RouterTransaction transaction : newBackstack) {
                transaction.controller.setRouter(this);
            }
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
    public void removeChangeListener(@NonNull ControllerChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    /**
     * Attaches this Router's existing backstack to its container if one exists.
     */
    @UiThread
    public void rebindIfNeeded() {
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
    }

    public void onActivityDestroyed(@NonNull Activity activity) {
        prepareForContainerRemoval();
        changeListeners.clear();

        for (RouterTransaction transaction : backstack) {
            transaction.controller.activityDestroyed(activity.isChangingConfigurations());

            for (Router childRouter : transaction.controller.getChildRouters()) {
                childRouter.onActivityDestroyed(activity);
            }
        }

        for (int index = destroyingControllers.size() - 1; index >= 0; index--) {
            Controller controller = destroyingControllers.get(index);
            controller.activityDestroyed(activity.isChangingConfigurations());

            for (Router childRouter : controller.getChildRouters()) {
                childRouter.onActivityDestroyed(activity);
            }
        }

        container = null;
    }

    public void prepareForHostDetach() {
        for (RouterTransaction transaction : backstack) {
            if (ControllerChangeHandler.completePushImmediately(transaction.controller.getInstanceId())) {
                transaction.controller.setNeedsAttach();
            }
            transaction.controller.prepareForHostDetach();
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        prepareForHostDetach();

        Bundle backstackState = new Bundle();
        backstack.saveInstanceState(backstackState);

        outState.putParcelable(KEY_BACKSTACK, backstackState);
        outState.putBoolean(KEY_POPS_LAST_VIEW, popsLastView);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Bundle backstackBundle = savedInstanceState.getParcelable(KEY_BACKSTACK);
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
        RouterTransaction topTransaction = backstack.peek();
        List<RouterTransaction> poppedTransactions = backstack.popTo(transaction);
        trackDestroyingControllers(poppedTransactions);

        if (poppedTransactions.size() > 0) {
            if (changeHandler == null) {
                changeHandler = topTransaction.popChangeHandler();
            }

            performControllerChange(backstack.peek(), topTransaction, false, changeHandler);
        }
    }

    void prepareForContainerRemoval() {
        if (container != null) {
            container.setOnHierarchyChangeListener(null);
        }
    }

    @NonNull
    final List<Controller> getControllers() {
        List<Controller> controllers = new ArrayList<>();

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

    private void performControllerChange(@Nullable final RouterTransaction to, @Nullable final RouterTransaction from, boolean isPush, @Nullable ControllerChangeHandler changeHandler) {
        Controller toController = to != null ? to.controller : null;
        Controller fromController = from != null ? from.controller : null;

        if (to != null) {
            to.ensureValidIndex(getTransactionIndexer());
            setControllerRouter(toController);
        } else if (backstack.size() == 0 && !popsLastView) {
            // We're emptying out the backstack. Views get weird if you transition them out, so just no-op it. The hosting
            // Activity should be handling this by finishing or at least hiding this view.
            changeHandler = new NoOpControllerChangeHandler();
        }

        ControllerChangeHandler.executeChange(toController, fromController, isPush, container, changeHandler, changeListeners);
    }

    private void pushToBackstack(@NonNull RouterTransaction entry) {
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
        for (int i = 0; i < childCount; i++) {
            final View child = container.getChildAt(i);
            if (!views.contains(child)) {
                container.removeView(child);
            }
        }
    }

    // Swap around transaction indicies to ensure they don't get thrown out of order by the
    // developer rearranging the backstack at runtime.
    private void ensureOrderedTransactionIndices(List<RouterTransaction> backstack) {
        List<Integer> indices = new ArrayList<>();
        for (RouterTransaction transaction : backstack) {
            transaction.ensureValidIndex(getTransactionIndexer());
            indices.add(transaction.transactionIndex);
        }

        Collections.sort(indices);

        for (int i = 0; i < backstack.size(); i++) {
            backstack.get(i).transactionIndex = indices.get(i);
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
    }

    abstract void invalidateOptionsMenu();
    abstract void startActivity(@NonNull Intent intent);
    abstract void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode);
    abstract void startActivityForResult(@NonNull String instanceId, @NonNull Intent intent, int requestCode, @Nullable Bundle options);
    abstract void registerForActivityResult(@NonNull String instanceId, int requestCode);
    abstract void unregisterForActivityResults(@NonNull String instanceId);
    abstract void requestPermissions(@NonNull String instanceId, @NonNull String[] permissions, int requestCode);
    abstract boolean hasHost();
    @NonNull abstract List<Router> getSiblingRouters();
    @NonNull abstract Router getRootRouter();
    @Nullable abstract TransactionIndexer getTransactionIndexer();

}
