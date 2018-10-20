package com.bluelinelabs.conductor.demo.kotlin.util

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable
import java.util.*

class BundleBuilder(private val bundle: Bundle) {

    fun putAll(bundle: Bundle): BundleBuilder {
        this.bundle.putAll(bundle)
        return this
    }

    fun putBoolean(key: String, value: Boolean): BundleBuilder {
        bundle.putBoolean(key, value)
        return this
    }

    fun putBooleanArray(key: String, value: BooleanArray): BundleBuilder {
        bundle.putBooleanArray(key, value)
        return this
    }

    fun putDouble(key: String, value: Double): BundleBuilder {
        bundle.putDouble(key, value)
        return this
    }

    fun putDoubleArray(key: String, value: DoubleArray): BundleBuilder {
        bundle.putDoubleArray(key, value)
        return this
    }

    fun putLong(key: String, value: Long): BundleBuilder {
        bundle.putLong(key, value)
        return this
    }

    fun putLongArray(key: String, value: LongArray): BundleBuilder {
        bundle.putLongArray(key, value)
        return this
    }

    fun putString(key: String, value: String): BundleBuilder {
        bundle.putString(key, value)
        return this
    }

    fun putStringArray(key: String, value: Array<String>): BundleBuilder {
        bundle.putStringArray(key, value)
        return this
    }

    fun putBundle(key: String, value: Bundle): BundleBuilder {
        bundle.putBundle(key, value)
        return this
    }

    fun putByte(key: String, value: Byte): BundleBuilder {
        bundle.putByte(key, value)
        return this
    }

    fun putByteArray(key: String, value: ByteArray): BundleBuilder {
        bundle.putByteArray(key, value)
        return this
    }

    fun putChar(key: String, value: Char): BundleBuilder {
        bundle.putChar(key, value)
        return this
    }

    fun putCharArray(key: String, value: CharArray): BundleBuilder {
        bundle.putCharArray(key, value)
        return this
    }

    fun putCharSequence(key: String, value: CharSequence): BundleBuilder {
        bundle.putCharSequence(key, value)
        return this
    }

    fun putCharSequenceArray(key: String, value: Array<CharSequence>): BundleBuilder {
        bundle.putCharSequenceArray(key, value)
        return this
    }

    fun putCharSequenceArrayList(key: String, value: ArrayList<CharSequence>): BundleBuilder {
        bundle.putCharSequenceArrayList(key, value)
        return this
    }

    fun putInt(key: String, value: Int): BundleBuilder {
        bundle.putInt(key, value)
        return this
    }

    fun putIntArray(key: String, value: IntArray): BundleBuilder {
        bundle.putIntArray(key, value)
        return this
    }

    fun putFloat(key: String, value: Float): BundleBuilder {
        bundle.putFloat(key, value)
        return this
    }

    fun putFloatArray(key: String, value: FloatArray): BundleBuilder {
        bundle.putFloatArray(key, value)
        return this
    }

    fun putIntegerArrayList(key: String, value: ArrayList<Int>): BundleBuilder {
        bundle.putIntegerArrayList(key, value)
        return this
    }

    fun putParcelable(key: String, value: Parcelable): BundleBuilder {
        bundle.putParcelable(key, value)
        return this
    }

    fun putParcelableArray(key: String, value: Array<Parcelable>): BundleBuilder {
        bundle.putParcelableArray(key, value)
        return this
    }

    fun putParcelableArrayList(key: String, value: ArrayList<out Parcelable>): BundleBuilder {
        bundle.putParcelableArrayList(key, value)
        return this
    }

    fun putSerializable(key: String, value: Serializable): BundleBuilder {
        bundle.putSerializable(key, value)
        return this
    }

    fun putShort(key: String, value: Short): BundleBuilder {
        bundle.putShort(key, value)
        return this
    }

    fun putShortArray(key: String, value: ShortArray): BundleBuilder {
        bundle.putShortArray(key, value)
        return this
    }

    fun putSparseParcelableArray(key: String, value: SparseArray<out Parcelable>): BundleBuilder {
        bundle.putSparseParcelableArray(key, value)
        return this
    }

    fun putStringArrayList(key: String, value: ArrayList<String>): BundleBuilder {
        bundle.putStringArrayList(key, value)
        return this
    }

    fun build(): Bundle {
        return bundle
    }

}
