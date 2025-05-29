package com.example.waterconservationappblank;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * This fragment represents the Settings screen.
 * It includes a switch for connecting to Air Selangor and a link to the Terms & Conditions.
 */
public class SettingFragment extends Fragment {

    private TextView usernametv;
    private TextView emailtv;
    private TextView termsText;
    private Button changepasswordbtn;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment using the fragment_setting.xml file
        View view = inflater.inflate(R.layout.fragment_setting, container, false);


        // Find the TextView that acts as a clickable Terms & Conditions link
        termsText = view.findViewById(R.id.termsTextView);
        usernametv = view.findViewById(R.id.tv_username);
        emailtv = view.findViewById(R.id.tv_email);
        changepasswordbtn = view.findViewById(R.id.btnChangePassword);

        // Set an onClickListener to open a browser and load the Terms & Conditions URL
        termsText.setOnClickListener(v -> {
            // Create an Intent to open a web browser with the given URL
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/terms"));
            startActivity(browserIntent); // Start the activity to open the URL
        });

        changepasswordbtn.setOnClickListener(v -> showChangePasswordDialog());

        //set user's name and email based on that in firebase
        if (user != null) {
            String email = user.getEmail();
            String username = user.getDisplayName(); // Only works if displayName was set

            // Set them to your EditTexts, TextViews, etc.
            emailtv.setText(email);
            usernametv.setText(username); // May be null if not set during signup
        }

        // Return the completed view for the fragment
        return view;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Password");

        // Create input field
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter new password");

        builder.setView(input);

        // Handle button clicks
        builder.setPositiveButton("Change", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();

            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(newPassword, user);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void changePassword(String newPassword, FirebaseUser user) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

}
