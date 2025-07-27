package com.mobicom.mco3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> journalList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private ExtendedFloatingActionButton btnNewEntry;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();

        recyclerView = findViewById(R.id.journalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter(journalList);
        recyclerView.setAdapter(adapter);

        btnNewEntry = findViewById(R.id.btnNewEntry);
        btnNewEntry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddJournalActivity.class);
            startActivity(intent);
        });

        loadJournalEntries();
    }

    private void loadJournalEntries() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.getJournalEntries(
                entries -> {
                    journalList.clear();
                    journalList.addAll(entries);
                    adapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(MainActivity.this, "Error loading entries", Toast.LENGTH_SHORT).show()
        );
        Log.d("MainActivity", "Loaded entries: " + journalList.size());
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadJournalEntries();  // Refresh entries when coming back
    }

}
