package model;

import java.time.LocalDate;

public class Event {
    private String id;
    private String title;
    private String description;
    private LocalDate date;
    private EventType type;
    private String portfolioId; // null if global event

    public Event() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Event(String title, String description, LocalDate date, EventType type) {
        this();
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    public Event(String title, String description, LocalDate date, EventType type, String portfolioId) {
        this(title, description, date, type);
        this.portfolioId = portfolioId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public boolean isGlobal() {
        return portfolioId == null || portfolioId.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", date, type.getDisplayName(), title);
    }
}