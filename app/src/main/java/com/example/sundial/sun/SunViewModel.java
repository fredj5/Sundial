package com.example.sundial.sun;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SunViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SunViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is sun fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
