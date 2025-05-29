package com.example.waterconservationappblank;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.Credential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

public class Login extends AppCompatActivity {

    // UI elements for email, password input and buttons
    EditText emailField, passwordField;
    Button loginBtn, signupBtn;
    ImageButton googleCtnBtn;

    // firebase authentication instance
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable edge to edge display for immersive UI experience
        EdgeToEdge.enable(this);

        // set the layout XML file for this activity
        setContentView(R.layout.activity_login);

        // initialize firebaseauth instance
        mAuth = FirebaseAuth.getInstance();

        // connect UI components from layout to java variables
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginButton);
        signupBtn = findViewById(R.id.signupButton);
        googleCtnBtn = findViewById(R.id.btnCtnGoogle);

        // set click listener for signup button to launch registeractivity
        loginBtn.setOnClickListener(v -> attemptLogin());
        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, RegisterActivity.class);
            startActivity(intent);
        });

        // set click listener for google login button to start google sign-in flow
        googleCtnBtn.setOnClickListener(v -> beginGoogleLogin());

    }

    // attempt login using email and password entered
    private void attemptLogin() {

        // retrieve input and trim whitespace
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // basic validation to check if fields are not empty
        if (!email.isEmpty() && !password.isEmpty()) {

            // use firebaseauth to sign in with email and password
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()) {

                    // login success, get current user
                    FirebaseUser user = mAuth.getCurrentUser();

                    // inform user of success
                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    // Go to main activity which will be dashboard
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);

                    // Optional: clear the back stack so user can't go back to login
                    finish();
                } else {

                    // login failed, show error message
                    Toast.makeText(Login.this, "Authentication failed: " +
                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {

            // one or both fields are empty, prompt user to fill them
            Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }

    }

    // start google sign in process using credential manager API
    private void beginGoogleLogin() {

        // create credential manager instance for this context
        CredentialManager credentialManager = CredentialManager.create(this);

        // ensure this matches your OAuth 2.0 Web client ID from Firebase
        String serverClientId = getString(R.string.client_id);

        // configure google ID option for requesting credentials
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // allow all google accounts
                .setServerClientId(serverClientId) // set client ID for server verification
                .build();

        // build the credential request including google ID option
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // create cancellation signal for request lifecycle management
        CancellationSignal cancellationSignal = new CancellationSignal();

        // request the credential for google sign in
        credentialManager.getCredentialAsync(
                this,
                request,
                cancellationSignal,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {

                        // handle successful credential retrieval
                        Credential credential = result.getCredential();
                        Log.d("LoginDebug", "Received credential class: " + credential.getClass().getName());

                        // check if credential is a Custom Credential(google id token)
                        if (credential instanceof CustomCredential) {
                            CustomCredential customCredential = (CustomCredential) credential;

                            // Extract the raw JSON data from the CustomCredential
                            Bundle dataBundle = customCredential.getData();
                            if (dataBundle != null) {
                                // Log all keys and their values in the dataBundle
                                for (String key : dataBundle.keySet()) {
                                    Log.d("LoginDebug", "Key: " + key + " | Value: " + dataBundle.get(key));
                                }

                                // Extract the ID token using the correct key
                                String idToken = dataBundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN");
                                if (idToken != null) {
                                    Log.d("LoginDebug", "Extracted ID Token: " + idToken);
                                    firebaseAuthWithGoogle(idToken);
                                } else {
                                    Log.e("LoginDebug", "ID token not found in CustomCredential data");
                                    Toast.makeText(Login.this, "ID token not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("LoginDebug", "CustomCredential data bundle is null");
                                Toast.makeText(Login.this, "Credential data is missing", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("LoginDebug", "Unexpected credential type: " + credential.getClass().getName());
                            Toast.makeText(Login.this, "Unexpected credential type", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {

                        // handle error during google sign in process
                        Log.e("LoginDebug", "Google Sign-In failed", e);
                        Toast.makeText(Login.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // authenticate with firebase using the google ID token
    private void firebaseAuthWithGoogle(String idToken) {
        FirebaseAuth.getInstance()
                .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        // sign in success, get current user
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Toast.makeText(Login.this, "Signed in successfully", Toast.LENGTH_SHORT).show();

                        // navigate to main activity, dashboard
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);

                        // finish login activity to prevent back navigation
                        finish();
                    } else {

                        //sign in failure notification
                        Toast.makeText(Login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}





