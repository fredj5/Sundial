package com.example.sundial.sun;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

public class SunViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    Button locationBtn;

    public SunViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is sun fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
