package com.bluelinelabs.conductor.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

public class ClassUtils {

    @Nullable @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> classForName(@NonNull String className, boolean allowEmptyName) {
        if (allowEmptyName && TextUtils.isEmpty(className)) {
            return null;
        }

        try {
            return (Class<? extends T>)Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while finding class for name " + className + ". " + e.getMessage());
        }
    }

    @Nullable @SuppressWarnings("unchecked")
    public static <T> T newInstance(@NonNull String className) {
        try {
            Class<? extends T> cls = classForName(className, true);
            return cls != null ? cls.newInstance() : null;
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while creating a new instance of " + className + ". " + e.getMessage());
        }
    }

}
