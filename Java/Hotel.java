package model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hotel manager: holds rooms and bookings, persists to disk using serialization.
 */
public class Hotel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Room> rooms = new LinkedHashMap<>(); // id -> room
    private final Map<String, Booking> bookings = new LinkedHashMap<>(); // bookingId -> booking

    private static final String ROOMS_FILE = "data/rooms.dat";
    private static final String BOOKINGS_FILE = "data/bookings.dat";

    // --- CRUD and search operations ---

    public synchronized void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public synchronized List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public synchronized List<Booking> getAllBookings() {
        return new ArrayList<>(bookings.values());
    }

    public synchronized Room getRoomById(String id) {
        return rooms.get(id);
    }

    public synchronized Booking getBookingById(String bookingId) {
        return bookings.get(bookingId);
    }

    /**
     * Search available rooms for the date range and optional category.
     */
    public synchronized List<Room> searchAvailable(LocalDate checkIn, LocalDate checkOut, Room.Category category) {
        List<Room> candidates = rooms.values().stream()
                .filter(r -> category == null || r.getCategory() == category)
                .collect(Collectors.toList());

        return candidates.stream().filter(r -> isRoomAvailable(r.getId(), checkIn, checkOut)).collect(Collectors.toList());
    }

    /**
     * True if no booking overlaps [checkIn, checkOut)
     */
    public synchronized boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        for (Booking b : bookings.values()) {
            if (!b.getRoomId().equals(roomId)) continue;
            // overlap check: if (start1 < end2) && (start2 < end1)
            if (b.getCheckIn().isBefore(checkOut) && checkIn.isBefore(b.getCheckOut())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a booking and persist it. Returns the bookingId or null on failure.
     */
    public synchronized Booking createBooking(String roomId, String guestName, LocalDate checkIn, LocalDate checkOut, boolean paid) {
        if (!isRoomAvailable(roomId, checkIn, checkOut)) return null;
        Room r = rooms.get(roomId);
        double nights = (double) (checkOut.toEpochDay() - checkIn.toEpochDay());
        double amount = r.getPricePerNight() * nights;
        Booking booking = new Booking(roomId, guestName, checkIn, checkOut, amount, paid);
        bookings.put(booking.getBookingId(), booking);
        saveBookings(); // persist
        return booking;
    }

    public synchronized boolean cancelBooking(String bookingId) {
        Booking b = bookings.remove(bookingId);
        if (b != null) { saveBookings(); return true; }
        return false;
    }

    // --- Persistence ---

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        // Ensure data folder exists
        File data = new File("data");
        if (!data.exists()) data.mkdirs();

        // load rooms
        File rf = new File(ROOMS_FILE);
        if (rf.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rf))) {
                Map<String, Room> loadedRooms = (Map<String, Room>) ois.readObject();
                rooms.clear();
                rooms.putAll(loadedRooms);
            } catch (Exception e) {
                System.err.println("Failed to load rooms: " + e.getMessage());
            }
        } else {
            // If file doesn't exist, create sample rooms
            createSampleRooms();
            saveRooms();
        }

        // load bookings
        File bf = new File(BOOKINGS_FILE);
        if (bf.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(bf))) {
                Map<String, Booking> loadedBookings = (Map<String, Booking>) ois.readObject();
                bookings.clear();
                bookings.putAll(loadedBookings);
            } catch (Exception e) {
                System.err.println("Failed to load bookings: " + e.getMessage());
            }
        }
    }

    public synchronized void saveRooms() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(rooms);
        } catch (Exception e) {
            System.err.println("Failed to save rooms: " + e.getMessage());
        }
    }

    public synchronized void saveBookings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            oos.writeObject(bookings);
        } catch (Exception e) {
            System.err.println("Failed to save bookings: " + e.getMessage());
        }
    }

    private void createSampleRooms() {
        addRoom(new Room("R001", Room.Category.STANDARD, 2500.0, "Standard room - single/double"));
        addRoom(new Room("R002", Room.Category.STANDARD, 2600.0, "Standard room with city view"));
        addRoom(new Room("R003", Room.Category.DELUXE, 4500.0, "Deluxe room - queen bed"));
        addRoom(new Room("R004", Room.Category.DELUXE, 4800.0, "Deluxe room - balcony"));
        addRoom(new Room("R005", Room.Category.SUITE, 9000.0, "Suite with living area"));
    }
}
