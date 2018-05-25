package com.bluelinelabs.conductor.demo.controllers.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Date;

public class ViewModelDemo extends ViewModel {
    private MutableLiveData<String> liveData = new MutableLiveData<>();

    public ViewModelDemo() {
        liveData.setValue(new Date().toString());
    }

    public LiveData<String> getLiveData() {
        return liveData;
    }
}
