package com.bluelinelabs.conductor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;

public class DialogController extends RestoreViewOnCreateController
  implements DialogInterface.OnShowListener,
  DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

  /**
   * Style for {@link #setStyle(int, int)}: a basic,
   * normal dialog.
   */
  public static final int STYLE_NORMAL = 0;

  /**
   * Style for {@link #setStyle(int, int)}: don't include
   * a title area.
   */
  public static final int STYLE_NO_TITLE = 1;

  /**
   * Style for {@link #setStyle(int, int)}: don't draw
   * any frame at all; the view hierarchy returned by {@link #onCreateView}
   * is entirely responsible for drawing the dialog.
   */
  public static final int STYLE_NO_FRAME = 2;

  /**
   * Style for {@link #setStyle(int, int)}: like
   * {@link #STYLE_NO_FRAME}, but also disables all input to the dialog.
   * The user can not touch it, and its window will not receive input focus.
   */
  public static final int STYLE_NO_INPUT = 3;

  private static final String SAVED_DIALOG_STATE_TAG = "android:savedDialogState";
  private static final String SAVED_STYLE = "android:style";
  private static final String SAVED_THEME = "android:theme";
  private static final String SAVED_CANCELABLE = "android:cancelable";
  private static final String SAVED_SHOWS_DIALOG = "android:showsDialog";

  int style = STYLE_NORMAL;
  int theme = 0;
  boolean cancelable = true;

  Dialog dialog;
  boolean dismissed;

  @Nullable private View dialogView = null;

  public DialogController() {
    super();
  }

  public DialogController(Bundle args) {
    super(args);
  }

  /**
   * Call to customize the basic appearance and behavior of the
   * fragment's dialog.  This can be used for some common dialog behaviors,
   * taking care of selecting flags, theme, and other options for you.  The
   * same effect can be achieve by manually setting Dialog and Window
   * attributes yourself.  Calling this after the fragment's Dialog is
   * created will have no effect.
   *
   * @param style Selects a standard style: may be {@link #STYLE_NORMAL},
   * {@link #STYLE_NO_TITLE}, {@link #STYLE_NO_FRAME}, or
   * {@link #STYLE_NO_INPUT}.
   * @param theme Optional custom theme.  If 0, an appropriate theme (based
   * on the style) will be selected for you.
   */
  public void setStyle(int style, int theme) {
    this.style = style;
    if (this.style == STYLE_NO_FRAME || this.style == STYLE_NO_INPUT) {
      this.theme = android.R.style.Theme_Panel;
    }
    if (theme != 0) {
      this.theme = theme;
    }
  }

  public Dialog getDialog() {
    return dialog;
  }

  public int getTheme() {
    return theme;
  }

  /**
   * Control whether the shown Dialog is cancelable.  Use this instead of
   * directly calling {@link Dialog#setCancelable(boolean)
   * Dialog.setCancelable(boolean)}, because DialogFragment needs to change
   * its behavior based on this.
   *
   * @param cancelable If true, the dialog is cancelable.  The default
   * is true.
   */
  public void setCancelable(boolean cancelable) {
    this.cancelable = cancelable;
    if (dialog != null)
      dialog.setCancelable(cancelable);
  }

  /**
   * Return the current value of {@link #setCancelable(boolean)}.
   */
  public boolean isCancelable() {
    return cancelable;
  }

  @Nullable public View inflateDialogView(@NonNull LayoutInflater inflater, @Nullable Bundle savedViewState) {
    return null;
  }

  public void onBindDialogView(@NonNull View view, @Nullable Bundle savedViewState) {

  }

  public void setupWindow(@Nullable View view, Window window) {

  }

  @NonNull @Override protected final View onCreateView(@NonNull LayoutInflater inflater,
    @NonNull ViewGroup container, @Nullable Bundle savedViewState) {
    View view = new View(inflater.getContext());
    onBindView(view, savedViewState);
    return view;
  }

  private void onBindView(@NonNull View view, @Nullable Bundle savedViewState) {
    dialog = onCreateDialog();
    if (dialog != null) {

      dismissed = false;

      switch (style) {
        case STYLE_NO_INPUT:
          dialog.getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
              WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
          // fall through...
        case STYLE_NO_FRAME:
        case STYLE_NO_TITLE:
          dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      }

      if (dialogView == null) {
        dialogView = inflateDialogView(LayoutInflater.from(dialog.getContext()), savedViewState);
      }

      if (dialogView != null) {
        dialog.setContentView(dialogView);
      }

      setupWindow(dialogView, dialog.getWindow());

      if (dialogView != null) {
        onBindDialogView(dialogView, savedViewState);
      }

      dialog.setOwnerActivity(getActivity());
      dialog.setCancelable(cancelable);
      dialog.setOnShowListener(this);
      dialog.setOnCancelListener(this);
      dialog.setOnDismissListener(this);
    }
  }

  public Dialog onCreateDialog() {
    return new Dialog(getActivity(), theme);
  }

  @Override public void onCancel(DialogInterface dialog) {
  }

  @Override public void onDismiss(DialogInterface dialog) {
    dismiss();
  }

  @Override public void onShow(DialogInterface dialogInterface) {

  }

  /**
   * Dismiss the dialog and pop this controller
   */
  public void dismiss() {
    if (dismissed) {
      return;
    }

    if (dialog != null) {
      dialog.dismiss();
    }
    dismissed = true;

    router.popController(this);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (dialog != null) {
      Bundle dialogState = dialog.onSaveInstanceState();
      if (dialogState != null) {
        outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState);
      }
    }
    if (style != STYLE_NORMAL) {
      outState.putInt(SAVED_STYLE, style);
    }
    if (theme != 0) {
      outState.putInt(SAVED_THEME, theme);
    }
    if (!cancelable) {
      outState.putBoolean(SAVED_CANCELABLE, cancelable);
    }
    if (!dismissed) {
      outState.putBoolean(SAVED_SHOWS_DIALOG, dismissed);
    }
  }

  @Override protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    style = savedInstanceState.getInt(SAVED_STYLE, STYLE_NORMAL);
    theme = savedInstanceState.getInt(SAVED_THEME, 0);
    cancelable = savedInstanceState.getBoolean(SAVED_CANCELABLE, true);
    dismissed = savedInstanceState.getBoolean(SAVED_SHOWS_DIALOG, dismissed);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (dialog != null) {
      dialog.show();
    }
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (dialog != null) {
      dialog.hide();
    }
  }
  /**
   * Remove dialog.
   */
  @Override protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    if (dialog != null) {
      // Set removed here because this dismissal is just to hide
      // the dialog -- we don't want this to cause the fragment to
      // actually be removed.
      dialog.setOnShowListener(null);
      dialog.setOnCancelListener(null);
      dialog.setOnDismissListener(null);
      dialog.dismiss();
      dialog = null;

      dialogView = null;
      dismissed = true;
    }
  }

  public void show(Router router, @Nullable String tag) {
    RouterTransaction transaction = RouterTransaction.with(this).tag(tag);
    transaction.pushChangeHandler(new SimpleSwapChangeHandler(false));
    transaction.popChangeHandler(new SimpleSwapChangeHandler());
    router.pushController(transaction);
  }
}
