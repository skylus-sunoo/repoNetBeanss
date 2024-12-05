package project;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import static project.MainUtils.*;

public class Queries {

    public static void testConnection(String dbName) {
        try (Connection conn = getConnection(dbName)) {
            System.out.println("Connection successful!");
            conn.close();
        } catch (SQLException e) {
            paneDatabaseError(e);
        }
    }

    public static Connection getConnection(String dbName) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/" + dbName;
        String username = "root";
        String password = "";

        return DriverManager.getConnection(url, username, password);
    }

    public static PreparedStatement prepareQuery(Connection conn, String query) throws SQLException {
        return conn.prepareStatement(query);
    }

    public static PreparedStatement prepareQueryWithParameters(Connection conn, String query, String... params) throws SQLException {
        PreparedStatement pst = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pst.setString(i + 1, params[i]);
        }
        return pst;
    }

    public static boolean recordExists(Connection conn, String query, String... params) throws SQLException {
        try (PreparedStatement pst = prepareQueryWithParameters(conn, query, params); ResultSet rs = pst.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static void executeUpdate(Connection conn, String query, String... params) throws SQLException {
        try (PreparedStatement pst = prepareQueryWithParameters(conn, query, params)) {
            pst.executeUpdate();
        }
    }

    public static void resetPrimaryKey(String tbName) {
        String query = "SELECT COUNT(*) FROM " + tbName;
        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQuery(conn, query); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int rowCount = rs.getInt(1);

                String alterQuery = "ALTER TABLE " + tbName + " AUTO_INCREMENT = ?";
                try (PreparedStatement alterPst = conn.prepareStatement(alterQuery)) {
                    alterPst.setInt(1, rowCount + 1); 
                    alterPst.executeUpdate();
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            }
        } catch (SQLException e) {
            paneDatabaseError(e);
        }
    }

    public static void repopulateComboBox(JComboBox comboBox, String columnName, String query) {
        Set<String> uniqueItems = new HashSet<>();
        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQuery(conn, query); ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString(columnName);
                if (name != null && !name.trim().isEmpty()) {
                    uniqueItems.add(name);
                }
            }

            List<String> sortedItems = new ArrayList<>(uniqueItems);
            Collections.sort(sortedItems, String.CASE_INSENSITIVE_ORDER);

            comboBox.removeAllItems();
            for (String item : sortedItems) {
                comboBox.addItem(item);
//                System.out.println(item);
            }

            if (!sortedItems.isEmpty()) {
                comboBox.setSelectedIndex(0);
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            paneDatabaseError(e);
        }
    }
}
