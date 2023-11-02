package com.example.habithero;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);  // Disable the default home button
            actionBar.setDisplayShowTitleEnabled(false); // Optional: hide title

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_actionbar_layout, null);

            // Optionally, add a click listener to the custom image view
            ImageView customActionBarImage = view.findViewById(R.id.custom_actionbar_image);
            customActionBarImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle your back action here
                    Intent homeIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                }
            });

            actionBar.setCustomView(view);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // NavigationBarView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener)
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        Toast.makeText(MainActivity.this, "Home selected", Toast.LENGTH_SHORT).show();
                        // TODO: Implement your logic for the Home section
                        break;
                    case R.id.nav_trophy:
                        Toast.makeText(MainActivity.this, "Trophy selected", Toast.LENGTH_SHORT).show();
                        // TODO: Implement your logic for the Trophy section
                        break;
                    case R.id.nav_settings:
                        Toast.makeText(MainActivity.this, "Settings selected", Toast.LENGTH_SHORT).show();
                        // TODO: Implement your logic for the Settings section
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle your back action here
                Intent homeIntent = new Intent(this, LandingActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.sign_out:
                // Show the sign-out dialog
                showSignOutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Handle your sign-out logic here, e.g., clearing shared preferences or session
                        Intent landingIntent = new Intent(MainActivity.this, LandingActivity.class);
                        startActivity(landingIntent);
                        finish(); // This closes the current activity after sign out
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
