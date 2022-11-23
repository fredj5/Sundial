package com.example.sundial.sun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.widget.Button;

import com.example.sundial.R;
import com.example.sundial.databinding.FragmentSunBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

public class SunFragment extends Fragment {

    public static final int REQUEST_LOCATION_PERMISSION = 12;
    private FragmentSunBinding binding;
    Button getLoc;
    Location mLastLocation;
    TextView mLocationTextView;
    FusedLocationProviderClient mFusedLocationClient;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SunViewModel sunViewModel =
                new ViewModelProvider(this).get(SunViewModel.class);

        getLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        binding = FragmentSunBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSun;
        sunViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess (Location location){
                    if (location != null) {
                        mLastLocation = location;
                        mLocationTextView.setText(
                                getString(R.string.location_text,
                                        mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude(),
                                        mLastLocation.getTime()));
                    } else {
                        mLocationTextView.setText(R.string.no_location);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    getLocation();
                } else {
                    Toast.makeText(getActivity(),
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
