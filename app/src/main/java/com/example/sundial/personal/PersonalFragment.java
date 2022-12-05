package com.example.sundial.personal;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.FirebaseApp.getInstance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sundial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class PersonalFragment extends Fragment {

    // Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    // Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Imgs/";

    // XML views
    ImageView profileAvatar, coverDisplay;
    TextView nameDisplay, emailDisplay;
    FloatingActionButton edit_profile_button;
    ImageButton calculateButton;
    EditText enterWeight;
    EditText enterSunPercent;
    TextView dailyDoseRec;
    TextView dailySunIntake;
    Spinner skinTones;
    ArrayAdapter<CharSequence> adapter;

    // dialogue
    ProgressDialog progressDialog;

    // Permissions
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST = 400;

    // String Arrays
    String[] cameraPermissions;
    String[] storagePermissions;

    // URI of selected image
    Uri image_uri;

    // Checking profile or banner/cover image
    String profileOrCoverPhoto;

    //spinner flag
    private boolean spinnerInitialized;

    public PersonalFragment() {

    } // Constructor


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_personal, container, false);


        // Initialize firebase data
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");
        storageReference = storage.getReference();

        // Initialize permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Initialize views
        profileAvatar = view.findViewById(R.id.profileAvatar);
        coverDisplay = view.findViewById(R.id.coverDisplay);
        nameDisplay = view.findViewById(R.id.nameDisplay);
        emailDisplay = view.findViewById(R.id.emailDisplay);
        edit_profile_button = view.findViewById(R.id.edit_profile_button);
        dailyDoseRec = view.findViewById(R.id.dailyDoseRec);
        skinTones = view.findViewById(R.id.skin_selection);
        dailySunIntake = view.findViewById(R.id.dailySunIntake);
        enterSunPercent = view.findViewById(R.id.enterSunPercent);

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.skin_tones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        skinTones.setAdapter(adapter);

        // Initialize progress dialogue
        progressDialog = new ProgressDialog(getActivity());

        // Calculate button listener
        calculateButton = (ImageButton) view.findViewById(R.id.doubleArrowBtn);
        enterWeight = (EditText) view.findViewById(R.id.enterWeight);
        dailyDoseRec = (TextView) view.findViewById(R.id.dailyDoseRec);

        //set flag
        spinnerInitialized = false;

        // Button calculation
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calculation
                String weight_value_string = enterWeight.getText().toString();
                String percent_value_string = enterSunPercent.getText().toString();
                // Empty case
                if (TextUtils.isEmpty(weight_value_string)) {
                    enterWeight.setError("Please enter your weight");
                    return;
                }

                int weight_value_int = Integer.parseInt(weight_value_string);
                Integer dailyDose = weight_value_int * 27;
                int percent_value_int = Integer.parseInt(percent_value_string);
                int dailyAbsorbed = (int) (dailyDose * (percent_value_int * 2 * 0.01));



                dailyDoseRec.setText(dailyDose + " IU");
                enterWeight.getText().clear();
                dailySunIntake.setText(dailyAbsorbed + " IU");
                enterSunPercent.getText().clear();

                // Empty edit text check
                HashMap<String, Object> result = new HashMap<>();
                result.put("DailyDose", dailyDose);
                result.put("DailySunIntake", dailyAbsorbed);

                reference.child(user.getUid()).updateChildren(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Dismiss progress with successful update
                                Toast.makeText(getActivity(), "Updated!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Dismiss progress and flash error
                                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        });
            }});

        // Retrieve user info from firebase
        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check all data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("name").getValue();
                    String email = "" + dataSnapshot.child("email").getValue();
                    String image = "" + dataSnapshot.child("image").getValue();
                    String banner = "" + dataSnapshot.child("banner").getValue();
                    String weight_string = "" + dataSnapshot.child("DailyDose").getValue();
                    String percent_string = "" + dataSnapshot.child("DailySunIntake").getValue();

                    // Set variables
                    nameDisplay.setText(name);
                    emailDisplay.setText(email);
                    dailyDoseRec.setText(weight_string);
                    dailySunIntake.setText(percent_string + " IU");

                    try {
                        Picasso.get().load(image).into(profileAvatar);
                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_camera).into(profileAvatar);
                    }

                    try {
                        Picasso.get().load(banner).into(coverDisplay);
                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_action_sun).into(coverDisplay);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //skin tone listener
        skinTones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }
                // Empty edit text check
                HashMap<String, Object> result = new HashMap<>();

                result.put("SkinTone", adapterView.getItemAtPosition(i).toString());

                reference.child(user.getUid()).updateChildren(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Dismiss progress with successful update
                                Toast.makeText(getActivity(), "Updated!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Dismiss progress and flash error
                                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        });
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // Edit profile button click
        edit_profile_button.setOnClickListener(view1 -> showEditProfilePage());

        return view;
    }

    private boolean checkStoragePermission() {
        // A check to see if the storage permission is enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_REQUEST);
    }

    private boolean checkCameraPermission() {
        // A check to see if the storage permission is enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestCameraPermission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST);
    }


    private void showEditProfilePage() {
        // Customizable profile aspects
        String[] edit_options = {"Change Profile Picture", "Change Banner Picture", "Update Name"};
        // Alert dialogue code
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an Action");
        builder.setItems(edit_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Click handling
                if (i == 0) {
                    // Edit profile picture
                    progressDialog.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image"; // changing profile picture
                    showPictureDialogue();
                }
                else if (i == 1) {
                    // Edit Banner
                    progressDialog.setMessage("Updating Banner Image");
                    profileOrCoverPhoto = "cover"; // changing banner/cover picture
                    showPictureDialogue();

                }
                else if (i == 2) {
                    // Edit name
                    progressDialog.setMessage("Updating Name");
                    showNameDialogue("name");

                }
            }
        });
        builder.create().show();
    }

    private void showNameDialogue(String name_key) {
        // Dialogue, the key "name" updates firebase instance
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + name_key);

        // Layout
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        // Edit Text fields
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + name_key);
        linearLayout.addView(editText);

        // Set view and add buttons
        builder.setView(linearLayout);

        // Button to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Retrieve text from input
                String value = editText.getText().toString().trim();
                // Empty edit text check
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(name_key, value);

                    reference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Dismiss progress with successful update
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated!", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Dismiss progress and flash error
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();


                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Enter " + name_key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Button to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        // Creation and dialogue
        builder.create().show();

    }

    private void showPictureDialogue() {

        String[] edit_options = {"Camera", "Gallery"};
        // Alert dialogue code
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Image From");
        builder.setItems(edit_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Click handling
                if (which == 0) {
                    // Camera click
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1) {
                    // Gallery click
                    if(!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Called with Allow/Deny permission, handles permission cases

        switch (requestCode) {
            case CAMERA_REQUEST: {
                // Check camera permissions
                if (grantResults.length > 0) {
                    boolean cameraAllowed = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAllowed = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAllowed && writeStorageAllowed) {
                        // Enabled, choose from camera
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "Enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                // Check storage permission and choose from gallery
                if (grantResults.length > 0) {
                    boolean writeStorageAllowed = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAllowed) {
                        // Enabled, choose from gallery
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Called after an image is selected
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST) {
                image_uri = data.getData();

                uploadProfileImage(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST) {
                uploadProfileImage(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileImage(Uri uri) {
        // Progress
        progressDialog.show();
        // The path and name of the image to be stored in firebase
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2 = storageReference.child(filePathAndName);
        storageReference2.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        // Check if image is uploaded
                        if (uriTask.isSuccessful()) {

                            HashMap<String, Object> results = new HashMap<>();
                            /* First parameters indicates the tag indicating if it's the
                            cover image or profile image*/
                            results.put(profileOrCoverPhoto, downloadUri.toString());
                            reference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // URL is in database of user, added successfully
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated!", Toast.LENGTH_SHORT).show();


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Same thing as onSuccess except flash an error
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Error while updating image", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                        }
                        else {
                            // error
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Error retrieving image", Toast.LENGTH_SHORT).show();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickFromGallery() {
        // Intent: Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST);
    }

    private void pickFromCamera() {
        // Intent: Pick images from device
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        // image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent: start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST);
    }



}




