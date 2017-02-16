package com.bluelinelabs.conductor.util;

import android.app.Activity;

public class TestActivity extends Activity {

    public boolean isChangingConfigurations = false;
    public boolean isDestroying = false;

    @Override
    public boolean isChangingConfigurations() {
        return isChangingConfigurations;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroying || super.isDestroyed();
    }
}
