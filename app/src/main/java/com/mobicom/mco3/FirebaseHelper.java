package com.mobicom.mco3;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final FirebaseFirestore db;
    private final FirebaseUser user;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void saveEntry(JournalEntry entry,
                          OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {
        if (user == null) {
            onFailure.onFailure(new Exception("No authenticated user"));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("entries")
                .add(entry)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void getJournalEntries(OnSuccessListener<List<JournalEntry>> onSuccess,
                                  OnFailureListener onFailure) {
        if (user == null) {
            onFailure.onFailure(new Exception("No authenticated user"));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("entries")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<JournalEntry> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        JournalEntry entry = doc.toObject(JournalEntry.class);
                        entries.add(entry);
                    }
                    onSuccess.onSuccess(entries);
                })
                .addOnFailureListener(onFailure);
    }
}
