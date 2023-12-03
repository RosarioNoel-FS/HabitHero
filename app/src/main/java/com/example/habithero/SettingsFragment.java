package com.example.habithero;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.habithero.User;

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
import java.io.InputStream;

public class SettingsFragment extends Fragment {
    public interface ProfileImageUpdateListener {
        void onProfileImageUpdated();
    }
    private ProfileImageUpdateListener updateListener;
    private ImageView profilePreviewImage;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Button fetchCategoryButton;
    private TextView greetingTextView;



    // ActivityResultLaunchers
    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    profilePreviewImage.setImageBitmap(bitmap);
                    Glide.with(getContext())
                            .load(bitmap)
                            .circleCrop() // Apply circleCrop() here too
                            .into(profilePreviewImage);
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
                        bitmap = rotateImageIfRequired(bitmap, uri);
                        Glide.with(getContext())
                                .load(bitmap)
                                .circleCrop() // Apply circleCrop() here too
                                .into(profilePreviewImage);
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
        EditText usernameInput = view.findViewById(R.id.username_input);
        Button updateUsernameButton = view.findViewById(R.id.update_username_button);
        greetingTextView = view.findViewById(R.id.greeting_text_view);
        loadUserGreeting();

//        fetchCategoryButton = view.findViewById(R.id.fetchcategory_btn);

        signOutButton.setOnClickListener(v -> showSignOutDialog());
        editProfileImageButton.setOnClickListener(v -> showImageSelectionDialog());

        updateUsernameButton.setOnClickListener(v -> {
            String newUsername = usernameInput.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
            } else {
                // Handle empty input, e.g., show a Toast
                Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });

//        fetchCategoryButton.setOnClickListener(view1 -> firebaseHelper.loadAllCategoryData());

        loadProfileImage();

        return view;
    }

    private void loadUserGreeting() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper.loadUserData(userId, new FirebaseHelper.FirestoreCallback<User>() {
            @Override
            public void onCallback(User user) {
                // Assuming 'User' is your model class that includes the username
                greetingTextView.setText("Hello, " + user.getUsername());
            }

            @Override
            public void onError(Exception e) {
                Log.e("SettingsFragment", "Error loading user data: " + e.getMessage(), e);
            }
        });
    }

    private void updateUsername(String newUsername) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper.updateUsername(userId, newUsername, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onCallback(Void result) {
                // Handle successful update, e.g., show a Toast
                Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();

                // Update the greeting text view with the new username
                greetingTextView.setText("Hello, " + newUsername);
            }

            @Override
            public void onError(Exception e) {
                // Handle error, e.g., show a Toast
                Toast.makeText(getContext(), "Error updating username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                Glide.with(getContext())
                        .load(R.drawable.profile_light_boy)
                        .circleCrop() // Apply circleCrop() here as well
                        .into(profilePreviewImage);            }
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

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = getActivity().getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (android.os.Build.VERSION.SDK_INT > 23) {
            ei = new ExifInterface(input);
        } else {
            ei = new ExifInterface(selectedImage.getPath());
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


}
