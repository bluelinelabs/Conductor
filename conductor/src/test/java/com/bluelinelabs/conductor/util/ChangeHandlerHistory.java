package com.bluelinelabs.conductor.util;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ChangeHandlerHistory {

    private List<Entry> entries = new ArrayList<>();
    public boolean isValidHistory = true;

    public void addEntry(View from, View to, boolean isPush, MockChangeHandler handler) {
        entries.add(new Entry(from, to, isPush, handler));
    }

    public int size() {
        return entries.size();
    }

    public View fromViewAt(int index) {
        return entries.get(index).from;
    }

    public View toViewAt(int index) {
        return entries.get(index).to;
    }

    public boolean isPushAt(int index) {
        return entries.get(index).isPush;
    }

    public MockChangeHandler changeHandlerAt(int index) {
        return entries.get(index).changeHandler;
    }

    public View latestFromView() {
        return fromViewAt(size() - 1);
    }

    public View latestToView() {
        return toViewAt(size() - 1);
    }

    public boolean latestIsPush() {
        return isPushAt(size() - 1);
    }

    public MockChangeHandler latestChangeHandler() {
        return changeHandlerAt(size() - 1);
    }

    private static class Entry {
        final View from;
        final View to;
        final boolean isPush;
        final MockChangeHandler changeHandler;

        Entry(View from, View to, boolean isPush, MockChangeHandler changeHandler) {
            this.from = from;
            this.to = to;
            this.isPush = isPush;
            this.changeHandler = changeHandler;
        }
    }

}
