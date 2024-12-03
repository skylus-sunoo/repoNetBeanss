package project;

import java.sql.SQLException;
import javax.swing.JOptionPane;

public class MainUtils {

    public static boolean isLoggedIn() {
        return project.Main.getUserSessionID() > 0;
    }

    public static void paneNotLoggedIn() {
        JOptionPane.showMessageDialog(null, "This action requires logging in!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void paneDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace(System.out);
    }
}
