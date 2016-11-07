package com.bluelinelabs.conductor.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.controllers.HomeController;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class MainActivity extends AppCompatActivity implements ActionBarProvider {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.controller_container) ViewGroup container;

    private Router router;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PATH_PREFIX = "/demo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        router = Conductor.attachRouter(this, container, savedInstanceState);
        if (!router.hasRootController()) {

            HomeController homeController = new HomeController();
            router.setRoot(RouterTransaction.with(homeController));
            handleIntentDataUri(homeController);
        }
    }

    /**
     * will handle the data URI of the intent and when it is valid, navigate to the matching
     * controller
     *
     * EXAMPLE: to test this via command line
     * <code>
     * <pre>
     * adb shell am start -W -a android.intent.action.VIEW
     *   -d "http://bluelinelabs.com/demo/NAVIGATION"
     *   com.bluelinelabs.conductor.demo
     * </pre>
     * </code>
     *
     * @param homeController the {@link HomeController} will handle the actual navigation
     */
    private void handleIntentDataUri(HomeController homeController) {
        final Intent intent = getIntent();
        Uri intentDataUri = intent.getData();
        if (intentDataUri == null || intentDataUri.getPath() == null) return;

        String path = intentDataUri.getPath();
        if (!path.startsWith(PATH_PREFIX)) {
            Log.w(TAG, "unexpected path: " + intentDataUri.getPath());
            return;
        }

        /* Example: http://bluelinelabs.com/demo/MASTER_DETAIL?item=1

           intentDataUri.getPath()  = /demo/MASTER_DETAIL
           intentDataUri.getQuery() = item=1
        */

        // skip the expected prefix, so that the remaining part is e.g. MASTER_DETAIL
        String navigationPath = path.substring(PATH_PREFIX.length());
        if (!homeController.navigateTo(navigationPath)) {
            Log.w(TAG, "cannot navigate to: " + intentDataUri.getPath());
        }
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

}
