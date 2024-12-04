package project;

import com.mysql.cj.jdbc.Blob;
import java.awt.Image;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TableUtils {

    public static ImageIcon blobToImage(ResultSet rs, String column_name) throws SQLException {
        Blob blob = (Blob) rs.getBlob(column_name);
        ImageIcon imageIcon = null;
        if (blob != null) {
            byte[] imageBytes = blob.getBytes(1, (int) blob.length());
            imageIcon = new ImageIcon(imageBytes);
            Image img = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(img);
        }

        return imageIcon;
    }

    public final static void refreshTableStockDistinct(JTable tableProduct) {
        String query = "SELECT * FROM " + Main.tbName_ProductStock;

        refreshTableStock(tableProduct, query);
    }

    public final static void refreshTableStockAll(JTable tableProduct) {
        String query = "SELECT * FROM " + Main.tbName_ProductStock;

        refreshTableStock(tableProduct, query);
    }

    public final static void refreshTableStock(JTable tableProduct, String query) {
        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQuery(conn, query); ResultSet rs = pst.executeQuery()) {
            DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                try {
                    String productID = rs.getString("product_ID");
                    String category = rs.getString("product_category");
                    String brand = rs.getString("product_brand");
                    String name = rs.getString("product_name");
                    String price = String.valueOf(rs.getFloat("product_price"));
                    String quantity = String.valueOf(rs.getInt("product_quantity"));
                    String totalPrice = String.valueOf(rs.getFloat("product_totalPrice"));
                    String deliveryDate = rs.getString("product_deliveryDate");

                    ImageIcon imageIcon = blobToImage(rs, "product_image");

                    int ID = rs.getInt("employee_ID");
                    String employeeName = getEmployeeNamebyID(ID);

                    model.addRow(new Object[]{
                        productID, brand, category, name, price, quantity, totalPrice, deliveryDate, imageIcon, employeeName
                    });

                } catch (SQLException e) {
                    e.printStackTrace(System.out);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(System.out);
        }
    }

    private static String getEmployeeNamebyID(int ID) {
        String employeeName = null;
        String query = "SELECT employee_name FROM " + Main.tbName_Employee + " WHERE employee_ID = ?";

        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, ID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                employeeName = rs.getString("employee_name");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(System.out);
        }

        return employeeName;
    }
}
