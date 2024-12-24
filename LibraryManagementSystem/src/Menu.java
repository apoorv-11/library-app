import java.sql.Connection;
import java.util.Scanner;

class Menu {
    public static void displayMainMenu(Connection connection) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Library Management System");
        System.out.println("1. Admin Login");
        System.out.println("2. Student Login");
        System.out.print("Choose your role: ");
        int role = scanner.nextInt();

        switch (role) {
            case 1 -> Admin.adminLogin(connection);
            case 2 -> Student.studentLogin(connection);
            default -> System.out.println("Invalid option.");
        }
    }
}

