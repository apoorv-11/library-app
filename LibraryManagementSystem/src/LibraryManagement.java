import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class LibraryManagement {

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/LibraryDB", "root", "T3rM1nAt0r_mysql@23csu042")) {
            Scanner scanner = new Scanner(System.in);
            int loginChoice;

            while (true) {
                System.out.println("\nWelcome to Library Management System");
                System.out.println("1. Admin Login");
                System.out.println("2. Student Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                loginChoice = scanner.nextInt();

                switch (loginChoice) {
                    case 1 -> {
                        if (!Admin.adminLogin(connection)) {
                            System.out.println("Returning to login page...");
                        }
                    }
                    case 2 -> {
                        if (!Student.studentLogin(connection)) {
                            System.out.println("Returning to login page...");
                        }
                    }
                    case 3 -> {
                        System.out.println("Exiting system. Goodbye!");
                        return; // Exit the application
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}
