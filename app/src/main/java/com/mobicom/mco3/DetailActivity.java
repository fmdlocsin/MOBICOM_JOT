package com.mobicom.mco3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobicom.mco3.databinding.ActivityDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;
public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private FirebaseFirestore db;
    private String entryId;
    private String uid;
    private JournalEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mainContent.setVisibility(View.GONE);
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        entryId = getIntent().getStringExtra("entryId");

        if (entryId == null || entryId.isEmpty()) {
            Toast.makeText(this, "No entry ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEntry();

        // Setup buttons
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditJournalActivity.class);
            intent.putExtra("entryId", entry.getId());
            startActivity(intent);
        });

        binding.btnDelete.setOnClickListener(v -> showDeleteConfirm());
        binding.btnBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void displayBase64Images(List<String> base64Images) {
        if (base64Images == null || base64Images.isEmpty()) return;

        androidx.gridlayout.widget.GridLayout gridLayout = binding.imageGrid;
        gridLayout.removeAllViews();
        gridLayout.setVisibility(View.VISIBLE);

        int columns = 3;
        int total = Math.min(base64Images.size(), 6);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        int spacingPx = (int) (4 * getResources().getDisplayMetrics().density); // 4dp spacing
        int totalSpacing = spacingPx * (columns + 1);
        int imageSize = (screenWidth - totalSpacing) / columns;

        gridLayout.setColumnCount(columns);

        for (int i = 0; i < total; i++) {
            String base64 = base64Images.get(i);
            try {
                byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = imageSize;
                params.height = imageSize;

                int row = i / columns;
                int col = i % columns;
                params.setMargins(
                        col == 0 ? spacingPx : spacingPx / 2,  // left
                        row == 0 ? spacingPx : spacingPx / 2,  // top
                        col == columns - 1 ? spacingPx : spacingPx / 2, // right
                        spacingPx / 2                          // bottom
                );

                imageView.setLayoutParams(params);
                imageView.setOnClickListener(v -> showImagePreviewDialog(bitmap));

                gridLayout.addView(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showImagePreviewDialog(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        builder.setView(imageView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showDeleteConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this journal entry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("users").document(uid)
                            .collection("entries").document(entry.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadEntry(); // reload data when returning from EditJournalActivity
    }

    private void loadEntry() {
        binding.mainContent.setVisibility(View.GONE);
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(uid)
                .collection("entries")
                .document(entryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        entry = documentSnapshot.toObject(JournalEntry.class);
                        if (entry != null) {
                            entry.setId(documentSnapshot.getId());

                            binding.detailTitle.setText(entry.getTitle());
                            binding.detailMood.setText("Mood: " + entry.getMood());
                            binding.detailReflection.setText(entry.getReflection());

                            Timestamp ts = entry.getTimestamp();
                            if (ts != null) {
                                String formatted = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                                        .format(ts.toDate());
                                binding.detailDate.setText(formatted);
                            } else {
                                binding.detailDate.setText("No date available");
                            }

                            displayBase64Images(entry.getImageBase64List());

                            binding.mainContent.setVisibility(View.VISIBLE);
                            binding.loadingIndicator.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load entry", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

}
