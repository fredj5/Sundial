package com.example.sundial.sun;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import com.example.sundial.MainActivity;
import com.example.sundial.R;
import com.example.sundial.databinding.FragmentSunBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SunFragment extends Fragment {

    private FragmentSunBinding binding;


    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    ImageView profileAvatar;

    private String latitude;
    private String longitude;
    private String uv;
    private String burnTime;
    private int skinToneIndex;
    private int updates;
    private String[] timeToBurn;


    private LocationRequest mLocationRequest;


    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sun, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        database = database.getInstance();
        reference = database.getReference("Users");

        binding = FragmentSunBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        uv = "";

        timeToBurn = new String[6];

        int updates = 0;


        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check all data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String skinTone = "" + dataSnapshot.child("SkinTone").getValue();
                    System.out.println("skin tone: " + skinTone);

                    skinToneIndex = Integer.parseInt(skinTone.substring(skinTone.length() - 1)) - 1;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        startLocationUpdates();

        return root;
    }

    protected void startLocationUpdates() {
        System.out.println("in start location updates");
        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        System.out.println("here");
        getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {

        // New location has now been determined
        if (updates == 0) {
            updates++;

            DecimalFormat df = new DecimalFormat("0.00");

            latitude = df.format(location.getLatitude()).toString();
            longitude = df.format(location.getLongitude()).toString();
            System.out.println(latitude);
            System.out.println(longitude);

            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        OkHttpClient client = new OkHttpClient();

                        Request request = new Request.Builder()
                                .url("https://api.openuv.io/api/v1/uv?lat=" + latitude + "&lng=" + longitude)
                                .get()
                                .addHeader("x-access-token", "5004de8b897772489690865a13c5cd5d")
                                .build();

                        try {
                            Response response = client.newCall(request).execute();

                            String jsonData = response.body().string();
                            JSONObject jObject = new JSONObject(jsonData);
                            JSONObject result = jObject.getJSONObject("result");

                            uv = result.get("uv").toString();

                            System.out.println("UV Index is: " + uv);

                            JSONObject exposureTime = result.getJSONObject("safe_exposure_time");

                            for (int i = 0; i < 6; i++) {
                                timeToBurn[i] = exposureTime.get("st" + (i + 1)).toString();
                                System.out.println("Time to burn for skin type " + (i + 1) + ": " + timeToBurn[i]);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    TextView uv_index = getView().findViewById(R.id.uv_index);
                                    TextView burnTimeText = getView().findViewById(R.id.burn_time);

                                    burnTime = timeToBurn[skinToneIndex];

                                    uv_index.setText(uv);
                                    if (uv != "0") {
                                        burnTimeText.setText(burnTime + " minutes");
                                    } else {
                                        burnTimeText.setText("No burn risk");
                                    }
                                }
                            });

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });

            thread.start();

            // You can now create a LatLng Object for use with maps
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
