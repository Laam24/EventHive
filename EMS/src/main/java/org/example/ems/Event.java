package org.example.ems;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private int id;
    private String name;
    private String description;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private int hostId;
    private String category;
    private String bannerPath;
    private String eventType;
    private int totalAttendees;
    private boolean photographyService;
    private LocalDate registrationStart;
    private LocalDate registrationEnd;
    private double ticketPrice;
    private int maxAttendees;

    // Constructor
    public Event(String name, String description, LocalDate eventDate, LocalTime eventTime, int hostId, String category, String bannerPath, String eventType) {
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.hostId = hostId;
        this.category = category;
        this.bannerPath = bannerPath;
        this.eventType = eventType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getTotalAttendees() {
        return totalAttendees;
    }

    public void setTotalAttendees(int totalAttendees) {
        this.totalAttendees = totalAttendees;
    }

    public boolean isPhotographyService() {
        return photographyService;
    }

    public void setPhotographyService(boolean photographyService) {
        this.photographyService = photographyService;
    }

    public LocalDate getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(LocalDate registrationStart) {
        this.registrationStart = registrationStart;
    }

    public LocalDate getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(LocalDate registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

}
