package com.example.logbook3_contactdatabase;

/**
 * Model class for a Contact.
 */
public class Contact {
    private int id;
    private String name;
    private String phoneNumber;
    private String imageUri; // Changed from avatarResId to imageUri

    public Contact(int id, String name, String phoneNumber, String imageUri) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.imageUri = imageUri;
    }

    public Contact(String name, String phoneNumber, String imageUri) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.imageUri = imageUri;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getImageUri() { return imageUri; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phone) { this.phoneNumber = phone; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}