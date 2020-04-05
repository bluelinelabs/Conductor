package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.internal.ThreadUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Master/detail controller implementation.
 *
 * @see <a href="https://github.com/bluelinelabs/Conductor/issues/176>#176</a>
 *
 */
public abstract class MasterDetailController extends Controller {

    private Router masterRouter;

    private Router cachedMasterRouter;

    private Router cachedDetailRouter;

    @IdRes private int detailContainerId;

    @Nullable private ViewGroup detailContainer;

    @Nullable private Boolean detailPopsLastView;

    protected MasterDetailController() {
        super();
    }

    protected MasterDetailController(Bundle args) {
        super(args);
    }

    /**
     * Returns the master {@link Router} object that can be used for pushing or popping other Controllers
     */
    @NonNull
    public final Router getMasterRouter() {
        checkCachedRouters();
        return cachedMasterRouter;
    }

    /**
     * Returns the detail {@link Router} object that can be used for pushing or popping other Controllers
     */
    @NonNull
    public final Router getDetailRouter() {
        checkCachedRouters();
        return cachedDetailRouter;
    }

    /**
     * Returns whether or not this Controller has two panes visible: one for master, another for detail.
     */
    public final boolean isTwoPanesMode() {
        checkInitialized();
        return detailContainer != null;
    }

    /**
     * Returns the {@link ControllerChangeHandler} that should be used in the single-pane mode
     * for pushing the root detail Controller to the master router.
     */
    public ControllerChangeHandler getRootDetailPushHandler() {
        return new HorizontalChangeHandler();
    }

    /**
     * Returns the {@link ControllerChangeHandler} that should be used in the single-pane mode
     * for popping the root detail Controller out of the master router.
     */
    public ControllerChangeHandler getRootDetailPopHandler() {
        return new HorizontalChangeHandler();
    }

