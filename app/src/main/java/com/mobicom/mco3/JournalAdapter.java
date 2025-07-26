package com.mobicom.mco3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.EntryViewHolder> {

    private List<JournalEntry> journalList;

    public JournalAdapter(List<JournalEntry> journalList) {
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

        Date date = entry.getTimestamp();
        String formatted = DateFormat.getDateTimeInstance().format(date);
        holder.tvTimestamp.setText(formatted);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvReflection, tvMood, tvTimestamp;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReflection = itemView.findViewById(R.id.tvReflection);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
