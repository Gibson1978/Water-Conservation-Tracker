package com.example.waterconservationappblank;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    // Ui elements for user input and navigation
    EditText nameField, emailField, passwordField, passwordConfField;
    FirebaseAuth mAuth; // firebase authentication instance
    TextView toLogin;
    Button registerBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // set layout

        // initialize firebaseauth instance
        mAuth = FirebaseAuth.getInstance();

        // link UI elements from XML layout to Java variables
        nameField = findViewById(R.id.et_username);
        emailField = findViewById(R.id.et_email);
        passwordField = findViewById(R.id.et_password);
        passwordConfField = findViewById(R.id.et_confirm_password);
        toLogin = findViewById(R.id.tv_toLogin);
        registerBut = findViewById(R.id.btn_register);

        // set click listener button to trigger registration attempts
        registerBut.setOnClickListener(v -> attemptRegister());

        // set click listener on "already have account? login" TextView to navigate to login screen
        toLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, Login.class);
            startActivity(intent);
        });
    }

    // attempts to register a new user with firebase authentication
    private void attemptRegister() {

        // get text input values and trim whitespace
        String username = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confPassword = passwordConfField.getText().toString().trim();

        // check is any field is empty, if yes show error toast and stop
        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || confPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // check if password and confirmation match
        if(!confPassword.equals(password)){
            Toast.makeText(RegisterActivity.this, "Password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // create user in firebase auth with email and password
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,task -> {
            if (task.isSuccessful()) {

                // user created properly
                FirebaseUser user = mAuth.getCurrentUser();

                // update display name to username inputted
                if (user != null){
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build());
                }
                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                // Go to main activity which will be dashboard
                Intent intent = new Intent(RegisterActivity.this, Login.class);
                startActivity(intent);

                // Optional: clear the back stack so user can't go back to login
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}