package com.bluelinelabs.conductor;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bluelinelabs.conductor.internal.ViewAttachHandler;
import com.bluelinelabs.conductor.internal.ViewAttachHandler.ViewAttachListener;
import com.bluelinelabs.conductor.util.ActivityProxy;
import com.bluelinelabs.conductor.util.ViewUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewAttachHandlerTests {

    private Activity activity;
    private ViewAttachHandler viewAttachHandler;
    private CountingViewAttachListener viewAttachListener;

    @Before
    public void setup() {
        activity = new ActivityProxy().create(null).getActivity();
        viewAttachListener = new CountingViewAttachListener();
        viewAttachHandler = new ViewAttachHandler(viewAttachListener);
    }

    @Test
    public void testSimpleViewAttachDetach() {
        View view = new View(activity);
        viewAttachHandler.listenForAttach(view);

        assertEquals(0, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        viewAttachHandler.onActivityStopped();
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);

        viewAttachHandler.onActivityStarted();
        assertEquals(3, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);
    }

    @Test
    public void testSimpleViewGroupAttachDetach() {
        View view = new View(activity);
        viewAttachHandler.listenForAttach(view);

        assertEquals(0, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        viewAttachHandler.onActivityStopped();
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);

        viewAttachHandler.onActivityStarted();
        assertEquals(3, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);
    }

    @Test
    public void testNestedViewGroupAttachDetach() {
        ViewGroup view = new LinearLayout(activity);
        View child = new LinearLayout(activity);
        view.addView(child);
        viewAttachHandler.listenForAttach(view);

        assertEquals(0, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true, false);
        assertEquals(0, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(child, true, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true, false);
        ViewUtils.reportAttached(child, true, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(0, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true, false);
        assertEquals(1, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(child, true, false);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(1, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        viewAttachHandler.onActivityStopped();
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(0, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, false, false);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);

        ViewUtils.reportAttached(view, true, false);
        ViewUtils.reportAttached(child, true, false);
        assertEquals(2, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);

        viewAttachHandler.onActivityStarted();
        assertEquals(3, viewAttachListener.attaches);
        assertEquals(2, viewAttachListener.detaches);
        assertEquals(1, viewAttachListener.detachAfterStops);
    }

    private static class CountingViewAttachListener implements ViewAttachListener {
        int attaches;
        int detaches;
        int detachAfterStops;

        @Override
        public void onAttached() {
            attaches++;
        }

        @Override
        public void onDetached(boolean fromActivityStop) {
            detaches++;
        }

        @Override
        public void onViewDetachAfterStop() {
            detachAfterStops++;
        }
    }

}
