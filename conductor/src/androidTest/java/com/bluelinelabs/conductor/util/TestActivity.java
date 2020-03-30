package com.bluelinelabs.conductor.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.test.R;

public class TestActivity extends Activity {

    public Router router;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        router = Conductor.attachRouter(this, (ViewGroup) findViewById(R.id.test_root), savedInstanceState);
    }
}
