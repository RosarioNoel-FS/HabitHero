package com.example.habithero;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements HabitPreferenceDialogFragment.HabitAddListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        setupBottomNavigation();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_actionbar_layout, null);

            ImageView customActionBarImage = view.findViewById(R.id.custom_actionbar_image);
            customActionBarImage.setOnClickListener(v -> {
                Intent homeIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(homeIntent);
            });

            actionBar.setCustomView(view);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.nav_rewards:
                        fragment = new RewardsFragment();
                        break;
                    case R.id.nav_settings:
                        fragment = new SettingsFragment();
                        break;
                }
                loadFragment(fragment);
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    //app icon and sign out handler
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, LandingActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.sign_out:
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
                .setPositiveButton("Yes", (dialog, whichButton) -> {
                    Intent landingIntent = new Intent(MainActivity.this, LandingActivity.class);
                    startActivity(landingIntent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void navigateToHomeFragment() {
        Log.d("MainActivity", "Navigating to HomeFragment");

        loadFragment(new HomeFragment());
    }

    @Override
    public void onHabitAdded(Habit habit) {
        Log.d("MainActivity", "onHabitAdded triggered with Habit ID: " + habit.getId());

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).addNewHabit(habit);
        } else {
            navigateToHomeFragmentWithHabit(habit);
        }
    }

    public void navigateToHomeFragmentWithHabit(Habit habit) {
        HomeFragment homeFragment = new HomeFragment();
        // Pass the habit to HomeFragment
        Bundle args = new Bundle();
        args.putSerializable("newHabit", habit);
        homeFragment.setArguments(args);

        loadFragment(homeFragment);
    }

}
