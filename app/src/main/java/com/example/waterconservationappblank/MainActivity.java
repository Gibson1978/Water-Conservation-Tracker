package com.example.waterconservationappblank;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.waterconservationappblank.AddFragmentPackage.AddFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    TextView TopBarDate; // Top bar TextView to display current date
    BottomNavigationView bottomNavigationView; // Bottom navigation bar

    // Fragment objects
    DashboardFragment dashboardFragment = new DashboardFragment();
    LogFragment logFragment = new LogFragment();
    AddFragment addFragment = new AddFragment();
    InfoFragment infoFragment = new InfoFragment();
    SettingFragment settingFragment = new SettingFragment();
// Login screen

    // Get current date in a formatted string like "Monday, April 15, 2025"
    String currentDate = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables drawing edge-to-edge in Android (optional UI polish)
        setContentView(R.layout.activity_main); // Set the layout for this activity

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }

        // Connect UI elements with layout views
        TopBarDate = findViewById(R.id.tv_date); // Finds the TextView showing the date
        bottomNavigationView = findViewById(R.id.bottom_navigation); // Finds the bottom navigation view
        getSupportFragmentManager().beginTransaction().replace(R.id.container, dashboardFragment).commit();//to show dashboard as default

        // Set the current date in the top bar
        TopBarDate.setText(currentDate);

        // Handle bottom navigation bar item selection
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dashboard:
                        // Navigate to DashboardFragment
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, dashboardFragment).commit();
                        return true;
                    case R.id.log:
                        // Navigate to LogFragment
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, logFragment).commit();
                        return true;
                    case R.id.add:
                        // Navigate to AddFragment (used to log water usage maybe)
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, addFragment).commit();
                        return true;
                    case R.id.info:
                        // Navigate to InfoFragment (e.g., conservation tips)
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, infoFragment).commit();
                        return true;
                    case R.id.setting:
                        // Navigate to SettingFragment (for app preferences)
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, settingFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * This method is called from the LoginFragment once the user has logged in successfully.
     * It loads the dashboard fragment and shows the top bar and bottom navigation.
     */
    public void loadDashboard() {

        // make the top bar date TextView visible
        TopBarDate.setVisibility(View.VISIBLE);

        // mak the bottom navigation bar visible
        bottomNavigationView.setVisibility(View.VISIBLE);

        // replace the container with the dashboard fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, dashboardFragment)
                .commit();
    }
}
