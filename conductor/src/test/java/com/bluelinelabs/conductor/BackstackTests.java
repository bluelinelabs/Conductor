package com.bluelinelabs.conductor;

import com.bluelinelabs.conductor.util.TestController;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BackstackTests {

    private Backstack backstack;

    @Before
    public void setup() {
        backstack = new Backstack();
    }

    @Test
    public void testPush() {
        assertEquals(0, backstack.size());
        backstack.push(RouterTransaction.with(new TestController()));
        assertEquals(1, backstack.size());
    }

    @Test
    public void testPop() {
        backstack.push(RouterTransaction.with(new TestController()));
        backstack.push(RouterTransaction.with(new TestController()));
        assertEquals(2, backstack.size());
        backstack.pop();
        assertEquals(1, backstack.size());
        backstack.pop();
        assertEquals(0, backstack.size());
    }

    @Test
    public void testPeek() {
        RouterTransaction transaction1 = RouterTransaction.with(new TestController());
        RouterTransaction transaction2 = RouterTransaction.with(new TestController());

        backstack.push(transaction1);
        assertEquals(transaction1, backstack.peek());

        backstack.push(transaction2);
        assertEquals(transaction2, backstack.peek());

        backstack.pop();
        assertEquals(transaction1, backstack.peek());
    }

    @Test
    public void testPopTo() {
        RouterTransaction transaction1 = RouterTransaction.with(new TestController());
        RouterTransaction transaction2 = RouterTransaction.with(new TestController());
        RouterTransaction transaction3 = RouterTransaction.with(new TestController());

        backstack.push(transaction1);
        backstack.push(transaction2);
        backstack.push(transaction3);

        assertEquals(3, backstack.size());

        backstack.popTo(transaction1);

        assertEquals(1, backstack.size());
        assertEquals(transaction1, backstack.peek());
    }
}
