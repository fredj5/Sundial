package com.example.sundial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registration extends AppCompatActivity {
    EditText registerFullName, registerEmail, registerPassword, registerConfirmPassword;
    Button registerUserButton, registerBackButton;
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registerFullName = findViewById(R.id.registerFullName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerConfirmPassword);
        registerUserButton = findViewById(R.id.registerButton);
        registerBackButton = findViewById(R.id.backToLogin);

        firebaseAuth = FirebaseAuth.getInstance();

        registerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();

            }
        });

        registerUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Extract the data from the registration page

                String fullName = registerFullName.getText().toString();
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confirmPassword = registerConfirmPassword.getText().toString();

                if(fullName.isEmpty()) {
                    registerFullName.setError("Please enter your full name");
                    return;
                }

                if(email.isEmpty()) {
                    registerEmail.setError("Please enter your email");
                    return;
                }

                if(password.isEmpty()) {
                    registerPassword.setError("Please enter your password");
                    return;
                }

                if(confirmPassword.isEmpty()) {
                    registerConfirmPassword.setError("Please enter your password again");
                    return;
                }

                if(!password.equals(confirmPassword)) {
                    registerConfirmPassword.setError("Passwords do not match. Please re-enter your password");
                    return;
                }

                Toast.makeText(Registration.this, "Welcome to Sundial", Toast.LENGTH_LONG).show();

                // Register the user using firebase

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Send the user to the next page
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registration.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}