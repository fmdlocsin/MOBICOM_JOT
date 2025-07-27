package com.mobicom.mco3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.EntryViewHolder> {

    private List<JournalEntry> journalList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public JournalAdapter(Context context, List<JournalEntry> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        JournalEntry entry = journalList.get(position);
        holder.tvTitle.setText(entry.getTitle());
        holder.tvReflection.setText(entry.getReflection());
        holder.tvMood.setText("Mood: " + entry.getMood());

        Timestamp timestamp = entry.getTimestamp();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date);
            holder.tvTimestamp.setText(formattedDate);
        } else {
            holder.tvTimestamp.setText("No timestamp");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("entryId", entry.getId());
            context.startActivity(intent);
        });
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditJournalActivity.class);
            intent.putExtra("entryId", entry.getId());  // Make sure JournalEntry implements Serializable
            v.getContext().startActivity(intent);
        });

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("users").document(user.getUid())
                                .collection("entries").document(entry.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                    journalList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvReflection, tvMood, tvTimestamp;
        ImageButton btnEdit, btnDelete;
        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.textTitle);
            tvReflection = itemView.findViewById(R.id.textContent);
            tvMood = itemView.findViewById(R.id.textMood);
            tvTimestamp = itemView.findViewById(R.id.textTimestamp);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }
    }
}