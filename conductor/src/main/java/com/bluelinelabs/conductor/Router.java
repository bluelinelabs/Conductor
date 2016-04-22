package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.internal.LifecycleHandler;
import com.bluelinelabs.conductor.internal.NoOpControllerChangeHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Router implements navigation and backstack handling for {@link Controller}s. Router objects are attached
 * to Activity/containing ViewGroup pairs. Routers do not directly render or push Views to the container ViewGroup,
 * but instead defer this responsibility to the {@link ControllerChangeHandler} specified in a given transaction.
 */
public class Router {

    private final Backstack mBackStack = new Backstack();
    private LifecycleHandler mLifecycleHandler;
    private ViewGroup mContainer;
    private final List<ControllerChangeListener> mChangeListeners = new ArrayList<>();
    private final List<Controller> mDestroyingControllers = new ArrayList<>();

    /**
     * Returns this Router's host Activity
     */
    public Activity getActivity() {
        return mLifecycleHandler != null ? mLifecycleHandler.getLifecycleActivity() : null;
    }

    /**
     * This should be called by the host Activity when its onActivityResult method is called if the instanceId
     * of the controller that called startActivityForResult is not known.
     *
     * @param requestCode The Activity's onActivityResult requestCode
     * @param resultCode The Activity's onActivityResult resultCode
     * @param data The Activity's onActivityResult data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getLifecycleHandler().onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This should be called by the host Activity when its onRequestPermissionsResult method is called. The call will be forwarded
     * to the {@link Controller} with the instanceId passed in.
     *
     * @param instanceId The instanceId of the Controller to which this result should be forwarded
     * @param requestCode The Activity's onRequestPermissionsResult requestCode
     * @param permissions The Activity's onRequestPermissionsResult permissions
     * @param grantResults The Activity's onRequestPermissionsResult grantResults
     */
    public void onRequestPermissionsResult(String instanceId, int requestCode, String[] permissions, int[] grantResults) {
        Controller controller = getControllerWithInstanceId(instanceId);
        if (controller != null) {
            controller.requestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * This should be called by the host Activity when its onBackPressed method is called. The call will be forwarded
     * to its top {@link Controller}. If that controller doesn't handle it, then it will be popped.
     */
    public boolean handleBack() {
        if (!mBackStack.isEmpty()) {
            if (mBackStack.peek().controller.handleBack()) {
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
    public boolean popCurrentController() {
        return popController(mBackStack.peek().controller);
    }

    /**
     * Pops the passed {@link Controller} from the backstack
     *
     * @param controller The controller that should be popped from this Router
     * @return Whether or not this Router still has controllers remaining on it after popping.
     */
    public boolean popController(Controller controller) {
        RouterTransaction topController = mBackStack.peek();
        boolean poppingTopController = topController.controller == controller;

        if (poppingTopController) {
            trackDestroyingController(mBackStack.pop());
        } else {
            for (RouterTransaction transaction : mBackStack) {
                if (transaction.controller == controller) {
                    mBackStack.remove(transaction);
                    break;
                }
            }
        }

        if (poppingTopController) {
            performControllerChange(mBackStack.peek(), topController, false);
        }

        return !mBackStack.isEmpty();
    }

    /**
     * Pushes a new {@link Controller} to the backstack
     *
     * @param transaction The transaction detailing what should be pushed, including the {@link Controller},
     *                    and its push and pop {@link ControllerChangeHandler}, and its tag.
     */
    public void pushController(@NonNull RouterTransaction transaction) {
        RouterTransaction from = mBackStack.peek();
        pushToBackstack(transaction);
        performControllerChange(transaction, from, true);
    }

    /**
     * Replaces this Router's top {@link Controller} with a new {@link Controller}
     *
     * @param transaction The transaction detailing what should be pushed, including the {@link Controller},
     *                    and its push and pop {@link ControllerChangeHandler}, and its tag.
     */
    public void replaceTopController(@NonNull RouterTransaction transaction) {
        RouterTransaction topTransaction = mBackStack.peek();
        if (!mBackStack.isEmpty()) {
            trackDestroyingController(mBackStack.pop());
        }

        pushToBackstack(transaction);
        performControllerChange(transaction, topTransaction, true);
    }

    /**
     * Pops all {@link Controller}s until only the root is left
     *
     * @return Whether or not any {@link Controller}s were popped in order to get to the root transaction
     */
    public boolean popToRoot() {
        return popToRoot(null);
    }

    /**
     * Pops all {@link Controller} until only the root is left
     *
     * @param changeHandler The {@link ControllerChangeHandler} to handle this transaction
     * @return Whether or not any {@link Controller}s were popped in order to get to the root transaction
     */
    public boolean popToRoot(ControllerChangeHandler changeHandler) {
        if (mBackStack.size() > 1) {
            popToTransaction(mBackStack.root(), changeHandler);
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
    public boolean popToTag(@NonNull String tag) {
        return popToTag(tag, null);
    }

    /**
     * Pops all {@link Controller}s until the {@link Controller} with the passed tag is at the top
     *
     * @param tag The tag being popped to
     * @param changeHandler The {@link ControllerChangeHandler} to handle this transaction
     * @return Whether or not the {@link Controller} with the passed tag is now at the top
     */
    public boolean popToTag(@NonNull String tag, ControllerChangeHandler changeHandler) {
        for (RouterTransaction transaction : mBackStack) {
            if (tag.equals(transaction.tag)) {
                popToTransaction(transaction, changeHandler);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the root {@link Controller}. If any {@link Controller}s are currently in the backstack, they will be removed.
     *
     * @param controller The new root {@link Controller}
     */
    public void setRoot(@NonNull Controller controller) {
        setRoot(controller, null, null);
    }

    /**
     * Sets the root {@link Controller}. If any {@link Controller}s are currently in the backstack, they will be removed.
     *
     * @param controller The new root {@link Controller}
     * @param tag The tag to use for this {@link Controller}
     */
    public void setRoot(@NonNull Controller controller, String tag) {
        setRoot(controller, tag, null);
    }

    /**
     * Sets the root {@link Controller}. If any {@link Controller}s are currently in the backstack, they will be removed.
     *
     * @param controller The new root {@link Controller}
     * @param changeHandler The {@link ControllerChangeHandler} to use for setting the root
     */
    public void setRoot(@NonNull Controller controller, ControllerChangeHandler changeHandler) {
        setRoot(controller, null, changeHandler);
    }

    /**
     * Sets the root Controller. If any {@link Controller}s are currently in the backstack, they will be removed.
     *
     * @param controller The new root {@link Controller}
     * @param tag The tag to use for this {@link Controller}
     * @param changeHandler The {@link ControllerChangeHandler} to use for setting the root
     */
    public void setRoot(@NonNull Controller controller, String tag, ControllerChangeHandler changeHandler) {
        RouterTransaction currentTop = mBackStack.peek();

        if (currentTop != null && currentTop.controller.getView() != null) {
            final View fromView = currentTop.controller.getView();

            final int childCount = mContainer.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = mContainer.getChildAt(i);
                if (child != fromView) {
                    mContainer.removeView(child);
                }
            }
        }

        trackDestroyingControllers(mBackStack.popAll());

        RouterTransaction transaction = RouterTransaction.builder(controller)
                .tag(tag)
                .pushChangeHandler(changeHandler != null ? changeHandler : new SimpleSwapChangeHandler())
                .popChangeHandler(new SimpleSwapChangeHandler())
                .build();

        pushToBackstack(transaction);
        performControllerChange(transaction, currentTop, true);
    }

    /**
     * Returns the hosted Controller with the given instance id, if available.
     *
     * @param instanceId The instance ID being searched for
     * @return The matching Controller, if one exists
     */
    public Controller getControllerWithInstanceId(String instanceId) {
        for (ControllerTransaction transaction : mBackStack) {
            Controller controllerWithId = transaction.controller.getControllerWithInstanceId(instanceId);
                if (controllerWithId != null) {
                    return controllerWithId;
                }
        }
        return null;
    }

    /**
     * Returns the hosted Controller that was pushed with the given tag, if available.
     *
     * @param tag The tag being searched for
     * @return The matching Controller, if one exists
     */
    public Controller getControllerWithTag(String tag) {
        for (ControllerTransaction transaction : mBackStack) {
            if (tag.equals(transaction.tag)) {
                return transaction.controller;
            }
        }
        return null;
    }

    /**
     * Returns the number of {@link Controller}s currently in the backstack
     */
    public int getBackstackSize() {
        return mBackStack.size();
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
    public void addChangeListener(ControllerChangeListener changeListener) {
        if (!mChangeListeners.contains(changeListener)) {
            mChangeListeners.add(changeListener);
        }
    }

    /**
     * Removes a previously added listener
     *
     * @param changeListener The listener to be removed
     */
    public void removeChangeListener(ControllerChangeListener changeListener) {
        mChangeListeners.remove(changeListener);
    }

    /**
     * Attaches this Router's existing backstack to its container if one exists.
     */
    void rebindIfNeeded() {
        Iterator<RouterTransaction> backstackIterator = mBackStack.reverseIterator();
        while (backstackIterator.hasNext()) {
            RouterTransaction transaction = backstackIterator.next();

            if (transaction.controller.getNeedsAttach()) {
                performControllerChange(transaction.controller, null, true, new SimpleSwapChangeHandler(false));
            }
        }
    }

    public final void onActivityResult(String instanceId, int requestCode, int resultCode, Intent data) {
        Controller controller = getControllerWithInstanceId(instanceId);
        if (controller != null) {
            controller.onActivityResult(requestCode, resultCode, data);
        }
    }

    public final void onActivityStarted(Activity activity) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.activityStarted(activity);
        }
    }

    public final void onActivityResumed(Activity activity) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.activityResumed(activity);
        }
    }

    public final void onActivityPaused(Activity activity) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.activityPaused(activity);
        }
    }

    public final void onActivityStopped(Activity activity) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.activityStopped(activity);
        }
    }

    public final void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.prepareForActivityPause();
        }

        mBackStack.detachAndSaveInstanceState(outState);
    }

