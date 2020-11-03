package com.bluelinelabs.conductor.demo.util;


public class Utils {

  /**
   * Copy/paste from {@link java.util.Objects#equals(Object, Object)} to support lower API version
   *
   * @param a
   * @param b
   * @return
   */
  public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }
}
