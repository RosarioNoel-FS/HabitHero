package com.example.habithero;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsFragment extends Fragment {
    public interface ProfileImageUpdateListener {
        void onProfileImageUpdated();
    }
    private ProfileImageUpdateListener updateListener;
    private ImageView profilePreviewImage;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;
    private static final int CAMERA_PERMISSION_CODE = 100;


    // ActivityResultLaunchers
    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    profilePreviewImage.setImageBitmap(bitmap);
                    saveProfileImageToFirestore(bitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        profilePreviewImage.setImageBitmap(bitmap);
                        saveProfileImageToFirestore(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        Button signOutButton = view.findViewById(R.id.sign_out_button);
        Button editProfileImageButton = view.findViewById(R.id.edit_profile_img_button);
        profilePreviewImage = view.findViewById(R.id.profile_preview_image);

        signOutButton.setOnClickListener(v -> showSignOutDialog());
        editProfileImageButton.setOnClickListener(v -> showImageSelectionDialog());

        loadProfileImage();

        return view;
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, whichButton) -> signOut())
                .setNegativeButton("No", null)
                .show();
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LandingActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Profile Picture");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery", "Cancel"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            takePicture();
                            break;
                        case 1:
                            pickImageLauncher.launch("image/*");
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                });
        builder.show();
    }

    private void takePicture() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            takePictureLauncher.launch(null);
        }
    }

    private void saveProfileImageToFirestore(Bitmap imageBitmap) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");

        profileImageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update the profile image URL in Firestore
                        firebaseHelper.updateProfileImageUrl(userId, uri, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onCallback(Void result) {
                                Log.d("SettingsFragment", "Profile image URL updated in Firestore.");
                                if (updateListener != null) {
                                    updateListener.onProfileImageUpdated();
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("SettingsFragment", "Error updating profile image URL: " + e.getMessage());
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle upload error
                    Log.e("SettingsFragment", "Error uploading profile image: " + e.getMessage());
                });
    }

    public void setUpdateListener(ProfileImageUpdateListener listener) {
        this.updateListener = listener;
    }

    private void loadProfileImage() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseHelper.loadProfileImage(userId, new FirebaseHelper.FirestoreCallback<Uri>() {
            @Override
            public void onCallback(Uri uri) {
                Glide.with(getContext())
                        .load(uri)
                        .placeholder(R.drawable.profile_light_boy)  // Set placeholder here
                        .circleCrop()
                        .into(profilePreviewImage);
            }

            @Override
            public void onError(Exception e) {
                // If there is an error, set a default image
                profilePreviewImage.setImageResource(R.drawable.profile_light_boy);
            }
        });
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to use the camera")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(null);
            } else {
                Toast.makeText(getContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure that the host activity implements the callback interface
        try {
            updateListener = (ProfileImageUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ProfileImageUpdateListener");
        }
    }


}
