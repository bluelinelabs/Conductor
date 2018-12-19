package com.bluelinelabs.conductor.internal;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.util.SparseArray;

public class StringSparseArrayParceler implements Parcelable {

    private final SparseArray<String> stringSparseArray;

    public StringSparseArrayParceler(@NonNull SparseArray<String> stringSparseArray) {
        this.stringSparseArray = stringSparseArray;
    }

    StringSparseArrayParceler(@NonNull Parcel in) {
        stringSparseArray = new SparseArray<>();

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            stringSparseArray.put(in.readInt(), in.readString());
        }
    }

    @NonNull
    public SparseArray<String> getStringSparseArray() {
        return stringSparseArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        final int size = stringSparseArray.size();

        out.writeInt(size);

        for (int i = 0; i < size; i++) {
            int key = stringSparseArray.keyAt(i);

            out.writeInt(key);
            out.writeString(stringSparseArray.get(key));
        }
    }

    public static final Parcelable.Creator<StringSparseArrayParceler> CREATOR = new Parcelable.Creator<StringSparseArrayParceler>() {
        @Override
        public StringSparseArrayParceler createFromParcel(Parcel in) {
            return new StringSparseArrayParceler(in);
        }

        @Override
        public StringSparseArrayParceler[] newArray(int size) {
            return new StringSparseArrayParceler[size];
        }
    };

}
