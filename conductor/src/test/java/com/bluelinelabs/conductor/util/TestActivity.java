package com.bluelinelabs.conductor.util;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

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
