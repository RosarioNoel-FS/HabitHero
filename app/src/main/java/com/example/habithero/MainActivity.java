package com.example.habithero;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity implements HabitPreferenceDialogFragment.HabitAddListener, SettingsFragment.ProfileImageUpdateListener, HabitNameScreen.HabitNameScreenListener {

    private Uri profileImageUri;
    private FirebaseHelper firebaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        firebaseHelper = new FirebaseHelper();
        setupActionBar();
        setupBottomNavigation();
        loadProfileImage();

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_actionbar_layout, null);

            ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.START | Gravity.CENTER_VERTICAL
            );

            actionBar.setCustomView(view, params);
        }
    }

    private void loadProfileImage() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper.loadProfileImage(userId, new FirebaseHelper.FirestoreCallback<Uri>() {
            @Override
            public void onCallback(Uri uri) {
                Log.d("MainActivity", "Loading profile image URI: " + uri);
                profileImageUri = uri;
                invalidateOptionsMenu(); // Invalidate the options menu so it will be recreated with the new image
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Error loading profile image: " + e.getMessage());
            }
        });
    }




    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem profileItem = menu.findItem(R.id.profile);
        ImageView profileImageView = (ImageView) profileItem.getActionView().findViewById(R.id.profile_image_view);

        if (profileImageUri != null) {
            Glide.with(this)
                    .load(profileImageUri)
                    .circleCrop()
                    .skipMemoryCache(true) // Skip memory cache
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk cache
                    .into(profileImageView);
        } else {
            // Set a default image if no profile image is available
            Glide.with(this)
                    .load(R.drawable.profile_light_boy) // Replace with your default image drawable
                    .circleCrop()
                    .into(profileImageView);
        }

        // Set click listener for the profile image view
        profileImageView.setOnClickListener(v -> navigateToSettingsFragment());

        return super.onPrepareOptionsMenu(menu);
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
            case R.id.profile:
                navigateToSettingsFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void navigateToSettingsFragment() {
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setUpdateListener(this); // 'this' refers to MainActivity

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit();
    }



    @Override
    public void onHabitAdded(Habit habit) {
        Log.d("MainActivity", "onHabitAdded triggered with Habit ID: " + habit.getId());
        navigateToHomeFragment(habit);
    }

    public void navigateToHomeFragment(Habit newHabit) {
        Log.d("DebugLog", "navigateToHomeFragment called with Habit: " + (newHabit != null ? newHabit.getName() : "null"));

        HomeFragment homeFragment = new HomeFragment();

        if (newHabit != null) {
            Bundle args = new Bundle();
            args.putSerializable("newHabit", newHabit);
            homeFragment.setArguments(args);
        }
        Log.d("DebugLog", "About to load HomeFragment");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }


    @Override
    public void onProfileImageUpdated() {
        Log.d("MainActivity", "Profile image update listener triggered, refreshing image.");
        loadProfileImage(); // Call this method to refresh the profile image
    }



    // CALL BACKS
    // In MainActivity
    @Override
    public void onHabitNameConfirmed(String habitName) {
        FrequencySelectionScreen frequencySelectionScreen = new FrequencySelectionScreen();
        Bundle args = new Bundle();
        args.putString("habitName", habitName);
        frequencySelectionScreen.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frequencySelectionScreen)
                .addToBackStack(null)
                .commit();
    }



}
