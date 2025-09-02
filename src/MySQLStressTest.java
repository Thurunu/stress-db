import java.sql.*;
import java.util.Random;
import java.util.concurrent.*;

public class MySQLStressTest {

    // Change DB credentials here
    private static final String URL = "jdbc:mysql://192.168.1.8:3306/testdb";
    private static final String USER = "server_user";
    private static final String PASS = "2115";

    private static final Random rand = new Random();

    public static void main(String[] args) {
        int numThreads = (args.length > 0) ? Integer.parseInt(args[0]) : 5; // Adjust default as needed; higher = more stress

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Create table using a temporary connection
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            createTableIfNotExists(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Starting stress test with " + numThreads + " threads. Press CTRL+C to stop...");

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new StressRunnable());
        }

        // Add shutdown hook for graceful termination on CTRL+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdownNow();
            System.out.println("Shutdown initiated.");
        }));

        // Wait for termination (won't happen until shutdown)
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // Expected on shutdown
        }
    }

    private static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS stress_test (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "data VARCHAR(255) NOT NULL)";
        conn.createStatement().execute(sql);
    }

    private static class StressRunnable implements Runnable {
        private final Random rand = new Random();

        @Override
        public void run() {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                while (!Thread.currentThread().isInterrupted()) {
                    int op = rand.nextInt(4); // 0=INSERT, 1=SELECT, 2=UPDATE, 3=DELETE
                    switch (op) {
                        case 0: insertRandom(conn); break;
                        case 1: selectRandom(conn); break;
                        case 2: updateRandom(conn); break;
                        case 3: deleteRandom(conn); break;
                    }
                    // No delay to maximize workload
                }
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void insertRandom(Connection conn) throws SQLException {
        String data = "Data_" + rand.nextInt(10000);
        String sql = "INSERT INTO stress_test (data) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data);
            ps.executeUpdate();
        }
        // System.out.println("INSERT: " + data); // Commented out to reduce overhead and maximize speed
    }

    private static void selectRandom(Connection conn) throws SQLException {
        String sql = "SELECT * FROM stress_test ORDER BY RAND() LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                // System.out.println("SELECT: id=" + rs.getInt("id") + ", data=" + rs.getString("data")); // Commented out
            } else {
                // System.out.println("SELECT: table empty"); // Commented out
            }
        }
    }

    private static void updateRandom(Connection conn) throws SQLException {
        int id = getRandomId(conn);
        if (id == -1) return;
        String newData = "Updated_" + rand.nextInt(10000);
        String sql = "UPDATE stress_test SET data=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newData);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
        // System.out.println("UPDATE id=" + id + " -> " + newData); // Commented out
    }

    private static void deleteRandom(Connection conn) throws SQLException {
        int id = getRandomId(conn);
        if (id == -1) return;
        String sql = "DELETE FROM stress_test WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        // System.out.println("DELETE id=" + id); // Commented out
    }

    private static int getRandomId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM stress_test ORDER BY RAND() LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }
}