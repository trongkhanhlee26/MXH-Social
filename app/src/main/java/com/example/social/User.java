package com.example.social;

public class User {
    private String userId;
    private String fullName;
    private String image;
    private String gender;
    private String dateOfBirth;
    private String email;
    private boolean onlineStatus;
    public User() {
        // Default constructor required by Firebase
    }

    public User(String fullName, String image, String gender, String dateOfBirth, String userId, String email, boolean onlineStatus) {
        this.fullName = fullName;
        this.image = image;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.userId = userId;
        this.email = email;
        this.onlineStatus = onlineStatus;
    }
    public String getUserId() {
        return userId;
    }
    public String getFullName() {
        return fullName;
    }
    public String getImage() {
        return image;
    }
    public String getGender() {
        return gender;
    }
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }
    public boolean isOnlineStatus() {
        return onlineStatus;
    }
    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

}
