package com.example.sundial;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sundial.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TextView verifyMessage;
    Button verifyEmailBtn;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        Button logoutBtn = findViewById(R.id.logoutBtn);
        verifyMessage = findViewById(R.id.verifyEmailMesg);
        verifyEmailBtn = findViewById(R.id.verifyEmailBtn);

        if(auth.getCurrentUser().isEmailVerified()) {
            verifyEmailBtn.setVisibility(View.GONE);
            verifyMessage.setVisibility(View.GONE);
        }

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send verification email
                auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Your verification email has been sent", Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this, "Check your spam folder if the email is not in your main inbox", Toast.LENGTH_LONG).show();
                        verifyEmailBtn.setVisibility(View.GONE);
                        verifyMessage.setVisibility(View.GONE);
                        startActivity(new Intent(getApplicationContext(), NavigationActivity.class));
                    }
                });
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

        // Get the Intent that started this activity and extract the string

    }
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {

    }
}