package project;

import com.mysql.cj.jdbc.Blob;
import java.awt.Image;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import static project.MainUtils.*;

public class TableUtils {

    public enum TableEnum {
        STOCK_DISTINCT,
        STOCK_DELIVERY,
        CATALOG_CATEGORY,
        CATALOG_PRODUCT,
    };

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

    public final static void refreshTableAll(JTable tableName, String tb_name, TableEnum tableEnum) {
        String query = "SELECT * FROM " + tb_name;

        refreshTable(tableName, query, tableEnum);
    }

    public final static void refreshTable(JTable tableName, String query, TableEnum tableEnum) {
        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQuery(conn, query); ResultSet rs = pst.executeQuery()) {
            DefaultTableModel model = (DefaultTableModel) tableName.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                try {
                    if (null != tableEnum) {
                        switch (tableEnum) {
                            case STOCK_DISTINCT: {
                                String category = rs.getString("product_category");
                                String brand = rs.getString("product_brand");
                                String name = rs.getString("product_name");
                                String quantity = String.valueOf(rs.getInt("product_quantity"));
                                String subQuery = "SELECT product_retail_price, product_image FROM " + Main.tbName_ProductItem + " WHERE product_name = ?";
                                try (PreparedStatement subPst = conn.prepareStatement(subQuery)) {
                                    subPst.setString(1, name);
                                    try (ResultSet subRs = subPst.executeQuery()) {
                                        if (subRs.next()) {
                                            String retailPrice = String.valueOf(subRs.getFloat("product_retail_price"));
                                            ImageIcon imageIcon = blobToImage(subRs, "product_image");

                                            model.addRow(new Object[]{
                                                category, brand, name, retailPrice, quantity, imageIcon
                                            });
                                        }
                                    }
                                }
                                break;
                            }
                            case STOCK_DELIVERY: {
                                String productID = rs.getString("product_ID");
                                String category = rs.getString("product_category");
                                String brand = rs.getString("product_brand");
                                String name = rs.getString("product_name");
                                String price = String.valueOf(rs.getFloat("product_price"));
                                String quantity = String.valueOf(rs.getInt("product_quantity"));
                                String totalPrice = String.valueOf(rs.getFloat("product_totalPrice"));
                                String deliveryDate = rs.getString("product_deliveryDate");
                                int ID = rs.getInt("employee_ID");
                                String employeeName = getEmployeeNamebyID(ID);
                                model.addRow(new Object[]{
                                    productID, category, brand, name, price, quantity, totalPrice, deliveryDate, employeeName
                                });
                                break;
                            }
                            case CATALOG_PRODUCT:
                                String product_ID = rs.getString("ID");
                                String product_category = rs.getString("product_category");
                                String product_brand = rs.getString("product_brand");
                                String product_name = rs.getString("product_name");
                                String product_retail_price = (String.valueOf(rs.getFloat("product_retail_price")));
                                ImageIcon imageIcon = blobToImage(rs, "product_image");
                                model.addRow(new Object[]{
                                    product_ID, product_category, product_brand, product_name, product_retail_price, imageIcon
                                });
                                break;
                            default:
                                break;
                        }
                    }
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            }

        } catch (SQLException e) {
            paneDatabaseError(e);
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
            paneDatabaseError(e);
        }

        return employeeName;
    }
}
