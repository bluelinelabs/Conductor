package com.bluelinelabs.conductor.support;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.SparseArray;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.support.util.FakePager;
import com.bluelinelabs.conductor.support.util.TestController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StateSaveTests {

    private FakePager pager;
    private RouterPagerAdapter pagerAdapter;

    public void createActivityController(Bundle savedInstanceState) {
        ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class).create().start().resume();
        Router router = Conductor.attachRouter(activityController.get(), new FrameLayout(activityController.get()), savedInstanceState);
        TestController controller = new TestController();
        router.setRoot(RouterTransaction.with(controller));

        pager = new FakePager(new FrameLayout(activityController.get()));
        pager.setOffscreenPageLimit(1);

        pagerAdapter = new RouterPagerAdapter(controller) {
            @Override
            public void configureRouter(@NonNull Router router, int position) {
                if (!router.hasRootController()) {
                    router.setRoot(RouterTransaction.with(new TestController()));
                }
            }

            @Override
            public int getCount() {
                return 20;
            }
        };

        pager.setAdapter(pagerAdapter);
    }

    @Before
    public void setup() {
        createActivityController(null);
    }

    @Test
    public void testNoMaxSaves() {
        // Load all pages
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            pager.pageTo(i);
        }

        pager.pageTo(pagerAdapter.getCount() / 2);

        // Ensure all non-visible pages are saved
        assertEquals(pagerAdapter.getCount() - 1 - pager.getOffscreenPageLimit() * 2, pagerAdapter.getSavedPages().size());
    }

    @Test
    public void testMaxSavedSet() {
        final int maxPages = 3;
        pagerAdapter.setMaxPagesToStateSave(maxPages);

        // Load all pages
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            pager.pageTo(i);
        }

        final int firstSelectedItem = pagerAdapter.getCount() / 2;
        pager.pageTo(firstSelectedItem);

        SparseArray<Bundle> savedPages = pagerAdapter.getSavedPages();

        // Ensure correct number of pages are saved
        assertEquals(maxPages, savedPages.size());

        // Ensure correct pages are saved
        assertEquals(pagerAdapter.getCount() - 3, savedPages.keyAt(0));
        assertEquals(pagerAdapter.getCount() - 2, savedPages.keyAt(1));
        assertEquals(pagerAdapter.getCount() - 1, savedPages.keyAt(2));

        final int secondSelectedItem = 1;
        pager.pageTo(secondSelectedItem);

        savedPages = pagerAdapter.getSavedPages();

        // Ensure correct number of pages are saved
        assertEquals(maxPages, savedPages.size());

        // Ensure correct pages are saved
        assertEquals(firstSelectedItem - 1, savedPages.keyAt(0));
        assertEquals(firstSelectedItem, savedPages.keyAt(1));
        assertEquals(firstSelectedItem + 1, savedPages.keyAt(2));
    }

}
