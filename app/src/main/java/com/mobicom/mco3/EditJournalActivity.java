package com.mobicom.mco3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditJournalActivity extends AppCompatActivity {

    private EditText editTitle, editReflection;
    private Spinner editMood;
    private Button btnSave;
    private GridLayout imagePreviewGrid;
    private FirebaseFirestore db;
    private JournalEntry entry;
    private final List<String> imageBase64List = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null && imageBase64List.size() < 6) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                            String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                            imageBase64List.add(encoded);
                            refreshImageGrid();
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        editTitle = findViewById(R.id.editTitle);
        editReflection = findViewById(R.id.editReflection);
        editMood = findViewById(R.id.editMood);
        btnSave = findViewById(R.id.btnSave);
        imagePreviewGrid = findViewById(R.id.imagePreviewGrid);

        editReflection.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mood_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editMood.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        String entryId = getIntent().getStringExtra("entryId");

        if (entryId == null || entryId.isEmpty()) {
            Toast.makeText(this, "No entry ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("entries")
                .document(entryId)
                .get()
                .addOnSuccessListener(doc -> {
                    entry = doc.toObject(JournalEntry.class);
                    if (entry != null) {
                        entry.setId(doc.getId());
                        editTitle.setText(entry.getTitle());
                        editReflection.setText(entry.getReflection());
                        int spinnerPosition = adapter.getPosition(entry.getMood());
                        editMood.setSelection(spinnerPosition);
                        if (entry.getImageBase64List() != null) {
                            imageBase64List.clear();
                            imageBase64List.addAll(entry.getImageBase64List());
                            refreshImageGrid();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load entry", Toast.LENGTH_SHORT).show();
                    finish();
                });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String reflection = editReflection.getText().toString().trim();
            String mood = editMood.getSelectedItem().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(reflection)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (entry == null) {
                Toast.makeText(this, "Entry not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            entry.setTitle(title);
            entry.setReflection(reflection);
            entry.setMood(mood);
            entry.setTimestamp(Timestamp.now());
            entry.setImageBase64List(new ArrayList<>(imageBase64List));

            db.collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("entries")
                    .document(entry.getId())
                    .set(entry)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void refreshImageGrid() {
        imagePreviewGrid.removeAllViews();
        imagePreviewGrid.setVisibility(View.VISIBLE);

        int imageSize = (int) (getResources().getDisplayMetrics().density * 100); // 100dp

        for (int i = 0; i < imageBase64List.size(); i++) {
            final int index = i;

            // Decode base64 to bitmap
            String base64 = imageBase64List.get(i);
            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            // FrameLayout container
            FrameLayout frame = new FrameLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            params.setMargins(8, 8, 8, 8);
            frame.setLayoutParams(params);

            // ImageView
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bitmap);
            frame.addView(imageView);

            // Remove ("X") Button
            ImageButton closeButton = new ImageButton(this);
            FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
                    (int) (32 * getResources().getDisplayMetrics().density),
                    (int) (32 * getResources().getDisplayMetrics().density)
            );
            closeParams.topMargin = 4;
            closeParams.rightMargin = 4;
            closeParams.gravity = android.view.Gravity.END | android.view.Gravity.TOP;

            closeButton.setLayoutParams(closeParams);
            closeButton.setImageResource(android.R.drawable.ic_delete);
            closeButton.setColorFilter(getResources().getColor(android.R.color.black));
            closeButton.setBackgroundResource(android.R.color.transparent);
            closeButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            closeButton.setOnClickListener(v -> {
                imageBase64List.remove(index);
                refreshImageGrid();
            });

            frame.addView(closeButton);

            imagePreviewGrid.addView(frame);
        }

        // Add Image "+" button if fewer than 6 images
        if (imageBase64List.size() < 6) {
            FrameLayout addFrame = new FrameLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            params.setMargins(8, 8, 8, 8);
            addFrame.setLayoutParams(params);

            ImageView addButton = new ImageView(this);
            addButton.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            addButton.setImageResource(android.R.drawable.ic_input_add);
            addButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            addButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            addButton.setOnClickListener(v -> openImagePicker());

            addFrame.addView(addButton);
            imagePreviewGrid.addView(addFrame);
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }
}
