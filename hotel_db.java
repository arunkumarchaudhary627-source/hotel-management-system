package hotel;

import java.sql.*;

public final class hotel_db {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String URL = "jdbc:mysql://localhost:3306/hotel";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";
    private static boolean initialized = false;
    // Logging in to MySQL takes ~0.4s, so one connection is opened once and reused by every page.
    private static Connection shared;
    private hotel_db() { }
    public static synchronized Connection connect() throws SQLException {
        try {
            if (shared == null || !shared.isValid(2)) {
                shared = open();
            }
            return keepOpen(shared);
        } catch (SQLException e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("No suitable driver")) {
                throw new SQLException("MySQL driver not found. Add mysql-connector-j jar to the project libraries.", e);
            }
            if (msg.contains("Communications link failure") || msg.contains("Connection refused")) {
                throw new SQLException("Cannot reach MySQL on localhost:3306. Start the MySQL server and try again.", e);
            }
            throw e;
        }
    }

    /** Wraps the shared connection so the callers' try-with-resources close() does not really close it. */
    private static Connection keepOpen(Connection real) {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) return null;
                    try {
                        return method.invoke(real, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private static Connection open() throws SQLException {
        if (!initialized) {
            try (Connection server = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
                 Statement statement = server.createStatement()) {
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS hotel");
            }
        }
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        if (!initialized) {
            // Create tables only once per run instead of on every query.
            setup(connection);
            initialized = true;
        }
        return connection;
    }
    private static void setup(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `user` (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS driver (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, age INT NOT NULL, gender VARCHAR(20), car_model VARCHAR(100) NOT NULL, availability VARCHAR(30), location VARCHAR(100))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS customer (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, phone VARCHAR(20), email VARCHAR(100), room_number VARCHAR(20), check_in DATE, check_out DATE, status VARCHAR(20) DEFAULT 'Checked In')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS add_room (room_number VARCHAR(20) PRIMARY KEY, availability VARCHAR(30), price DOUBLE, cleaning_status VARCHAR(30), bed_type VARCHAR(30))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS employee (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, age INT, gender VARCHAR(20), job VARCHAR(100), salary DOUBLE, phone VARCHAR(20), aadhaar VARCHAR(20), email VARCHAR(100))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS room_service (id INT AUTO_INCREMENT PRIMARY KEY, customer_id INT, service_name VARCHAR(255), cost DOUBLE, order_date DATE, FOREIGN KEY (customer_id) REFERENCES customer(id))");
            st.executeUpdate("INSERT IGNORE INTO `user` (username, password) VALUES ('admin', 'admin123')");
            ensureEmployeeColumns(st);
        }
    }

    private static void ensureEmployeeColumns(Statement statement) {
        addColumnIfMissing(statement, "age INT");
        addColumnIfMissing(statement, "gender VARCHAR(20)");
        addColumnIfMissing(statement, "salary DOUBLE");
        addColumnIfMissing(statement, "aadhaar VARCHAR(20)");
    }

    private static void addColumnIfMissing(Statement statement, String definition) {
        try {
            statement.executeUpdate("ALTER TABLE employee ADD COLUMN " + definition);
        } catch (SQLException ignored) {
            // The column already exists in databases created by a newer version of the app.
        }
    }
}
