package model;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Category { STANDARD, DELUXE, SUITE }

    private final String id; // e.g. "R001"
    private final Category category;
    private final double pricePerNight;
    private String description;

    public Room(String id, Category category, double pricePerNight, String description) {
        this.id = id;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.description = description;
    }

    public String getId() { return id; }
    public Category getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }

    @Override
    public String toString() {
        return id + " | " + category + " | ₹" + pricePerNight + " | " + description;
    }
}
