package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.IdRes;

import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;

import junit.framework.Assert;

import org.junit.Test;

public class ControllerTransactionTests {

    @Test
    public void testRouterSaveRestore() {
        RouterTransaction transaction = RouterTransaction.builder(new TestController())
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new VerticalChangeHandler())
                .tag("Test Tag")
                .build();

        Bundle bundle = transaction.saveInstanceState();

        RouterTransaction restoredTransaction = new RouterTransaction(bundle);

        Assert.assertEquals(transaction.getController().getClass(), restoredTransaction.getController().getClass());
        Assert.assertEquals(transaction.getPushControllerChangeHandler().getClass(), restoredTransaction.getPushControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.getPopControllerChangeHandler().getClass(), restoredTransaction.getPopControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.getTag(), restoredTransaction.getTag());
    }

    @Test
    public void testChildSaveRestore() {
        @IdRes int layoutId = 234;
        ChildControllerTransaction transaction = ChildControllerTransaction.builder(new TestController(), layoutId)
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new VerticalChangeHandler())
                .tag("Test Tag")
                .build();

        Bundle bundle = transaction.saveInstanceState();

        ChildControllerTransaction restoredTransaction = new ChildControllerTransaction(bundle);

        Assert.assertEquals(transaction.containerId, restoredTransaction.containerId);
        Assert.assertEquals(transaction.getController().getClass(), restoredTransaction.getController().getClass());
        Assert.assertEquals(transaction.getPushControllerChangeHandler().getClass(), restoredTransaction.getPushControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.getPopControllerChangeHandler().getClass(), restoredTransaction.getPopControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.getTag(), restoredTransaction.getTag());
    }
}
