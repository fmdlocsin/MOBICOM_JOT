package com.mobicom.mco3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
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

        //back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
        });

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        entryId = getIntent().getStringExtra("entryId");

        if (entryId == null || entryId.isEmpty()) {
            Toast.makeText(this, "No entry ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

                            // Setup edit/delete buttons
                            binding.btnEdit.setOnClickListener(v -> {
                                Intent intent = new Intent(this, EditJournalActivity.class);
                                intent.putExtra("entryId", entry.getId());  // JournalEntry implements Serializable
                                startActivity(intent);
                            });
                            binding.btnDelete.setOnClickListener(v -> showDeleteConfirm());
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
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
}
