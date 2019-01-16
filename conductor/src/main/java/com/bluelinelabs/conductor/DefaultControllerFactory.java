package com.bluelinelabs.conductor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class DefaultControllerFactory implements ControllerFactory {

    static final DefaultControllerFactory INSTANCE = new DefaultControllerFactory();

    @Nullable
    @Override
    public final Controller create(@NonNull String controllerName, @Nullable Bundle args) {
        return null;
    }
}
