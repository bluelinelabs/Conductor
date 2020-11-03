package com.bluelinelabs.conductor.demo.controllers.bottomnavigation;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ChangeHandlerFrameLayout;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;

import java.util.List;

import butterknife.BindView;

/**
 * The {@link Controller} for the Bottom Navigation View. Populates a {@link BottomNavigationView}
 * with the supplied {@link Menu} resource. The first item set as checked will be shown by default.
 * The backstack of each {@link MenuItem} is switched out, in order to maintain a separate backstack
 * for each {@link MenuItem} - even though that is against the Google Design Guidelines.
 * <p>
 * <p>Internally works similarly to {@link com.bluelinelabs.conductor.support.RouterPagerAdapter},
 * in the sense that it keeps track of the currently active {@link MenuItem} and the paired Child
 * {@link Router}. Everytime we navigate from one to another, or {@link
 * Controller#onSaveInstanceState(Bundle)} is called, we save the entire instance state of the Child
 * {@link Router}, and cache it, so we have it available when we navigate to another {@link
 * MenuItem} and can then restore the correct Child {@link Router} (and thus the entire backstack)
 *
 * @author chris6647@gmail.com
 * @see <a
 * href="https://material.io/guidelines/components/bottom-navigation.html#bottom-navigation-behavior">Material
 * Design Guidelines</a>
 */
public abstract class BottomNavigationController extends BaseController {

  public static final String TAG = "BottomNavigationContr";

  private static final String KEY_MENU_RESOURCE = "key_menu_resource";
  private static final String KEY_STATE_ROUTER_BUNDLES = "key_state_router_bundles";
  private static final String KEY_STATE_CURRENTLY_SELECTED_ID = "key_state_currently_selected_id";
  public static final int INVALID_INT = -1;

  @BindView(R.id.navigation)
  protected BottomNavigationView bottomNavigationView;

  @BindView(R.id.bottom_navigation_controller_container)
  ChangeHandlerFrameLayout controllerContainer;

  protected int currentlySelectedItemId = BottomNavigationController.INVALID_INT;

  protected SparseArray<Bundle> routerSavedStateBundles;

  public BottomNavigationController(@MenuRes int menu) {
    this(new BundleBuilder(new Bundle()).putInt(KEY_MENU_RESOURCE, menu).build());
  }

  public BottomNavigationController(Bundle args) {
    super(args);
  }

