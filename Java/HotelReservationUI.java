package ui;

import model.*;
import util.PaymentSimulator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

/**
 * Main GUI for hotel reservation system.
 */
public class HotelReservationUI extends JFrame {

    private final Hotel hotel;
    private final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

    // UI components
    private JComboBox<String> categoryCombo;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JTable resultsTable;
    private DefaultTableModel resultsModel;

    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;

    public HotelReservationUI(Hotel hotel) {
        this.hotel = hotel;
        setTitle("Hotel Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(root);

        // Top: search panel
        JPanel top = new JPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        top.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"Any", "STANDARD", "DELUXE", "SUITE"});
        top.add(categoryCombo);
        top.add(new JLabel("Check-in (YYYY-MM-DD):"));
        checkInField = new JTextField(LocalDate.now().toString(), 10);
        top.add(checkInField);
        top.add(new JLabel("Check-out (YYYY-MM-DD):"));
        checkOutField = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
        top.add(checkOutField);
        JButton searchBtn = new JButton("Search Available");
        top.add(searchBtn);
        JButton refreshBtn = new JButton("Refresh Bookings");
        top.add(refreshBtn);

        root.add(top, BorderLayout.NORTH);

        // Center split: left results, right bookings
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);

        // Left: search results and booking form
        JPanel left = new JPanel(new BorderLayout(6,6));
        resultsModel = new DefaultTableModel(new String[]{"Room ID","Category","Price/Nt","Description"}, 0) {
            public boolean isCellEditable(int r, int c){ return false; }
        };
        resultsTable = new JTable(resultsModel);
        left.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField guestField = new JTextField(12);
        bookPanel.add(new JLabel("Guest name:"));
        bookPanel.add(guestField);
        JButton bookBtn = new JButton("Book Selected Room (Pay)");
        JButton holdBtn = new JButton("Hold Booking (No payment)");
        bookPanel.add(bookBtn);
        bookPanel.add(holdBtn);
        left.add(bookPanel, BorderLayout.SOUTH);

        split.setLeftComponent(left);

        // Right: bookings list and cancel
        JPanel right = new JPanel(new BorderLayout(6,6));
        bookingsModel = new DefaultTableModel(new String[]{"BookingID","RoomID","Guest","CheckIn","CheckOut","Amount","Paid"},0) {
            public boolean isCellEditable(int r, int c){ return false; }
        };
        bookingsTable = new JTable(bookingsModel);
        right.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewDetailsBtn = new JButton("View Booking Details");
        JButton cancelBtn = new JButton("Cancel Booking");
        rightButtons.add(viewDetailsBtn);
        rightButtons.add(cancelBtn);
        right.add(rightButtons, BorderLayout.SOUTH);

        split.setRightComponent(right);
        root.add(split, BorderLayout.CENTER);

        // initial load
        refreshBookingsTable();
        // action listeners
        searchBtn.addActionListener(e -> performSearch());
        refreshBtn.addActionListener(e -> refreshBookingsTable());

        bookBtn.addActionListener(e -> {
            int row = resultsTable.getSelectedRow();
            if (row < 0) { showMessage("Select a room from search results."); return; }
            String roomId = (String) resultsModel.getValueAt(row, 0);
            String guest = guestField.getText().trim();
            if (guest.isEmpty()) { showMessage("Enter guest name."); return; }
            LocalDate checkIn, checkOut;
            try {
                checkIn = LocalDate.parse(checkInField.getText().trim(), df);
                checkOut = LocalDate.parse(checkOutField.getText().trim(), df);
            } catch (Exception ex) { showMessage("Invalid dates format. Use YYYY-MM-DD."); return; }
            if (!checkIn.isBefore(checkOut)) { showMessage("Check-out must be after check-in."); return; }

            // Payment dialog
            double nights = (double)(checkOut.toEpochDay() - checkIn.toEpochDay());
            double pricePerNight = Double.parseDouble(resultsModel.getValueAt(row,2).toString());
            double total = nights * pricePerNight;

            JPanel paymentPanel = new JPanel(new GridLayout(0,2,6,6));
            JTextField cardName = new JTextField();
            JTextField cardNumber = new JTextField();
            JTextField expiry = new JTextField("MM/YY");
            JTextField cvv = new JTextField();
            paymentPanel.add(new JLabel("Amount (₹):")); paymentPanel.add(new JLabel(String.format("%.2f", total)));
            paymentPanel.add(new JLabel("Card holder:")); paymentPanel.add(cardName);
            paymentPanel.add(new JLabel("Card number:")); paymentPanel.add(cardNumber);
            paymentPanel.add(new JLabel("Expiry (MM/YY):")); paymentPanel.add(expiry);
            paymentPanel.add(new JLabel("CVV:")); paymentPanel.add(cvv);

            int opt = JOptionPane.showConfirmDialog(this, paymentPanel, "Enter payment details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) return;

            PaymentSimulator.PaymentResult pr = PaymentSimulator.process(cardName.getText(), cardNumber.getText(), expiry.getText(), cvv.getText(), total);
            if (!pr.success) {
                showMessage("Payment failed: " + pr.message);
                return;
            }

            Booking booking = hotel.createBooking(roomId, guest, checkIn, checkOut, true);
            if (booking == null) {
                showMessage("Room not available for selected dates (someone else booked in the meantime). Try another room/date.");
                refreshBookingsTable();
            } else {
                showMessage("Booking successful! ID: " + booking.getBookingId() + "\n" + pr.message);
                refreshBookingsTable();
            }
        });

        // Hold booking (no payment)
        holdBtn.addActionListener(e -> {
            int row = resultsTable.getSelectedRow();
            if (row < 0) { showMessage("Select a room from search results."); return; }
            String roomId = (String) resultsModel.getValueAt(row, 0);
            String guest = guestField.getText().trim();
            if (guest.isEmpty()) { showMessage("Enter guest name."); return; }
            LocalDate checkIn, checkOut;
            try {
                checkIn = LocalDate.parse(checkInField.getText().trim(), df);
                checkOut = LocalDate.parse(checkOutField.getText().trim(), df);
            } catch (Exception ex) { showMessage("Invalid dates format. Use YYYY-MM-DD."); return; }
            if (!checkIn.isBefore(checkOut)) { showMessage("Check-out must be after check-in."); return; }

            Booking booking = hotel.createBooking(roomId, guest, checkIn, checkOut, false);
            if (booking == null) {
                showMessage("Room not available for selected dates.");
            } else {
                showMessage("Booking held successfully (not paid). ID: " + booking.getBookingId());
                refreshBookingsTable();
            }
        });

        // view details
        viewDetailsBtn.addActionListener(e -> {
            int r = bookingsTable.getSelectedRow();
            if (r < 0) { showMessage("Select a booking."); return; }
            String bid = (String) bookingsModel.getValueAt(r, 0);
            Booking b = hotel.getBookingById(bid);
            if (b == null) { showMessage("Booking not found."); refreshBookingsTable(); return; }
            Room room = hotel.getRoomById(b.getRoomId());
            String msg = "Booking ID: " + b.getBookingId()
                    + "\nGuest: " + b.getGuestName()
                    + "\nRoom: " + b.getRoomId() + " (" + (room != null ? room.getCategory() : "Unknown") + ")"
                    + "\nCheck-in: " + b.getCheckIn()
                    + "\nCheck-out: " + b.getCheckOut()
                    + "\nAmount: ₹" + String.format("%.2f", b.getAmount())
                    + "\nPaid: " + b.isPaid();
            JOptionPane.showMessageDialog(this, msg, "Booking Details", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelBtn.addActionListener(e -> {
            int r = bookingsTable.getSelectedRow();
            if (r < 0) { showMessage("Select a booking to cancel."); return; }
            String bid = (String) bookingsModel.getValueAt(r, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Cancel booking " + bid + " ? This will free the room for those dates.", "Confirm cancel", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            boolean ok = hotel.cancelBooking(bid);
            if (ok) {
                showMessage("Booking canceled.");
                refreshBookingsTable();
            } else {
                showMessage("Failed to cancel booking (maybe already removed).");
            }
        });
    }

    private void performSearch() {
        resultsModel.setRowCount(0);
        String cat = (String) categoryCombo.getSelectedItem();
        model.Room.Category category = null;
        if (!"Any".equals(cat)) category = model.Room.Category.valueOf(cat);

        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInField.getText().trim(), df);
            checkOut = LocalDate.parse(checkOutField.getText().trim(), df);
        } catch (Exception e) {
            showMessage("Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (!checkIn.isBefore(checkOut)) {
            showMessage("Check-out must be after check-in.");
            return;
        }

        List<Room> avail = hotel.searchAvailable(checkIn, checkOut, category);
        if (avail.isEmpty()) {
            showMessage("No rooms available for selected criteria.");
            return;
        }
        for (Room r : avail) {
            resultsModel.addRow(new Object[]{r.getId(), r.getCategory().toString(), r.getPricePerNight(), r.getDescription()});
        }
    }

    private void refreshBookingsTable() {
        bookingsModel.setRowCount(0);
        for (Booking b : hotel.getAllBookings()) {
            bookingsModel.addRow(new Object[]{b.getBookingId(), b.getRoomId(), b.getGuestName(), b.getCheckIn().toString(), b.getCheckOut().toString(), String.format("₹%.2f", b.getAmount()), b.isPaid()});
        }
    }

    private void showMessage(String m) {
        JOptionPane.showMessageDialog(this, m);
    }

    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        hotel.load();

        SwingUtilities.invokeLater(() -> {
            HotelReservationUI ui = new HotelReservationUI(hotel);
            ui.setVisible(true);
        });

        // Save rooms state on shutdown (if any changes)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hotel.saveRooms();
            hotel.saveBookings();
        }));
    }
}
