package com.bluelinelabs.conductor.support.util;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller;

public class TestController extends Controller {

    @Override @NonNull
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return new FrameLayout(inflater.getContext());
    }

}
