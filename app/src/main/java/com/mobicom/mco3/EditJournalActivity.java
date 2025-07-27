package com.mobicom.mco3;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditJournalActivity extends AppCompatActivity {

    private EditText editTitle, editReflection, editMood;
    private Button btnSave;
    private FirebaseFirestore db;
    private JournalEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);

        // Find views from XML
        editTitle = findViewById(R.id.editTitle);
        editReflection = findViewById(R.id.editReflection);
        editMood = findViewById(R.id.editMood);
        btnSave = findViewById(R.id.btnSave);  // ✅ defined in XML layout

        db = FirebaseFirestore.getInstance();
        String entryId = getIntent().getStringExtra("entryId");

        if (entryId == null || entryId.isEmpty()) {
            Toast.makeText(this, "No entry ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch entry from Firestore
        db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("entries")
                .document(entryId)
                .get()
                .addOnSuccessListener(doc -> {
                    entry = doc.toObject(JournalEntry.class);
                    if (entry != null) {
                        entry.setId(doc.getId());

                        // Populate fields
                        editTitle.setText(entry.getTitle());
                        editReflection.setText(entry.getReflection());
                        editMood.setText(entry.getMood());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load entry", Toast.LENGTH_SHORT).show();
                    finish();
                });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String reflection = editReflection.getText().toString().trim();
            String mood = editMood.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(reflection)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (entry == null) {
                Toast.makeText(this, "Entry not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update and save
            entry.setTitle(title);
            entry.setReflection(reflection);
            entry.setMood(mood);
            entry.setTimestamp(Timestamp.now());

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
}
