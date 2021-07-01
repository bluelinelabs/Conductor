package com.bluelinelabs.conductor;

import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ControllerChangeHandlerTests {

    @Test
    public void testSaveRestore() {
        HorizontalChangeHandler horizontalChangeHandler = new HorizontalChangeHandler();
        FadeChangeHandler fadeChangeHandler = new FadeChangeHandler(120, false);

        RouterTransaction transaction = RouterTransaction.with(new TestController())
                .pushChangeHandler(horizontalChangeHandler)
                .popChangeHandler(fadeChangeHandler);
        RouterTransaction restoredTransaction = new RouterTransaction(transaction.saveInstanceState());

        ControllerChangeHandler restoredHorizontal = restoredTransaction.pushChangeHandler();
        ControllerChangeHandler restoredFade = restoredTransaction.popChangeHandler();

        assertEquals(horizontalChangeHandler.getClass(), restoredHorizontal.getClass());
        assertEquals(fadeChangeHandler.getClass(), restoredFade.getClass());

        HorizontalChangeHandler restoredHorizontalCast = (HorizontalChangeHandler) restoredHorizontal;
        FadeChangeHandler restoredFadeCast = (FadeChangeHandler) restoredFade;

        assertEquals(horizontalChangeHandler.getAnimationDuration(), restoredHorizontalCast.getAnimationDuration());
        assertEquals(horizontalChangeHandler.removesFromViewOnPush(), restoredHorizontalCast.removesFromViewOnPush());

        assertEquals(fadeChangeHandler.getAnimationDuration(), restoredFadeCast.getAnimationDuration());
        assertEquals(fadeChangeHandler.removesFromViewOnPush(), restoredFadeCast.removesFromViewOnPush());
    }

}
