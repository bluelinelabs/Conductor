package com.bluelinelabs.conductor.demo.controllers.bottomnavigation;

import android.support.annotation.IdRes;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.bottomnavigation.children.FriendsDepthChildController;
import com.bluelinelabs.conductor.demo.controllers.bottomnavigation.children.HomeDepthChildController;
import com.bluelinelabs.conductor.demo.controllers.bottomnavigation.children.IntroDepthChildController;
import com.bluelinelabs.conductor.demo.controllers.bottomnavigation.children.PlannerDepthChildController;
import com.bluelinelabs.conductor.demo.controllers.bottomnavigation.children.SettingsDepthChildController;
import com.bluelinelabs.conductor.demo.util.Utils;

/** Enum representation of valid Bottom Navigation Menu Items */
public enum BottomNavigationMenuItem {
  HOME(R.id.navigation_home, HomeDepthChildController.class),
  FRIENDS(R.id.navigation_friends, FriendsDepthChildController.class),
  INTRO(R.id.navigation_intro, IntroDepthChildController.class),
  PLANNER(R.id.navigation_planner, PlannerDepthChildController.class),
  SETTINGS(R.id.navigation_settings, SettingsDepthChildController.class);

  private int menuResId;
  private Class<? extends Controller> controllerClass;

  BottomNavigationMenuItem(@IdRes int menuResId, Class<? extends Controller> controllerClass) {
    this.menuResId = menuResId;
    this.controllerClass = controllerClass;
  }

  public static BottomNavigationMenuItem getEnum(@IdRes int menuResId) {
    for (BottomNavigationMenuItem type : BottomNavigationMenuItem.values()) {
      if (menuResId == type.getMenuResId()) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unable to map " + menuResId);
  }

  public static BottomNavigationMenuItem getEnum(Class<? extends Controller> controllerClass) {
    for (BottomNavigationMenuItem type : BottomNavigationMenuItem.values()) {
      if (Utils.equals(controllerClass, type.getControllerClass())) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unable to map " + controllerClass);
  }

  public int getMenuResId() {
    return menuResId;
  }

  public Class<? extends Controller> getControllerClass() {
    return controllerClass;
  }

  @Override
  public String toString() {
    return "BottomNavigationMenuItem{"
        + "menuResId="
        + menuResId
        + ", controllerClass="
        + controllerClass
        + '}';
  }
}
