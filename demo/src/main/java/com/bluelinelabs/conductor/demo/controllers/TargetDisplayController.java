package com.bluelinelabs.conductor.demo.controllers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.TargetTitleEntryController.TargetTitleEntryControllerListener;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.OnClick;

public class TargetDisplayController extends BaseController implements TargetTitleEntryControllerListener {

    private static final int REQUEST_SELECT_IMAGE = 126;

    private static final String KEY_SELECTED_TEXT = "TargetDisplayController.selectedText";
    private static final String KEY_SELECTED_IMAGE = "TargetDisplayController.selectedImage";

    @Bind(R.id.tv_selection) TextView mTvSelection;
    @Bind(R.id.image_view) ImageView mImageView;

    private String mSelectedText;
    private Uri mImageUri;

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_target_display, container, false);
    }

    @OnClick(R.id.btn_pick_title) void launchTitlePicker() {
        getRouter().pushController(RouterTransaction.builder(new TargetTitleEntryController(this))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler())
                .build());
    }

    @OnClick(R.id.btn_pick_image) void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_SELECT_IMAGE);
    }

    @Override
    public void onTitlePicked(String option) {
        mSelectedText = option;
        setTextView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            mImageUri = data.getData();
            setImageView();
        }
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        setTextView();
        setImageView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SELECTED_TEXT, mSelectedText);
        outState.putString(KEY_SELECTED_IMAGE, mImageUri != null ? mImageUri.toString() : null);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedText = savedInstanceState.getString(KEY_SELECTED_TEXT);

        String uriString = savedInstanceState.getString(KEY_SELECTED_IMAGE);
        if (!TextUtils.isEmpty(uriString)) {
            mImageUri = Uri.parse(uriString);
        }
    }

    @Override
    protected String getTitle() {
        return "Target Controller Demo";
    }

    private void setImageView() {
        Picasso.with(getActivity())
                .load(mImageUri)
                .fit()
                .centerCrop()
                .into(mImageView);
    }

    private void setTextView() {
        if (mTvSelection != null) {
            if (!TextUtils.isEmpty(mSelectedText)) {
                mTvSelection.setText(mSelectedText);
            } else {
                mTvSelection.setText("Press pick title to set this title, or pick image to fill in the image view.");
            }
        }
    }
}
