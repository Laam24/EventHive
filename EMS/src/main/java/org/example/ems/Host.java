package org.example.ems;

public class Host {
    private int id;
    private String name;
    private String specialty;
    private double rating;
    private String imageUrl;

    public Host(int id, String name, String specialty, double rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        // Return a description of the host
        return "Description of the host"; // Replace with actual description
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public double getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }

    // Setters (if needed)
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setRating(double rating) { this.rating = rating; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}