  @NonNull
  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_bottom_navigation, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    /* Setup the BottomNavigationView with the constructor supplied Menu resource */
    bottomNavigationView.inflateMenu(getMenuResource());
    bottomNavigationView.setOnNavigationItemSelectedListener(
        new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            BottomNavigationController.this.navigateTo(BottomNavigationController.this.getControllerFor(item.getItemId()), false);
            return true;
          }
        });
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (routerSavedStateBundles == null) {
      /*
       * Fresh start, setup everything.
       * Must be done in onAttach to avoid artifacts if using multiple Activities,
       * and in case of resuming the app (i.e. when the view is not created again)
       */
      Menu menu = bottomNavigationView.getMenu();
      int menuSize = menu.size();
      routerSavedStateBundles = new SparseArray<>(menuSize);
      for (int i = 0; i < menuSize; i++) {
        MenuItem menuItem = menu.getItem(i);
        /* Ensure the first checked item is shown */
        if (menuItem.isChecked()) {
          /*
           * Seems like the BottomNavigationView always initializes index 0 as isChecked / Selected,
           * regardless of what was set in the menu xml originally.
           * So basically all we're doing here is always setting up menuItem index 0.
           */
          int itemId = menuItem.getItemId();
          bottomNavigationView.setSelectedItemId(itemId);
          navigateTo(getControllerFor(itemId), false);
          break;
        }
      }
    } else {
      /*
       * Since we are restoring our state,
       * and onRestoreInstanceState is called before onViewBound,
       * all we need to do is rebind.
       */
      getChildRouter(currentlySelectedItemId).rebindIfNeeded();
    }
  }

  /**
   * Get the Child {@link Router} matching the supplied ItemId.
   *
   * @param menuItemId {@link MenuItem} ID
   * @return
   */
  protected Router getChildRouter(@IdRes int menuItemId) {
    return getChildRouter(controllerContainer, "itemId:" + menuItemId);
  }

  /**
   * Remove the supplied {@link Router} as a child of this Controller.
   *
   * @param childRouter
   */
  protected void destroyChildRouter(@NonNull Router childRouter) {
    removeChildRouter(childRouter);
  }

  /**
   * Navigate to the supplied {@link Controller}, while setting the menuItemId as selected on the
   * {@link BottomNavigationView}. Supplied Controller must match the nextMenuItemId as defined in
   * {@link BottomNavigationMenuItem} or an {@link IllegalArgumentException} will be thrown.
   * <p>
   * <p>If this method is to be called when Requires {@link BottomNavigationController} is not
   * attached, it requires {@link BottomNavigationController#getTag()} to return a valid tag, that
   * will return this {@link BottomNavigationController} instance in this {@link Router}'s
   * backstack.
   *
   * @param controller              to navigate to
   * @param shouldOverrideBackstack whether or not to navigate to whatever existing backstack, or to
   *                                set a new one
   */
  public void navigateTo(final @NonNull Controller controller, final boolean shouldOverrideBackstack) {
    if (isSupportedController(controller)) {
      if (isAttached()) {
        @IdRes int menuItemId = getIdForController(controller);
        if (currentlySelectedItemId != menuItemId) {
          /* Navigating to new menuItemId */
          if (currentlySelectedItemId != BottomNavigationController.INVALID_INT) {
            /* Save state of current router before destroying it */
            Router oldChildRouter = getChildRouter(currentlySelectedItemId);
            save(oldChildRouter, currentlySelectedItemId);
            destroyChildRouter(oldChildRouter);
          }
          /* Configure the new childRouter */
          Router newChildRouter = getChildRouter(menuItemId);
          Bundle routerSavedState = routerSavedStateBundles.get(menuItemId);
          if (!shouldOverrideBackstack
              && routerSavedState != null
              && !routerSavedState.isEmpty()) {
            newChildRouter.restoreInstanceState(routerSavedState);
            routerSavedStateBundles.remove(menuItemId);
            newChildRouter.rebindIfNeeded();
          } else {
            newChildRouter.setRoot(RouterTransaction.with(controller));
          }

          ensureMenuSelectionState(menuItemId);
        } else if (currentlySelectedItemId != BottomNavigationController.INVALID_INT) {
          /* Navigating to same menuItemId */
          @IdRes int newMenuItem = shouldOverrideBackstack ? menuItemId : currentlySelectedItemId;
          /* Don't want to save state, because we are resetting it */
          destroyChildRouter(getChildRouter(newMenuItem));
          routerSavedStateBundles.remove(newMenuItem);
          /* Must get reference to newly recreated childRouter to avoid old view not being removed */
          getChildRouter(newMenuItem)
              .setRoot(
                  RouterTransaction.with(controller)
                      .pushChangeHandler(new FadeChangeHandler(true)));
          if (newMenuItem != currentlySelectedItemId) {
            ensureMenuSelectionState(newMenuItem);
          }
        } else {
          Log.e(TAG,
              "Attempted to reset backstack on BottomNavigationController with currentlySelectedItemId=" +
                  currentlySelectedItemId);
        }
      } else {
        /*
         * Navigate to ourselves, and once the view is ready (so we can get the childRouter),
         * navigate as instructed.
         */
        BottomNavigationController.this.addLifecycleListener(
            new Controller.LifecycleListener() {
              @Override
              public void postAttach(@NonNull Controller attachedController, @NonNull View view) {
                super.postAttach(attachedController, view);
                BottomNavigationController.this.removeLifecycleListener(this);
                BottomNavigationController.this.navigateTo(controller, shouldOverrideBackstack);
              }

              @Override
              public void preDestroy(@NonNull Controller controller) {
                super.preDestroy(controller);
                /* Clean ourselves up in case we're destroyed without having been attached */
                BottomNavigationController.this.removeLifecycleListener(this);
              }
            });
        getRouter().popToTag(getTag());
      }
    } else {
      Log.e(TAG, "Attempted to navigate to unsupported controller=" + controller);
    }
  }

  /**
   * Resets the backstack for the menuItemId matching the {@link Controller} found at backstack
   * index 0.
   *
   * @param backstack
   */
  public void navigateTo(final @NonNull List<RouterTransaction> backstack) {
    Controller rootController = backstack.get(0).controller();
    if (isSupportedController(rootController)) {
      int menuItemId = getIdForController(rootController);
      if (isAttached()) {
        destroyChildRouter(getChildRouter(currentlySelectedItemId));
        routerSavedStateBundles.remove(currentlySelectedItemId);
        if (currentlySelectedItemId != menuItemId) {
          ensureMenuSelectionState(menuItemId);
        }
        /* Must get reference to newly recreated childRouter to avoid old view not being removed */
        getChildRouter(currentlySelectedItemId).setBackstack(backstack, new FadeChangeHandler());
      } else {
        /*
         * Navigate to ourselves, and once the view is ready (so we can get the childRouter),
         * navigate as instructed.
         */
        BottomNavigationController.this.addLifecycleListener(
            new Controller.LifecycleListener() {
              @Override
              public void postAttach(@NonNull Controller controller, @NonNull View view) {
                super.postAttach(controller, view);
                BottomNavigationController.this.removeLifecycleListener(this);
                BottomNavigationController.this.navigateTo(backstack);
              }

              @Override
              public void preDestroy(@NonNull Controller controller) {
                super.preDestroy(controller);
                /* Clean ourselves up in case we're destroyed without having been attached */
                BottomNavigationController.this.removeLifecycleListener(this);
              }
            });
        getRouter().popToTag(getTag());
      }
    } else {
      Log.e(TAG,
          "Attempted to navigate to backstack with unsupported root controller=" + rootController);
    }
  }

  /**
   * Ensure correct Checked state based on given menuItemId
   *
   * @param menuItemId
   */
  private void ensureMenuSelectionState(@IdRes int menuItemId) {
    Menu menu = bottomNavigationView.getMenu();
    for (int i = 0; i < menu.size(); i++) {
      MenuItem menuItem = menu.getItem(i);
      if (menuItem.isChecked() && menuItem.getItemId() != menuItemId) {
        menuItem.setChecked(false);
      } else if (menuItem.getItemId() == menuItemId) {
        menuItem.setChecked(true);
        currentlySelectedItemId = menuItemId;
      }
    }
    if (currentlySelectedItemId != menuItemId) {
      Log.e(TAG, "Unexpected BottomNavigation selected Menu Item.");
    }
  }

  /**
   * Saves the Child {@link Router} into a {@link Bundle} and caches that {@link Bundle}.
   * <p>
   * <p>Be cautious as this call causes the controller flag it needs reattach, so it should only be
   * called just prior to destroying the router
   *
   * @param menuItemId {@link MenuItem} ID
   */
  private void save(Router childRouter, @IdRes int menuItemId) {
    Bundle routerBundle = new Bundle();
    childRouter.saveInstanceState(routerBundle);
    routerSavedStateBundles.put(menuItemId, routerBundle);
  }

  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    routerSavedStateBundles = savedInstanceState.getSparseParcelableArray(KEY_STATE_ROUTER_BUNDLES);
    currentlySelectedItemId =
        savedInstanceState.getInt(KEY_STATE_CURRENTLY_SELECTED_ID, BottomNavigationController.INVALID_INT);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    outState.putSparseParcelableArray(KEY_STATE_ROUTER_BUNDLES, routerSavedStateBundles);
    /*
     * For some reason the BottomNavigationView does not seem to correctly restore its
     * selectedId, even though the view appears with the correct state.
     * So we keep track of it manually
     */
    outState.putInt(KEY_STATE_CURRENTLY_SELECTED_ID, currentlySelectedItemId);
  }

  @Override
  public boolean handleBack() {
    /*
     * The childRouter should handleBack,
     * as this BottomNavigationController doesn't have a back step sensible to the user.
     */
    if (controllerContainer != null) {
      Router childRouter = getChildRouter(currentlySelectedItemId);
      if (childRouter != null) {
        return childRouter.handleBack();
      } else {
        Log.e(TAG, "handleBack called with getChildRouter(currentlySelectedItemId) == null.",
            new IllegalStateException(
                "handleBack called with getChildRouter(currentlySelectedItemId) == null."));
      }
    } else {
      Log.e(TAG, "handleBack called with controllerContainer == null."
          , new IllegalStateException("handleBack called with controllerContainer == null."));
    }
    return false;
  }

  /**
   * Get the {@link Menu} Resource ID from {@link Controller#getArgs()}
   *
   * @return the {@link Menu} Resource ID
   */
  private int getMenuResource() {
    return getArgs().getInt(KEY_MENU_RESOURCE);
  }

  /**
   * Return a target instance of {@link Controller} for given menu item ID
   *
   * @param menuItemId the ID tapped by the user
   * @return the {@link Controller} instance to navigate to
   */
  protected abstract @NonNull Controller getControllerFor(@IdRes int menuItemId);

  /**
   * @param controller
   * @return the MenuItemId for matching the given {@link Controller}
   */
  protected abstract @IdRes int getIdForController(@NonNull Controller controller);

  /**
   * @param controller {@link Controller} to test
   * @return whether or not the supplied controller is of a class supported in this {@link
   * BottomNavigationController}
   */
  protected abstract boolean isSupportedController(@NonNull Controller controller);

  /**
   * Used to popToTag (this) {@link BottomNavigationController} instance if needed
   *
   * @return The tag set on the transaction contains this {@link BottomNavigationController}
   * instance
   */
  protected abstract String getTag();
}
