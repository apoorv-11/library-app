import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Admin {

    public static boolean adminLogin(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Admin Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String hashedPassword = PasswordUtil.hashPassword(password);

        String query = "SELECT * FROM admins WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login successful.");
                    adminMenu(connection);
                    return true;
                } else {
                    System.out.println("Invalid email or password.");
                    return false;
                }
            }
        }
    }

    private static void adminMenu(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\nAdmin Menu");
            System.out.println("1. Add Student");
            System.out.println("2. Delete Student");
            System.out.println("3. View Students");
            System.out.println("4. Add Book");
            System.out.println("5. Delete Book");
            System.out.println("6. View Books");
            System.out.println("7. View Transactions");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> addStudent(connection);
                case 2 -> deleteStudent(connection);
                case 3 -> viewStudents(connection);
                case 4 -> addBook(connection);
                case 5 -> deleteBook(connection);
                case 6 -> viewBooks(connection);
                case 7 -> viewTransactions(connection);
                case 8 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 8);
    }

    private static void addStudent(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Student Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String hashedPassword = PasswordUtil.hashPassword(password);

        String query = "INSERT INTO students (name, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.executeUpdate();
            System.out.println("Student added successfully.");
        }
    }

    private static void deleteStudent(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Student ID to delete: ");
        int studentId = scanner.nextInt();

        // Fetch the student name before deleting
        String fetchQuery = "SELECT name FROM students WHERE student_id = ?";
        try (PreparedStatement fetchStmt = connection.prepareStatement(fetchQuery)) {
            fetchStmt.setInt(1, studentId);
            try (ResultSet rs = fetchStmt.executeQuery()) {
                if (rs.next()) {
                    String studentName = rs.getString("name");

                    // Proceed to delete the student
                    String deleteQuery = "DELETE FROM students WHERE student_id = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, studentId);
                        int rowsAffected = deleteStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Student deleted successfully:");
                            System.out.println("ID: " + studentId + ", Name: " + studentName);
                        } else {
                            System.out.println("No student found with the given ID.");
                        }
                    }
                } else {
                    System.out.println("No student found with the given ID.");
                }
            }
        }
    }

    private static void viewBooks(Connection connection) throws SQLException {
        String query = "SELECT book_id, title, author, available FROM books";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nBooks:");
            System.out.println("----------------------------------------------------");
            System.out.printf("%-10s %-30s %-20s %-10s%n", "Book ID", "Title", "Author", "Available");
            System.out.println("----------------------------------------------------");
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean available = rs.getBoolean("available");
                System.out.printf("%-10d %-30s %-20s %-10s%n", bookId, title, author, available ? "Yes" : "No");
            }
        }
    }

    private static void viewStudents(Connection connection) throws SQLException {
        String query = "SELECT student_id, name, email FROM students";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nStudents:");
            System.out.println("----------------------------------------------------");
            System.out.printf("%-10s %-20s %-30s%n", "Student ID", "Name", "Email");
            System.out.println("----------------------------------------------------");
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                System.out.printf("%-10d %-20s %-30s%n", studentId, name, email);
            }
        }
    }

    private static void addBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Book Author: ");
        String author = scanner.nextLine();

        String query = "INSERT INTO books (title, author, available) VALUES (?, ?, TRUE)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.executeUpdate();
            System.out.println("Book added successfully.");
        }
    }

    private static void deleteBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Book ID to delete: ");
        int bookId = scanner.nextInt();

        String query = "DELETE FROM books WHERE book_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book deleted successfully.");
            } else {
                System.out.println("No book found with the given ID.");
            }
        }
    }

    private static void viewTransactions(Connection connection) throws SQLException {
        String query = "SELECT t.transaction_id, t.student_id, s.name AS student_name, t.book_id, b.title AS book_title, t.issue_date, t.return_date " +
                       "FROM transactions t " +
                       "JOIN students s ON t.student_id = s.student_id " +
                       "JOIN books b ON t.book_id = b.book_id";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nTransactions:");
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-10s %-20s %-10s %-30s %-20s %-20s%n", "Transaction ID", "Student ID", "Student Name", "Book ID", "Book Title", "Issue Date", "Return Date");
            System.out.println("------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("student_name");
                int bookId = rs.getInt("book_id");
                String bookTitle = rs.getString("book_title");
                String issueDate = rs.getTimestamp("issue_date").toString();
                String returnDate = rs.getTimestamp("return_date") != null ? rs.getTimestamp("return_date").toString() : "Not Returned";
                System.out.printf("%-15d %-10d %-20s %-10d %-30s %-20s %-20s%n", transactionId, studentId, studentName, bookId, bookTitle, issueDate, returnDate);
            }
        }
    }
}