    /**
     * Initializes the master/detail {@link Router}s.
     *
     * @param parentView        The View hosting the master/detail containers.
     * @param masterContainerId The master container ID
     * @param detailContainerId The detail container ID
     */
    protected void initRouters(@NonNull View parentView, @IdRes int masterContainerId, @IdRes int detailContainerId) {
        View masterView = parentView.findViewById(masterContainerId);
        if (masterView == null) {
            throw new IllegalStateException("No master container found");
        }
        if (!(masterView instanceof ViewGroup)) {
            throw new IllegalStateException("Master container must be a ViewGroup. Currently its " + masterView);
        }

        this.detailContainerId = detailContainerId;
        View detailView = parentView.findViewById(detailContainerId);
        if (detailView instanceof ViewGroup) {
            detailContainer = (ViewGroup) detailView;
        } else {
            detailContainer = null;
        }

        // Reset cached routers before we initialize the master router.
        cachedMasterRouter = null;
        cachedDetailRouter = null;

        // Initialize the master router.
        masterRouter = getChildRouter((ViewGroup) masterView);

        // Check if cached routers are initialized.
        // The prior call might attach the controllers which already accessed the cached routers
        // thus initialize them. If not, do this right now.
        checkCachedRouters();
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);
        detailContainer = null;
    }

    private void checkInitialized() {
        if (!masterRouter.hasHost()) {
            throw new IllegalStateException("Master router is not attached to the view. " +
                    "Perhaps you called this after onDestroyView() or forgot to call initRouters().");
        }
    }

    private void checkCachedRouters() {
        checkInitialized();
        if (cachedMasterRouter == null || cachedDetailRouter == null) {
            if (detailContainer != null) {
                cachedMasterRouter = masterRouter;
                cachedDetailRouter = getChildRouter(detailContainer);
                cachedDetailRouter.isDetail = true;
                if (detailPopsLastView != null) {
                    cachedDetailRouter.setPopsLastView(detailPopsLastView);
                    detailPopsLastView = null;
                }
                if (!cachedDetailRouter.hasRootController()) {
                    splitBackstacks();
                }
            } else {
                cachedMasterRouter = new MasterRouterForSinglePane();
                cachedDetailRouter = new DetailRouterForSinglePane();
                mergeBackstacks();
            }
        }
    }

    private void splitBackstacks() {
        List<RouterTransaction> masterTransactions = new ArrayList<>();
        List<RouterTransaction> detailTransactions = new ArrayList<>();
        List<RouterTransaction> backstack = masterRouter.getBackstack();
        for (RouterTransaction transaction : backstack) {
            if (transaction.isDetail()) {
                detailTransactions.add(transaction);
            } else {
                masterTransactions.add(transaction);
            }
        }
        if (!detailTransactions.isEmpty()) {
            setNeedsAttachLast(detailTransactions);
            setNeedsAttachLast(masterTransactions);
            cachedDetailRouter.setBackstack(detailTransactions, null);
            masterRouter.setBackstack(masterTransactions, null);
        }
    }

    private void mergeBackstacks() {
        Router detailRouter = null;
        for (Router router : getChildRouters()) {
            if (((ControllerHostedRouter) router).getHostId() == detailContainerId) {
                detailRouter = router;
                break;
            }
        }
        if (detailRouter != null && detailRouter.hasRootController()) {
            List<RouterTransaction> masterTransactions = masterRouter.getBackstack();
            masterTransactions.addAll(detailRouter.getBackstack());
            setNeedsAttachLast(masterTransactions);
            masterRouter.setBackstack(masterTransactions, null);
            detailRouter.backstack.clear();
        }
    }

    private void setNeedsAttachLast(List<RouterTransaction> transactions) {
        for (int i = 0; i < transactions.size(); i++) {
            RouterTransaction transaction = transactions.get(i);
            transaction.controller().setNeedsAttach(i == transactions.size() - 1);
        }
    }

    private List<RouterTransaction> getChildBackstack(boolean isDetail) {
        List<RouterTransaction> transactions = new ArrayList<>();
        Iterator<RouterTransaction> iterator = masterRouter.backstack.reverseIterator();
        while (iterator.hasNext()) {
            RouterTransaction transaction = iterator.next();
            if (isDetail == transaction.isDetail()) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private int getChildBackstackSize(boolean isDetail) {
        int count = 0;
        for (RouterTransaction transaction : masterRouter.backstack) {
            if (transaction.isDetail() == isDetail) {
                count++;
            }
        }
        return count;
    }

    /**
     * Master Router used in single-pane mode.
     * Manages the transactions which belong to master Router
     * but doesn't touch the detail Router transactions
     * which are temporarily placed in the master Router.
     */
    private class MasterRouterForSinglePane extends ControllerHostedRouter {

        @NonNull
        @Override
        public Router setPopsLastView(boolean popsLastView) {
            masterRouter.setPopsLastView(popsLastView);
            return masterRouter;
        }

        @Override
        public boolean popController(@NonNull Controller controller) {
            return masterRouter.popController(controller);
        }

        @Override
        public boolean popCurrentController() {
            ThreadUtils.ensureMainThread();

            if (getChildBackstackSize(true) == 0) {
                return masterRouter.popCurrentController();
            } else {
                List<RouterTransaction> masterTransactions = getChildBackstack(false);
                if (masterTransactions.isEmpty()) {
                    throw new IllegalStateException("Trying to pop the current controller when there are none on the backstack.");
                }
                masterTransactions.remove(masterTransactions.size() - 1);
                masterTransactions.addAll(getChildBackstack(true));
                setNeedsAttachLast(masterTransactions);
                masterRouter.setBackstack(masterTransactions, null);
                return true;
            }
        }

        @Override
        public void pushController(@NonNull RouterTransaction transaction) {
            ThreadUtils.ensureMainThread();

            if (getChildBackstackSize(true) == 0) {
                masterRouter.pushController(transaction);
            } else {
                List<RouterTransaction> masterTransactions = getChildBackstack(false);
                masterTransactions.add(transaction);
                masterTransactions.addAll(getChildBackstack(true));
                setNeedsAttachLast(masterTransactions);
                masterRouter.setBackstack(masterTransactions, null);
            }
        }

        @Override
        public void replaceTopController(@NonNull RouterTransaction transaction) {
            ThreadUtils.ensureMainThread();

            if (getChildBackstackSize(true) == 0) {
                masterRouter.replaceTopController(transaction);
            } else {
                List<RouterTransaction> masterTransactions = getChildBackstack(false);
                if (masterTransactions.isEmpty()) {
                    masterTransactions.add(transaction);
                } else {
                    masterTransactions.set(masterTransactions.size() - 1, transaction);
                }
                masterTransactions.addAll(getChildBackstack(true));
                setNeedsAttachLast(masterTransactions);
                masterRouter.setBackstack(masterTransactions, null);
            }
        }

        @Override
        public boolean popToRoot(@Nullable ControllerChangeHandler changeHandler) {
            ThreadUtils.ensureMainThread();

            if (getChildBackstackSize(true) == 0) {
                return masterRouter.popToRoot(changeHandler);
            } else {
                List<RouterTransaction> masterTransactions = getChildBackstack(false);
                if (!masterTransactions.isEmpty()) {
                    List<RouterTransaction> transactions = getChildBackstack(true);
                    transactions.add(0, masterTransactions.get(0));
                    setNeedsAttachLast(transactions);
                    masterRouter.setBackstack(transactions, null);
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean popToTag(@NonNull String tag, @Nullable ControllerChangeHandler changeHandler) {
            ThreadUtils.ensureMainThread();

            if (getChildBackstackSize(true) == 0) {
                return masterRouter.popToTag(tag, changeHandler);
            } else {
                int index = -1;
                List<RouterTransaction> masterTransactions = getChildBackstack(false);
                for (int i = 0; i < masterTransactions.size(); i++) {
                    if (tag.equals(masterTransactions.get(i).tag())) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    return false;
                }
                List<RouterTransaction> transactions = new ArrayList<>(masterTransactions.subList(0, index + 1));
                transactions.addAll(getChildBackstack(true));
                setNeedsAttachLast(transactions);
                masterRouter.setBackstack(transactions, null);
                return true;
            }
        }

        @Override
        public void setRoot(@NonNull RouterTransaction transaction) {
            ThreadUtils.ensureMainThread();

            if (getChildBackstackSize(true) == 0) {
                masterRouter.setRoot(transaction);
            } else {
                List<RouterTransaction> transactions = getChildBackstack(true);
                transactions.add(0, transaction);
                setNeedsAttachLast(transactions);
                masterRouter.setBackstack(transactions, null);
            }
        }

        @Override @NonNull
        public List<RouterTransaction> getBackstack() {
            return getChildBackstack(false);
        }

        @Override
        public int getBackstackSize() {
            return getChildBackstackSize(false);
        }
    }

    /**
     * Detail Router used in single-pane mode.
     * Manages the transactions which belong to detail Router
     * but temporarily are placed in the master Router.
     */
    private class DetailRouterForSinglePane extends ControllerHostedRouter {

        @Override @NonNull
        public Router setPopsLastView(boolean popsLastView) {
            ThreadUtils.ensureMainThread();

            detailPopsLastView = popsLastView;
            return this;
        }

        @Override
        public boolean popController(@NonNull Controller controller) {
            return masterRouter.popController(controller);
        }

        @Override
        public boolean popCurrentController() {
            return masterRouter.popCurrentController();
        }

        @Override
        public void pushController(@NonNull RouterTransaction transaction) {
            masterRouter.pushController(transaction.isDetail(true));
        }

        @Override
        public void replaceTopController(@NonNull RouterTransaction transaction) {
            masterRouter.replaceTopController(transaction.isDetail(true));
        }

        @Override
        public boolean popToRoot(@Nullable ControllerChangeHandler changeHandler) {
            ThreadUtils.ensureMainThread();

            Iterator<RouterTransaction> iterator = masterRouter.backstack.reverseIterator();
            while (iterator.hasNext()) {
                RouterTransaction transaction = iterator.next();
                if (transaction.isDetail()) {
                    masterRouter.popToTransaction(transaction, changeHandler);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean popToTag(@NonNull String tag, @Nullable ControllerChangeHandler changeHandler) {
            ThreadUtils.ensureMainThread();

            for (RouterTransaction transaction : masterRouter.backstack) {
                if (transaction.isDetail() && tag.equals(transaction.tag())) {
                    masterRouter.popToTransaction(transaction, changeHandler);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void setRoot(@NonNull RouterTransaction transaction) {
            ThreadUtils.ensureMainThread();

            List<RouterTransaction> transactions = getChildBackstack(false);
            transactions.add(transaction.isDetail(true));
            masterRouter.setBackstack(transactions, transaction.pushChangeHandler() != null
                    ? transaction.pushChangeHandler()
                    : getRootDetailPushHandler());
        }

        @Override @NonNull
        public List<RouterTransaction> getBackstack() {
            ThreadUtils.ensureMainThread();

            return getChildBackstack(true);
        }

        @Override
        public int getBackstackSize() {
            ThreadUtils.ensureMainThread();

            return getChildBackstackSize(true);
        }
    }
}
