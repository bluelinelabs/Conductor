package com.bluelinelabs.conductor.demo.controllers.bottomnavigation;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.demo.R;

import java.lang.reflect.Constructor;

/**
 * An implementation of the bottom navigation controller
 * @author chris6647@gmail.com
 */
public class DemoBottomNavigationController extends BottomNavigationController {

  public static final String TAG = "DemoBottomNavi";

  public DemoBottomNavigationController() {
    super(R.menu.navigation);
  }

  /**
   * Supplied MenuItemId must match a {@link Controller} as defined in {@link
   * BottomNavigationMenuItem} or an {@link IllegalArgumentException} will be thrown.
   *
   * @param itemId
   */
  @Override
  protected @NonNull
  Controller getControllerFor(@IdRes int itemId) {
    BottomNavigationMenuItem bottomNavigationMenuItem = BottomNavigationMenuItem.getEnum(itemId);
    Constructor[] constructors = bottomNavigationMenuItem.getControllerClass().getConstructors();
    Controller controller = null;
    try {
      /* Determine default constructor */
      for (Constructor constructor : constructors) {
        if (constructor.getParameterTypes().length == 0) {
          controller = (Controller) constructor.newInstance();
          break;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "An exception occurred while creating a new instance for mapping of "
              + itemId
              + ". "
              + e.getMessage(),
          e);
    }

    if (controller == null) {
      throw new RuntimeException("Controller must have a public an empty constructor. " + itemId);
    }
    return controller;
  }

  @Override
  protected boolean isSupportedController(@NonNull Controller controller) {
    return BottomNavigationMenuItem.getEnum(controller.getClass()) != null;
  }

  @Override
  protected @IdRes int getIdForController(@NonNull Controller controller) {
    return BottomNavigationMenuItem.getEnum(controller.getClass()).getMenuResId();
  }

  @Override
  protected String getTag() {
    return DemoBottomNavigationController.TAG;
  }

}
