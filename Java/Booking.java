package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String bookingId;
    private final String roomId;
    private final String guestName;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double amount;
    private boolean paid;

    public Booking(String roomId, String guestName, LocalDate checkIn, LocalDate checkOut, double amount, boolean paid) {
        this.bookingId = UUID.randomUUID().toString();
        this.roomId = roomId;
        this.guestName = guestName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.amount = amount;
        this.paid = paid;
    }

    public String getBookingId() { return bookingId; }
    public String getRoomId() { return roomId; }
    public String getGuestName() { return guestName; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public double getAmount() { return amount; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    @Override
    public String toString() {
        return String.format("%s | Room:%s | %s -> %s | %s | ₹%.2f", bookingId, roomId, checkIn, checkOut, guestName, amount);
    }
}
