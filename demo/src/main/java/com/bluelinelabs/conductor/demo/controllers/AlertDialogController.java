package com.bluelinelabs.conductor.demo.controllers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.bluelinelabs.conductor.DialogController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;

public class AlertDialogController extends DialogController {

    private static final String KEY_TITLE = "DialogController.title";
    private static final String KEY_DESCRIPTION = "DialogController.description";

    public AlertDialogController(CharSequence title, CharSequence description) {
        super(new BundleBuilder(new Bundle())
                .putCharSequence(KEY_TITLE, title)
                .putCharSequence(KEY_DESCRIPTION, description)
                .build());
    }

    public AlertDialogController(Bundle args) {
        super(args);
    }

    @NonNull
    @Override
    protected Dialog onCreateDialog(@Nullable Bundle savedViewState) {
        //noinspection ConstantConditions
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getArgs().getCharSequence(KEY_TITLE))
                .setMessage(getArgs().getCharSequence(KEY_DESCRIPTION))
                .setPositiveButton(android.R.string.ok, null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TextView messageTextView = ((TextView) alertDialog.findViewById(android.R.id.message));
                if (messageTextView != null) {
                    //Make links clickable in textview
                    messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        });

        return alertDialog;
    }
}
