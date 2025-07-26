package com.mobicom.mco3;

import java.util.Date;

public class JournalEntry {
    private String title;
    private String reflection;
    private String mood;
    private Date timestamp;

    //This one is for the firestone bit
    public JournalEntry() {}

    public JournalEntry(String title, String reflection, String mood, Date timestamp) {
        this.title = title;
        this.reflection = reflection;
        this.mood = mood;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getReflection() {
        return reflection;
    }

    public String getMood() {
        return mood;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
