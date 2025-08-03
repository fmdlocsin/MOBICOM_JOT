package com.mobicom.mco3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.Timestamp;
public class JournalEntry implements Serializable {
    private String title;
    private String reflection;
    private String mood;
    private Timestamp timestamp;
    private String id;
    //This one is for the firestone bit
    private List<String> imageBase64List;
    public JournalEntry() {
        this.imageBase64List = new ArrayList<>();
    }

    public JournalEntry(String title, String reflection, String mood, Timestamp timestamp) {
        this.title = title;
        this.reflection = reflection;
        this.mood = mood;
        this.timestamp = timestamp;
        this.imageBase64List = new ArrayList<>();
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

    public List<String> getImageBase64List() {
        return imageBase64List;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }

    public void setImageBase64List(List<String> imageBase64List) {
        this.imageBase64List = imageBase64List;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
