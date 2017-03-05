package com.bluelinelabs.conductor.demo.util;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

public class BundleBuilder {

    private final Bundle bundle;

    public BundleBuilder(Bundle bundle) {
        this.bundle = bundle;
    }

    public BundleBuilder putAll(Bundle bundle) {
        this.bundle.putAll(bundle);
        return this;
    }

    public BundleBuilder putBoolean(String key, boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleBuilder putBooleanArray(String key, boolean[] value) {
        bundle.putBooleanArray(key, value);
        return this;
    }

    public BundleBuilder putDouble(String key, double value) {
        bundle.putDouble(key, value);
        return this;
    }

    public BundleBuilder putDoubleArray(String key, double[] value) {
        bundle.putDoubleArray(key, value);
        return this;
    }

    public BundleBuilder putLong(String key, long value) {
        bundle.putLong(key, value);
        return this;
    }

    public BundleBuilder putLongArray(String key, long[] value) {
        bundle.putLongArray(key, value);
        return this;
    }

    public BundleBuilder putString(String key, String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleBuilder putStringArray(String key, String[] value) {
        bundle.putStringArray(key, value);
        return this;
    }

    public BundleBuilder putBundle(String key, Bundle value) {
        bundle.putBundle(key, value);
        return this;
    }

    public BundleBuilder putByte(String key, byte value) {
        bundle.putByte(key, value);
        return this;
    }

    public BundleBuilder putByteArray(String key, byte[] value) {
        bundle.putByteArray(key, value);
        return this;
    }

    public BundleBuilder putChar(String key, char value) {
        bundle.putChar(key, value);
        return this;
    }

    public BundleBuilder putCharArray(String key, char[] value) {
        bundle.putCharArray(key, value);
        return this;
    }

    public BundleBuilder putCharSequence(String key, CharSequence value) {
        bundle.putCharSequence(key, value);
        return this;
    }

    public BundleBuilder putCharSequenceArray(String key, CharSequence[] value) {
        bundle.putCharSequenceArray(key, value);
        return this;
    }

    public BundleBuilder putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        bundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public BundleBuilder putInt(String key, int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleBuilder putIntArray(String key, int[] value) {
        bundle.putIntArray(key, value);
        return this;
    }

    public BundleBuilder putFloat(String key, float value) {
        bundle.putFloat(key, value);
        return this;
    }

    public BundleBuilder putFloatArray(String key, float[] value) {
        bundle.putFloatArray(key, value);
        return this;
    }

    public BundleBuilder putIntegerArrayList(String key, ArrayList<Integer> value) {
        bundle.putIntegerArrayList(key, value);
        return this;
    }

    public BundleBuilder putParcelable(String key, Parcelable value) {
        bundle.putParcelable(key, value);
        return this;
    }

    public BundleBuilder putParcelableArray(String key, Parcelable[] value) {
        bundle.putParcelableArray(key, value);
        return this;
    }

    public BundleBuilder putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        bundle.putParcelableArrayList(key, value);
        return this;
    }

    public BundleBuilder putSerializable(String key, Serializable value) {
        bundle.putSerializable(key, value);
        return this;
    }

    public BundleBuilder putShort(String key, short value) {
        bundle.putShort(key, value);
        return this;
    }

    public BundleBuilder putShortArray(String key, short[] value) {
        bundle.putShortArray(key, value);
        return this;
    }

    public BundleBuilder putSparseParcelableArray(String key, SparseArray<? extends Parcelable> value) {
        bundle.putSparseParcelableArray(key, value);
        return this;
    }

    public BundleBuilder putStringArrayList(String key, ArrayList<String> value) {
        bundle.putStringArrayList(key, value);
        return this;
    }

    public Bundle build() {
        return bundle;
    }

}
