import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static void main(String[] args) {
        // Database connection parameters
        // String url = "jdbc:postgresql://vcm-41481.vm.duke.edu:5432/proj24db";
        // String username = "postgres";
        // String password = "123456";
        String url = "jdbc:postgresql://vcm-41568.vm.duke.edu:5432/proj24db";
        String username = "postgres";
        String password = "12345";
        

        // Load the PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load PostgreSQL JDBC driver.");
            e.printStackTrace();
            return;
        }

        // Establish connection
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected to the PostgreSQL server successfully.");

            // Perform database operations here...

        } catch (SQLException e) {
            System.out.println("Connection failed. Check the stack trace for details.");
            e.printStackTrace();
        }
    }
}
