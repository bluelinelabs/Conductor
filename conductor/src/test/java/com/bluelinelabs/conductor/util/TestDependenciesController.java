package com.bluelinelabs.conductor.util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;

import androidx.annotation.NonNull;

public class TestDependenciesController extends Controller {

    public static final String WAS_RECREATED_WITH_BUNDLE_ARGS = "WAS_RECREATED_WITH_BUNDLE_ARGS";
    private static final String INSTANCE_STATE = "INSTANCE_STATE";
    public final Boolean wasCreatedByFactory;
    public Boolean wasInstanceStateRestored = false;
    public final Boolean wasRecreatedWithBundleArgs;

    public TestDependenciesController(Bundle args, Boolean wasCreatedByFactory) {
        super(args);
        this.wasRecreatedWithBundleArgs = args.getBoolean(WAS_RECREATED_WITH_BUNDLE_ARGS, false);
        this.wasCreatedByFactory = wasCreatedByFactory;
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return new AttachFakingFrameLayout(inflater.getContext());
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSTANCE_STATE, true);
    }

    @Override protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        wasInstanceStateRestored = savedInstanceState.getBoolean(INSTANCE_STATE);
    }
}
