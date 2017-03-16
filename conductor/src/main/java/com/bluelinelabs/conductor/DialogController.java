package com.bluelinelabs.conductor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;

/**
 * @author Dmitriy Gorbunov
 */

public abstract class DialogController extends Controller {

    private static final String SAVED_DIALOG_STATE_TAG = "android:savedDialogState";

    private Dialog mDialog;
    private boolean mDismissed;

    public DialogController(@Nullable Bundle args) {super(args);}

    @NonNull
    @Override
    final protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        mDialog = onCreateDialog(getActivity());
        mDialog.setOwnerActivity(getActivity());
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dismissDialog();
            }
        });
        return new View(getActivity());//stub view
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Bundle dialogState = savedInstanceState.getBundle(SAVED_DIALOG_STATE_TAG);
        if (dialogState != null) {
            mDialog.onRestoreInstanceState(dialogState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDialog != null) {
            Bundle dialogState = mDialog.onSaveInstanceState();
            outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState);
        }
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        mDialog.show();
    }

    @Override
    protected void onDetach(@NonNull View view) {
        super.onDetach(view);
        mDialog.hide();
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);
        mDialog.setOnDismissListener(null);
        mDialog.dismiss();
        mDialog = null;
    }

    public void showDialog(@NonNull Router router) {
        mDismissed = false;
        router.pushController(RouterTransaction.with(this)
                .pushChangeHandler(new FadeChangeHandler(false))
                .popChangeHandler(new FadeChangeHandler()));
    }

    public void dismissDialog() {
        if (mDismissed) {
            return;
        }
        getRouter().popCurrentController();
        mDismissed = true;
    }

    @NonNull
    protected abstract Dialog onCreateDialog(@NonNull Context context);
}
