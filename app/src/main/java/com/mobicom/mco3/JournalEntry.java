package com.mobicom.mco3;

import java.io.Serializable;
import com.google.firebase.Timestamp;
public class JournalEntry implements Serializable {
    private String title;
    private String reflection;
    private String mood;
    private Timestamp timestamp;
    private String id;
    //This one is for the firestone bit
    public JournalEntry() {}

    public JournalEntry(String title, String reflection, String mood, Timestamp timestamp) {
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTitle(String title) {this.title = title;}

    public void setMood(String mood) { this.mood = mood;}

    public void setReflection(String reflection) { this.reflection = reflection;}


    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
