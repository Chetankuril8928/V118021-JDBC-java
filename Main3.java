import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

class Room {
    int roomNumber;
    boolean isBooked;
    String guestName;

    public Room(int roomNumber) {
        this.roomNumber = roomNumber;
        this.isBooked = false;
    }

    public void bookRoom(String guestName) {
        if (!isBooked) {
            this.guestName = guestName;
            this.isBooked = true;
            System.out.println("Room " + roomNumber + " booked successfully for " + guestName);
        } else {
            System.out.println("Room " + roomNumber + " is already booked.");
        }
    }

    public void releaseRoom() {
        if (isBooked) {
            System.out.println("Room " + roomNumber + " released from " + guestName);
            this.isBooked = false;
            this.guestName = null;
        } else {
            System.out.println("Room " + roomNumber + " is not currently booked.");
        }
    }
}

class Hotel {
    ArrayList<Room> rooms;

    public Hotel(int numRooms) {
        rooms = new ArrayList<>(numRooms);
        for (int i = 1; i <= numRooms; i++) {
            rooms.add(new Room(i));
        }
    }

    public void displayStatus() {
        for (Room room : rooms) {
            System.out.println("Room " + room.roomNumber + " is " + (room.isBooked ? "booked by " + room.guestName : "available"));
        }
    }

    public void bookRoom(int roomNumber, String guestName) {
        if (roomNumber > 0 && roomNumber <= rooms.size()) {
            rooms.get(roomNumber - 1).bookRoom(guestName);
        } else {
            System.out.println("Invalid room number.");
        }
    }

    public void releaseRoom(int roomNumber) {
        if (roomNumber > 0 && roomNumber <= rooms.size()) {
            rooms.get(roomNumber - 1).releaseRoom();
        } else {
            System.out.println("Invalid room number.");
        }
    }

    public void loadRoomsFromDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                int roomNumber = rs.getInt("room_number");
                boolean isBooked = rs.getBoolean("is_booked");
                String guestName = rs.getString("guest_name");
                Room room = new Room(roomNumber);
                room.isBooked = isBooked;
                room.guestName = guestName;
                rooms.add(room);
            }
            System.out.println("Rooms loaded from the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRoomInDatabase(Room room, Connection conn) {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE rooms SET is_booked = ?, guest_name = ? WHERE room_number = ?")) {
            pstmt.setBoolean(1, room.isBooked);
            pstmt.setString(2, room.guestName);
            pstmt.setInt(3, room.roomNumber);
            pstmt.executeUpdate();
            System.out.println("Room " + room.roomNumber + " updated in the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class Main3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of rooms in the hotel: ");
        int numRooms = scanner.nextInt();

        Hotel hotel = new Hotel(numRooms);

        // Database connection details
        String url = "jdbc:mysql://localhost:3307/hoteldb";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Database connected successfully!");

            hotel.loadRoomsFromDatabase(conn);

            while (true) {
                System.out.println("\nHotel Management System");
                System.out.println("1. Display Room Status");
                System.out.println("2. Book Room");
                System.out.println("3. Release Room");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        hotel.displayStatus();
                        break;
                    case 2:
                        System.out.print("Enter room number to book: ");
                        int roomNumberToBook = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter guest name: ");
                        String guestName = scanner.nextLine();
                        hotel.bookRoom(roomNumberToBook, guestName);
                        Room bookedRoom = hotel.rooms.get(roomNumberToBook - 1);
                        hotel.updateRoomInDatabase(bookedRoom, conn);
                        break;
                    case 3:
                        System.out.print("Enter room number to release: ");
                        int roomNumberToRelease = scanner.nextInt();
                        hotel.releaseRoom(roomNumberToRelease);
                        Room releasedRoom = hotel.rooms.get(roomNumberToRelease - 1);
                        hotel.updateRoomInDatabase(releasedRoom, conn);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
