package com.bluelinelabs.conductor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ControllerFactory {
    @Nullable
    Controller create(@NonNull String controllerName, @Nullable Bundle args);
}
