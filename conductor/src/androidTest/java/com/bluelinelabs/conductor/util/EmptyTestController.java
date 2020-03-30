package com.bluelinelabs.conductor.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.bluelinelabs.conductor.Controller;

public class EmptyTestController extends Controller {
    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return new FrameLayout(container.getContext());
    }
}
