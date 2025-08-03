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

import android.net.Uri;
import com.mobicom.mco3.util.SpotifyMoodHelper;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> journalList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private ExtendedFloatingActionButton btnNewEntry;
    private FirebaseHelper firebaseHelper;

    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.mainProgressBar);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();

        recyclerView = findViewById(R.id.journalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter(this, journalList);
        recyclerView.setAdapter(adapter);

        btnNewEntry = findViewById(R.id.btnNewEntry);
        btnNewEntry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddJournalActivity.class);
            startActivity(intent);
            //This is a transition down here
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        loadJournalEntries();
    }

    /*private void loadJournalEntries() {
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
    }

     */

    private void loadJournalEntries() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.getJournalEntries(
                entries -> {
                    progressBar.setVisibility(View.GONE);
                    journalList.clear();
                    journalList.addAll(entries);
                    adapter.notifyDataSetChanged();

                    if (!journalList.isEmpty()) {
                        String latestMood = journalList.get(0).getMood();
                        String playlistUrl = SpotifyMoodHelper.INSTANCE.getRandomPlaylistUrl(latestMood);

                        if (playlistUrl != null) {
                            findViewById(R.id.spotifyCard).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.spotifyMoodTitle)).setText("Mood: " + latestMood);
                            ((TextView) findViewById(R.id.spotifyMoodSubtitle)).setText("Here's a playlist to match your mood.");
                            findViewById(R.id.btn_listen_spotify).setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playlistUrl));
                                v.getContext().startActivity(intent);
                            });
                        }
                    }
                },

                e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error loading entries", Toast.LENGTH_SHORT).show();
                }
        );
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadJournalEntries();  // Refresh entries when coming back
    }

}
