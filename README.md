# CodeAlpha_HotelReservationSystem
# Hotel Reservation System (Java Swing, File-based persistence)

## What it is
A simple hotel reservation desktop app using Java, Swing UI, and file-based persistence (serialization).
Features:
- Room categories (Standard, Deluxe, Suite)
- Search rooms by category and date
- Create bookings (payment simulation or hold booking)
- View and cancel bookings
- Data persisted to `data/rooms.dat` and `data/bookings.dat`

## Requirements
- JDK 17+
- VS Code (recommended) + Java Extension Pack (Language Support for Java(TM), Debugger for Java)

## How to run
1. Open the project folder (`HotelReservationSystem`) in VS Code.
2. Build/run `src/ui/HotelReservationUI.java` (Run button or F5).
   OR build via terminal:
   - Compile:
     ```
     javac -d out src/model/*.java src/util/*.java src/ui/*.java
     ```
   - Run:
     ```
     java -cp out ui.HotelReservationUI
     ```
3. The GUI will open. Data files created automatically in `data/`.

## Notes
- Payment is purely simulated.
- For production you should replace serialization with a proper database and add input validation & security.
