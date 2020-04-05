package com.bluelinelabs.conductor;

import android.os.Bundle;

import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bluelinelabs.conductor.util.TestController;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ControllerTransactionTests {

    @Test
    public void testRouterSaveRestore() {
        RouterTransaction transaction = RouterTransaction.with(new TestController())
                .pushChangeHandler(new HorizontalChangeHandler())
                .isDetail(true)
                .popChangeHandler(new VerticalChangeHandler())
                .tag("Test Tag");

        Bundle bundle = transaction.saveInstanceState();

        RouterTransaction restoredTransaction = new RouterTransaction(bundle);

        assertEquals(transaction.controller().getClass(), restoredTransaction.controller().getClass());
        assertEquals(transaction.pushChangeHandler().getClass(), restoredTransaction.pushChangeHandler().getClass());
        assertEquals(transaction.popChangeHandler().getClass(), restoredTransaction.popChangeHandler().getClass());
        assertEquals(transaction.tag(), restoredTransaction.tag());
        assertEquals(transaction.isDetail(), restoredTransaction.isDetail());
    }
}
