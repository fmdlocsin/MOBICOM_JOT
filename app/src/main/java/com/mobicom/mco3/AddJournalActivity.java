package com.mobicom.mco3;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddJournalActivity extends AppCompatActivity {

    private EditText etTitle, etReflection;
    private Spinner spinnerMood;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_journal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.inputTitle);
        etReflection = findViewById(R.id.inputReflection);
        spinnerMood = findViewById(R.id.spinnerMood);
        btnSave = findViewById(R.id.btnSaveEntry);

        // Load mood choices
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mood_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveJournalEntry());
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

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> entry = new HashMap<>();
        entry.put("title", title);
        entry.put("reflection", reflection);
        entry.put("mood", mood);
        entry.put("timestamp", Timestamp.now());

        db.collection("users")
                .document(uid)
                .collection("entries")
                .add(entry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etReflection.setText("");
                    spinnerMood.setSelection(0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
