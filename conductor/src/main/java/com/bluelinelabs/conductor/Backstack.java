package com.bluelinelabs.conductor;

import android.os.Bundle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class Backstack implements Iterable<RouterTransaction> {

    private static final String KEY_ENTRIES = "Backstack.entries";

    private final ArrayDeque<RouterTransaction> mBackStack = new ArrayDeque<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        return mBackStack.isEmpty();
    }

    public int size() {
        return mBackStack.size();
    }

    public RouterTransaction root() {
        return mBackStack.size() > 0 ? mBackStack.getLast() : null;
    }

    @Override
    public Iterator<RouterTransaction> iterator() {
        return mBackStack.iterator();
    }

    public Iterator<RouterTransaction> reverseIterator() {
        return mBackStack.descendingIterator();
    }

    public List<RouterTransaction> popTo(RouterTransaction transaction) {
        List<RouterTransaction> popped = new ArrayList<>();
        if (mBackStack.contains(transaction)) {
            while (mBackStack.peek() != transaction) {
                RouterTransaction poppedTransaction = pop();
                popped.add(poppedTransaction);
            }
        } else {
            throw new RuntimeException("Tried to pop to a transaction that was not on the back stack");
        }
        return popped;
    }

    public RouterTransaction pop() {
        RouterTransaction popped = mBackStack.pop();
        popped.getController().destroy();
        return popped;
    }

    public RouterTransaction peek() {
        return mBackStack.peek();
    }

    public void remove(RouterTransaction transaction) {
        mBackStack.removeFirstOccurrence(transaction);
    }

    public void push(RouterTransaction transaction) {
        mBackStack.push(transaction);
    }

    public List<RouterTransaction> popAll() {
        List<RouterTransaction> list = new ArrayList<>();
        while (!isEmpty()) {
            list.add(pop());
        }
        return list;
    }

    public void saveInstanceState(Bundle outState) {
        ArrayList<Bundle> entryBundles = new ArrayList<>(mBackStack.size());
        for (RouterTransaction entry : mBackStack) {
            entryBundles.add(entry.saveInstanceState());
        }

        outState.putParcelableArrayList(KEY_ENTRIES, entryBundles);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        ArrayList<Bundle> entryBundles = savedInstanceState.getParcelableArrayList(KEY_ENTRIES);
        if (entryBundles != null) {
            Collections.reverse(entryBundles);
            for (Bundle transactionBundle : entryBundles) {
                mBackStack.push(new RouterTransaction(transactionBundle));
            }
        }
    }
}
