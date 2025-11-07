import java.sql.*;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bankingsimulation", "root", "2004")) {
            System.out.println("✅ Connected to the database!");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }
}
