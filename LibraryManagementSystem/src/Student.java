import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

class Student {

    private static boolean isValidStudent(Connection connection, int studentId, String password) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        String query = "SELECT * FROM students WHERE student_id = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, hashedPassword);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean studentLogin(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (isValidStudent(connection, studentId, password)) {
            System.out.println("Login successful.");
            studentMenu(connection, studentId);
            return true;
        } else {
            System.out.println("Invalid Student ID or Password.");
            return false;
        }
    }


    private static void studentMenu(Connection connection, int studentId) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\nStudent Menu");
            System.out.println("1. View Borrowed Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> viewIssuedBooks(connection, studentId);
                case 2 -> issueBook(connection, studentId);
                case 3 -> returnBook(connection, studentId);
                case 4 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 4);
    }


    private static boolean validateStudentForAction(Connection connection, int studentId) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Re-enter your password to confirm: ");
        String password = scanner.nextLine();
        if (isValidStudent(connection, studentId, password)) {
            return true;
        } else {
            System.out.println("Authentication failed. Returning to menu.");
            return false;
        }
    }


    private static void issueBook(Connection connection, int studentId) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Book ID to issue: ");
        int bookId = scanner.nextInt();

        String checkQuery = "SELECT available FROM books WHERE book_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bookId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getBoolean("available")) {
                    String issueQuery = "INSERT INTO transactions (student_id, book_id) VALUES (?, ?)";
                    try (PreparedStatement issueStmt = connection.prepareStatement(issueQuery)) {
                        issueStmt.setInt(1, studentId);
                        issueStmt.setInt(2, bookId);
                        issueStmt.executeUpdate();

                        String updateQuery = "UPDATE books SET available = FALSE WHERE book_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, bookId);
                            updateStmt.executeUpdate();
                        }

                        System.out.println("Book issued successfully.");
                    }
                } else {
                    System.out.println("Book is not available.");
                }
            }
        }
    }

    private static void returnBook(Connection connection, int studentId) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Book ID to return: ");
        int bookId = scanner.nextInt();

        String checkQuery = "SELECT * FROM transactions WHERE student_id = ? AND book_id = ? AND return_date IS NULL";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, bookId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String returnQuery = "UPDATE transactions SET return_date = CURRENT_TIMESTAMP WHERE student_id = ? AND book_id = ?";
                    try (PreparedStatement returnStmt = connection.prepareStatement(returnQuery)) {
                        returnStmt.setInt(1, studentId);
                        returnStmt.setInt(2, bookId);
                        returnStmt.executeUpdate();

                        String updateQuery = "UPDATE books SET available = TRUE WHERE book_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, bookId);
                            updateStmt.executeUpdate();
                        }

                        System.out.println("Book returned successfully.");
                    }
                } else {
                    System.out.println("No such transaction found.");
                }
            }
        }
    }

    private static void viewIssuedBooks(Connection connection, int studentId) throws SQLException {
        String query = "SELECT b.book_id, b.title, b.author, t.issue_date " +
                       "FROM transactions t JOIN books b ON t.book_id = b.book_id " +
                       "WHERE t.student_id = ? AND t.return_date IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Book ID: " + rs.getInt("book_id") +
                            ", Title: " + rs.getString("title") +
                            ", Author: " + rs.getString("author") +
                            ", Issue Date: " + rs.getTimestamp("issue_date"));
                }
            }
        }
    }
}