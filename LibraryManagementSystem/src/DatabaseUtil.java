import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/LibraryDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "T3rM1nAt0r_mysql@23csu042";

    public static void loadDriver() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
