package org.example.ems;

import java.time.LocalDateTime;

public class ContactMessage {
    private int id;
    private String name;
    private String email;
    private String message;
    private LocalDateTime submissionDate;

    public ContactMessage(int id, String name, String email, String message, LocalDateTime submissionDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.submissionDate = submissionDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
// Getters and setters
    // ...
}