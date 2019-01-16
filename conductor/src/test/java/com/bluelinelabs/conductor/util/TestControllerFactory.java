package com.bluelinelabs.conductor.util;

import android.os.Bundle;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TestControllerFactory implements ControllerFactory {
    @Nullable
    @Override
    public Controller create(@NonNull String controllerName, @Nullable Bundle args) {
        if (controllerName.equals(TestDependenciesController.class.getName())) {
            return new TestDependenciesController(args, true);
        }
        return null;
    }
}