    public final void onActivityDestroyed(Activity activity) {
        mContainer.setOnHierarchyChangeListener(null);
        mChangeListeners.clear();

        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.activityDestroyed(activity.isChangingConfigurations());
        }

        for (Controller controller : mDestroyingControllers) {
            controller.activityDestroyed(activity.isChangingConfigurations());
        }

        mLifecycleHandler = null;
        mContainer = null;
    }

    public final void onRestoreInstanceState(Bundle savedInstanceState) {
        mBackStack.restoreInstanceState(savedInstanceState);

        Iterator<RouterTransaction> backstackIterator = mBackStack.reverseIterator();
        while (backstackIterator.hasNext()) {
            backstackIterator.next().controller.setRouter(this);
        }
    }

    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.createOptionsMenu(menu, inflater);
        }
    }

    public final void onPrepareOptionsMenu(Menu menu) {
        for (RouterTransaction transaction : mBackStack) {
            transaction.controller.prepareOptionsMenu(menu);
        }
    }

    public final boolean onOptionsItemSelected(MenuItem item) {
        for (RouterTransaction transaction : mBackStack) {
            if (transaction.controller.optionsItemSelected(item)) {
                return true;
            }
        }
        return false;
    }

    final void invalidateOptionsMenu() {
        if (mLifecycleHandler != null) {
            mLifecycleHandler.getFragmentManager().invalidateOptionsMenu();
        }
    }

    private void popToTransaction(@NonNull RouterTransaction transaction, ControllerChangeHandler changeHandler) {
        RouterTransaction topTransaction = mBackStack.peek();
        List<RouterTransaction> poppedTransactions = mBackStack.popTo(transaction);
        trackDestroyingControllers(poppedTransactions);

        if (poppedTransactions.size() > 0) {
            if (changeHandler == null) {
                changeHandler = topTransaction.getPopControllerChangeHandler();
            }

            performControllerChange(mBackStack.peek().controller, topTransaction.controller, false, changeHandler);
        }
    }

    public final void setHost(@NonNull LifecycleHandler lifecycleHandler, @NonNull ViewGroup container) {
        if (mLifecycleHandler != lifecycleHandler || mContainer != container) {
            if (mContainer != null && mContainer instanceof ControllerChangeListener) {
                removeChangeListener((ControllerChangeListener)mContainer);
            }

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            mLifecycleHandler = lifecycleHandler;
            mContainer = container;
        }
    }

    final LifecycleHandler getLifecycleHandler() {
        return mLifecycleHandler;
    }

    public final Boolean handleRequestedPermission(@NonNull String permission) {
        for (ControllerTransaction transaction : mBackStack) {
            if (transaction.controller.didRequestPermission(permission)) {
                return transaction.controller.shouldShowRequestPermissionRationale(permission);
            }
        }
        return null;
    }

    private void performControllerChange(RouterTransaction to, RouterTransaction from, boolean isPush) {
        ControllerChangeHandler changeHandler;
        if (isPush) {
            //noinspection ConstantConditions
            changeHandler = to.getPushControllerChangeHandler();
        } else if (from != null) {
            changeHandler = from.getPopControllerChangeHandler();
        } else {
            changeHandler = new SimpleSwapChangeHandler();
        }

        Controller toController = to != null ? to.controller : null;
        Controller fromController = from != null ? from.controller : null;

        performControllerChange(toController, fromController, isPush, changeHandler);
    }

    private void performControllerChange(final Controller to, final Controller from, boolean isPush, @NonNull ControllerChangeHandler changeHandler) {
        if (to != null) {
            to.setRouter(this);
        } else if (mBackStack.size() == 0) {
            // We're emptying out the backstack. Views get weird if you transition them out, so just no-op it. The hosting
            // Activity should be handling this by finishing or at least hiding this view.
            changeHandler = new NoOpControllerChangeHandler();
        }

        if (mContainer != null) {
            ControllerChangeHandler.executeChange(to, from, isPush, mContainer, changeHandler, mChangeListeners);
        }

    }

    private void pushToBackstack(@NonNull RouterTransaction entry) {
        mBackStack.push(entry);
    }

    private void trackDestroyingController(RouterTransaction transaction) {
        if (!transaction.controller.isDestroyed()) {
            mDestroyingControllers.add(transaction.controller);

            transaction.controller.addLifecycleListener(new LifecycleListener() {
                @Override
                public void postDestroy(@NonNull Controller controller) {
                    mDestroyingControllers.remove(controller);
                }
            });
        }
    }

    private void trackDestroyingControllers(List<RouterTransaction> transactions) {
        for (RouterTransaction transaction : transactions) {
            trackDestroyingController(transaction);
        }
    }

}
