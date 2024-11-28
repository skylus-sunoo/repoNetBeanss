package project;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Queries {

    private static final String DB_FOLDER = "databases/";
    private static final String DB_EXTENSION = ".accdb";

    public static Connection getConnection(String dbName) throws SQLException {
        String dbPath = DB_FOLDER + dbName + DB_EXTENSION;
        String dbUrl = "jdbc:ucanaccess://" + System.getProperty("user.dir") + "/" + dbPath;
        return DriverManager.getConnection(dbUrl);
    }

    public static PreparedStatement prepareQuery(Connection conn, String query) throws SQLException {
        return conn.prepareStatement(query);
    }

    public static PreparedStatement prepareQueryWithParameters(Connection conn, String query, String... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setString(i + 1, params[i]);
        }
        return stmt;
    }

    public static boolean recordExists(Connection conn, String query, String... params) throws SQLException {
        try (PreparedStatement stmt = prepareQueryWithParameters(conn, query, params); ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static void executeUpdate(Connection conn, String query, String... params) throws SQLException {
        try (PreparedStatement stmt = prepareQueryWithParameters(conn, query, params)) {
            stmt.executeUpdate();
        }
    }
}
