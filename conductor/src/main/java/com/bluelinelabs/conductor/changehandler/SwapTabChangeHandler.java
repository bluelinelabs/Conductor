package com.bluelinelabs.conductor.changehandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;

/**
 * A {@link ControllerChangeHandler} that will instantly swap Views with no animations or transitions.
 */
public class SwapTabChangeHandler extends SimpleSwapChangeHandler {

  public SwapTabChangeHandler() {
  }


  @Override
  public void performChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
    if (!canceled) {
      if (from != null) {
        container.removeView(from);
      }

      if (to != null && to.getParent() == null) {
        container.addView(to);
      }
    }

    if (container.getWindowToken() != null) {
      changeListener.onChangeCompleted();
    } else {
      this.changeListener = changeListener;
      this.container = container;
      container.addOnAttachStateChangeListener(this);
    }

  }
}
