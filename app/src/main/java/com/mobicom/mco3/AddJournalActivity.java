package com.mobicom.mco3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddJournalActivity extends AppCompatActivity {

    private EditText etTitle, etReflection;
    private Spinner spinnerMood;
    private MaterialButton btnSave, btnAddImage,btnBack;
    private FlexboxLayout imagePreviewContainer;
    private FirebaseHelper firebaseHelper;
    private final List<Uri> pickedImageUris = new ArrayList<>();
    private ActivityResultLauncher<String[]> pickImagesLauncher;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_journal);

        firebaseHelper = new FirebaseHelper();

        etTitle = findViewById(R.id.inputTitle);
        etReflection = findViewById(R.id.inputReflection);
        spinnerMood = findViewById(R.id.spinnerMood);
        btnSave = findViewById(R.id.btnSaveEntry);
        btnAddImage = findViewById(R.id.btnAddImage);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        btnBack = findViewById(R.id.btnBack);

        etReflection.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        // Setup mood options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mood_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> uris) {
                        if (uris == null || uris.isEmpty()) return;

                        for (Uri uri : uris) {
                            if (pickedImageUris.size() >= 6) break;
                            if (!pickedImageUris.contains(uri)) {
                                pickedImageUris.add(uri);
                                getContentResolver().takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            }
                        }

                        if (!pickedImageUris.isEmpty()) {
                            imagePreviewContainer.setVisibility(View.VISIBLE);
                            refreshImagePreviews();
                        }
                    }
                });

        btnAddImage.setOnClickListener(v ->
                pickImagesLauncher.launch(new String[]{"image/*"})
        );
        btnBack.setOnClickListener(v -> {
            finish(); // Just closes this activity and goes back to MainActivity
        });

        btnSave.setOnClickListener(v -> saveJournalEntry());
    }

    private void refreshImagePreviews() {
        imagePreviewContainer.removeAllViews();
        for (Uri uri : pickedImageUris) {
            androidx.appcompat.widget.AppCompatImageView iv = new androidx.appcompat.widget.AppCompatImageView(this);
            int sizeInPx = (int) (getResources().getDisplayMetrics().density * 100);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(sizeInPx, sizeInPx);
            lp.setMargins(8, 8, 8, 8);
            iv.setLayoutParams(lp);
            iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(uri).into(iv);
            imagePreviewContainer.addView(iv);
        }
    }

    private void saveJournalEntry() {
        String title = etTitle.getText().toString().trim();
        String reflection = etReflection.getText().toString().trim();
        String mood = spinnerMood.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(reflection)) {
            etReflection.setError("Write something");
            etReflection.requestFocus();
            return;
        }
        btnSave.setEnabled(false);

        List<String> base64Images = new ArrayList<>();
        for (Uri uri : pickedImageUris) {
            String base64 = convertImageToBase64(uri);
            if (base64 != null) base64Images.add(base64);
        }

        JournalEntry entry = new JournalEntry(title, reflection, mood, Timestamp.now());
        entry.setImageBase64List(base64Images);

        firebaseHelper.saveEntry(entry,
                unused -> {
                    Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show();
                    resetForm();
                    finish();
                },
                e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                });
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void resetForm() {
        etTitle.setText("");
        etReflection.setText("");
        spinnerMood.setSelection(0);
        pickedImageUris.clear();
        imagePreviewContainer.removeAllViews();
        imagePreviewContainer.setVisibility(View.GONE);
        btnSave.setEnabled(true);
    }
}
