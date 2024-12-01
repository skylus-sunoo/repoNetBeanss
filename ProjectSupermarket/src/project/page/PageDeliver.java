package project.page;

import com.mysql.cj.jdbc.Blob;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import project.Main;
import static project.Main.tbName_Product;
import project.Queries;
import project.TableUtils;
import static project.TableUtils.refreshTableStock;
import static project.TableUtils.refreshTableStockAll;
import project.WindowUtils;
import project.search.*;
import project.swing.ImageRenderer;

/**
 *
 * @author Dric
 */
public class PageDeliver extends javax.swing.JPanel {

    String imgPath = null;

    // Pages
    private final SearchCategory SearchCategory = new SearchCategory();
    private final SearchDeliveryDateBetween SearchDeliveryDateBetween = new SearchDeliveryDateBetween();

    /**
     * Creates new form FormBody
     */
    public PageDeliver() {
        initComponents();
        WindowUtils.setTransparentFrame(fieldName);
        WindowUtils.setTransparentFrame(fieldPrice);
        WindowUtils.setTransparentFrame(fieldQuantity);
        WindowUtils.setTransparentFrame(fieldTotalPrice);
        WindowUtils.setTransparentFrame(fieldDOD);
        WindowUtils.setTransparentFrame(fieldDOE);
//        WindowUtils.setTransparentFrame(fieldImage);
        WindowUtils.setTransparentFrame(fieldDOE);

        setForm(SearchCategory);

        fieldName.getDocument().addDocumentListener(new PageDeliver.FieldChangeListener());
        fieldPrice.getDocument().addDocumentListener(new PageDeliver.FieldChangeListener());
        fieldQuantity.getDocument().addDocumentListener(new PageDeliver.FieldChangeListener());

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(175);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(200);
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableProduct.getColumnModel().getColumn(6).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(7).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(8).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(9).setPreferredWidth(124);
        tableProduct.getColumnModel().getColumn(9).setCellRenderer(new ImageRenderer());
        tableProduct.getColumnModel().getColumn(10).setPreferredWidth(150);
        tableProduct.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableProduct.isCellEditable(ERROR, WIDTH);
        TableUtils.refreshTableStockAll(tableProduct);

        tableProduct.setDefaultEditor(Object.class, null);
        tableProduct.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableProduct.getSelectedRow();
                if (selectedRow >= 0) {
                    selectTableStock(selectedRow);
                }
            }
        });
    }

    private void setForm(JComponent com) {
        panelSearch.removeAll();
        panelSearch.add(com);
        panelSearch.repaint();
        panelSearch.revalidate();

        if (com instanceof SearchCategory searchCategory) {
            searchCategory.repopulateComboBox();
        }
    }

    public void selectTableStock(int selectedRow) {
        String id = (String) tableProduct.getValueAt(selectedRow, 0);
        String category = (String) tableProduct.getValueAt(selectedRow, 1);
        String name = (String) tableProduct.getValueAt(selectedRow, 2);
        String price = (String) tableProduct.getValueAt(selectedRow, 3);
        String quantity = (String) tableProduct.getValueAt(selectedRow, 4);
        String uom = (String) tableProduct.getValueAt(selectedRow, 6);
        String dod = (String) tableProduct.getValueAt(selectedRow, 7);
        String doe = (String) tableProduct.getValueAt(selectedRow, 8);
        ImageIcon imgIcon = (ImageIcon) tableProduct.getValueAt(selectedRow, 9);

        fieldID.setText(id);
        comboCategory.setSelectedItem(category);
        fieldName.setText(name);
        fieldPrice.setText(price);
        fieldQuantity.setText(quantity);
        calculateTotalPrice();
        comboUOM.setSelectedItem(uom);
        fieldDOD.setText(dod);
        fieldDOE.setText(doe);

        fieldName.setForeground(new Color(0, 0, 0));
        fieldPrice.setForeground(new Color(0, 0, 0));
        fieldQuantity.setForeground(new Color(0, 0, 0));

        ImageIcon resizedIcon = ResizeImage(imgIcon);
        labelImgIcon.setIcon(resizedIcon);

        setUpdateDeleteEnable();
    }

    public void clearFields() {
        fieldID.setText("");
        setUpdateDeleteEnable();
        comboCategory.setSelectedIndex(0);
        fieldName.setText("Enter Product Name");
        fieldName.setForeground(new Color(153, 153, 153));
        fieldPrice.setText("Enter Price");
        fieldPrice.setForeground(new Color(153, 153, 153));
        fieldQuantity.setText("Enter Quantity");
        fieldQuantity.setForeground(new Color(153, 153, 153));
        fieldTotalPrice.setText("Total Price");
        fieldTotalPrice.setForeground(new Color(153, 153, 153));
        comboUOM.setSelectedIndex(0);
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        fieldDOD.setText(formattedDate);
        fieldDOE.setText(formattedDate);

        imgPath = null;
        labelImgIcon.setIcon(null);
    }

    public void addProduct() throws FileNotFoundException {
        if (project.Main.getUserSessionID() > 0) {
            String product_category = comboCategory.getSelectedItem().toString();
            String product_name = fieldName.getText().toUpperCase();
            String product_price = fieldPrice.getText();
            String product_quantity = fieldQuantity.getText();
            String product_uom = comboUOM.getSelectedItem().toString();
            String product_deliveryDate = fieldDOD.getText();
            String product_expirationDate = fieldDOE.getText();

            String employee_id = String.valueOf(project.Main.getUserSessionID());

            LocalDate date1 = LocalDate.parse(product_deliveryDate);
            LocalDate date2 = LocalDate.parse(product_expirationDate);
            boolean isValidExpiration = false;

            if (date1.isBefore(date2)) {
                isValidExpiration = true;
            }

            if (isValidExpiration) {
                String query = "INSERT INTO " + tbName_Product + " (product_category, product_name, product_price, product_quantity, product_uom, product_deliveryDate, product_expirationDate, product_image, employee_id)\n"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setString(1, product_category);
                    pst.setString(2, product_name);
                    pst.setString(3, product_price);
                    pst.setString(4, product_quantity);
                    pst.setString(5, product_uom);
                    pst.setString(6, product_deliveryDate);
                    pst.setString(7, product_expirationDate);

                    InputStream img = new FileInputStream(new File(imgPath));
                    pst.setBlob(8, img);

                    pst.setString(9, employee_id);
                    pst.executeUpdate();

                    clearFields();
                    TableUtils.refreshTableStockAll(tableProduct);

//                    Queries.executeUpdate(conn, query, product_category, product_name, product_price, product_quantity, product_uom, product_deliveryDate, product_expirationDate, employee_id);
                    JOptionPane.showMessageDialog(this, "Product Added!", "Success", JOptionPane.INFORMATION_MESSAGE);
//                    clearFields();
//                    TableUtils.refreshTableStock(tableProduct);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace(System.out);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Expiration Date must not be the same as or before the Delivery Date!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "This action requires logging in!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateProduct() {
        if (project.Main.getUserSessionID() > 0) {
            String path = imgPath;

            int id = Integer.parseInt(fieldID.getText());
            String product_category = comboCategory.getSelectedItem().toString();
            String product_name = fieldName.getText().toUpperCase();
            String product_price = fieldPrice.getText();
            String product_quantity = fieldQuantity.getText();
            String product_uom = comboUOM.getSelectedItem().toString();
            String product_deliveryDate = fieldDOD.getText();
            String product_expirationDate = fieldDOE.getText();

            String employee_id = String.valueOf(project.Main.getUserSessionID());

            LocalDate date1 = LocalDate.parse(product_deliveryDate);
            LocalDate date2 = LocalDate.parse(product_expirationDate);
            boolean isValidExpiration = false;

            if (date1.isBefore(date2)) {
                isValidExpiration = true;
            }

            if (isValidExpiration) {
                String query = "UPDATE " + tbName_Product + " SET product_category = ?, product_name = ?, product_price = ?, product_quantity = ?, product_uom = ?, product_deliveryDate = ?, product_expirationDate = ?, employee_id = ? WHERE product_ID = ?";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setString(1, product_category);
                    pst.setString(2, product_name);
                    pst.setString(3, product_price);
                    pst.setString(4, product_quantity);
                    pst.setString(5, product_uom);
                    pst.setString(6, product_deliveryDate);
                    pst.setString(7, product_expirationDate);
                    pst.setString(8, employee_id);
                    pst.setInt(9, id);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product Updated!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearFields();
                    TableUtils.refreshTableStockAll(tableProduct);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace(System.out);
                }

                if (path != null) {
                    try {
                        InputStream img = new FileInputStream(new File(path));
                        query = "UPDATE " + tbName_Product + " SET product_image = ? WHERE product_ID = ?";

                        try (Connection conn = Queries.getConnection(Main.dbName);) {
                            PreparedStatement pst = conn.prepareStatement(query);
                            pst.setBlob(1, img);
                            pst.setInt(2, id);
                            pst.executeUpdate();

                            clearFields();
                            TableUtils.refreshTableStockAll(tableProduct);
                        } catch (SQLException e) {
                            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace(System.out);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Expiration Date must not be the same as or before the Delivery Date!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "This action requires logging in!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteProduct() {
        if (project.Main.getUserSessionID() > 0) {
            int id = Integer.parseInt(fieldID.getText());
            String query = "DELETE FROM " + tbName_Product + " WHERE product_ID = ?";

            try (Connection conn = Queries.getConnection(Main.dbName);) {
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setInt(1, id);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Removed!", "Success", JOptionPane.INFORMATION_MESSAGE);

                clearFields();
                TableUtils.refreshTableStockAll(tableProduct);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(System.out);
            }
        } else {
            JOptionPane.showMessageDialog(this, "This action requires logging in!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setUpdateDeleteEnable() {
        if (fieldID.getText().equals("")) {
            btnUpdateProduct.setEnabled(false);
            btnDeleteProduct.setEnabled(false);
        } else {
            btnUpdateProduct.setEnabled(true);
            btnDeleteProduct.setEnabled(true);
        }
    }

    private class FieldChangeListener implements DocumentListener, ActionListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkFields();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkFields();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkFields();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            checkFields();
        }

        private void checkFields() {
            String product_name = fieldName.getText().trim();
            String product_price = fieldPrice.getText().trim();
            String product_quantity = fieldQuantity.getText().trim();

            boolean isValid = !product_name.isEmpty()
                    && !product_name.equals("Enter Product Name")
                    && !product_price.isEmpty()
                    && !product_price.equals("Enter Price")
                    && !product_quantity.isEmpty()
                    && !product_quantity.equals("Enter Quantity");
            btnAddProduct.setEnabled(isValid);
        }
    }

    private void calculateTotalPrice() {
        if (!fieldPrice.getText().equals("Enter Price") && !fieldQuantity.getText().equals("Enter Quantity")) {
            try {
                float price = Float.parseFloat(fieldPrice.getText());
                float quantity = Float.parseFloat(fieldQuantity.getText());
                float total = price * quantity;
                fieldTotalPrice.setText(String.valueOf(total));
            } catch (NumberFormatException e) {
                fieldTotalPrice.setText("Total Price");
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateDOD = new project.date.DateChooser();
        dateDOE = new project.date.DateChooser();
        fieldID = new javax.swing.JTextField();
        jProgressBar1 = new javax.swing.JProgressBar();
        panelBody = new project.component.ShadowPanel();
        panelMain = new javax.swing.JPanel();
        comboCategory = new javax.swing.JComboBox<>();
        separatorCategory = new javax.swing.JSeparator();
        labelName = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        separatorName = new javax.swing.JSeparator();
        labelCategory = new javax.swing.JLabel();
        labelPrice = new javax.swing.JLabel();
        fieldPrice = new javax.swing.JTextField();
        separatorPrice = new javax.swing.JSeparator();
        labelQuantity = new javax.swing.JLabel();
        fieldQuantity = new javax.swing.JTextField();
        separatorQuantity = new javax.swing.JSeparator();
        labelTotalPrice = new javax.swing.JLabel();
        fieldTotalPrice = new javax.swing.JTextField();
        separatorTotalPrice = new javax.swing.JSeparator();
        labelTotalPriceUOM = new javax.swing.JLabel();
        comboUOM = new javax.swing.JComboBox<>();
        separatorPriceUOM = new javax.swing.JSeparator();
        labelDOD = new javax.swing.JLabel();
        fieldDOD = new javax.swing.JTextField();
        btnDOD = new javax.swing.JButton();
        separatorDOD = new javax.swing.JSeparator();
        labelDOE = new javax.swing.JLabel();
        fieldDOE = new javax.swing.JTextField();
        separatorDOE = new javax.swing.JSeparator();
        btnDOE = new javax.swing.JButton();
        btnImage = new javax.swing.JButton();
        labelImgIcon = new javax.swing.JLabel();
        scrollProduct = new javax.swing.JScrollPane();
        tableProduct = new project.swing.Table();
        panelButtons = new javax.swing.JPanel();
        btnAddProduct = new javax.swing.JButton();
        btnUpdateProduct = new javax.swing.JButton();
        btnDeleteProduct = new javax.swing.JButton();
        btnClearProduct = new javax.swing.JButton();
        labelSearch = new javax.swing.JLabel();
        comboSearch = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        panelSearch = new javax.swing.JPanel();

        dateDOD.setDateFormat("yyyy-MM-dd");
        dateDOD.setTextRefernce(fieldDOD);

        dateDOE.setDateFormat("yyyy-MM-dd");
        dateDOE.setTextRefernce(fieldDOE);

        fieldID.setText("jTextField1");

        setMaximumSize(new java.awt.Dimension(915, 544));
        setMinimumSize(new java.awt.Dimension(915, 544));

        panelBody.setMaximumSize(new java.awt.Dimension(915, 544));
        panelBody.setMinimumSize(new java.awt.Dimension(915, 544));
        panelBody.setPreferredSize(new java.awt.Dimension(915, 544));

        panelMain.setOpaque(false);

        comboCategory.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BEVERAGES", "BREADS AND BAKERY", "CANNED AND JARRED", "DAIRY", "DRY GOODS AND BAKING", "FRESH PRODUCE", "FROZEN", "HEALTH AND WELLNESS", "HOUSEHOLD", "MEAT", "PERSONAL CARE", "PET CARE", "SNACKS" }));

        labelName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelName.setText("Name");
        labelName.setMaximumSize(new java.awt.Dimension(67, 20));
        labelName.setMinimumSize(new java.awt.Dimension(67, 20));
        labelName.setPreferredSize(new java.awt.Dimension(67, 20));

        fieldName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldName.setForeground(new java.awt.Color(153, 153, 153));
        fieldName.setText("Enter Product Name");
        fieldName.setBorder(null);
        fieldName.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldNameFocusLost(evt);
            }
        });

        labelCategory.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelCategory.setText("Category");

        labelPrice.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelPrice.setText("Price");

        fieldPrice.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldPrice.setForeground(new java.awt.Color(153, 153, 153));
        fieldPrice.setText("Enter Price");
        fieldPrice.setBorder(null);
        fieldPrice.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldPrice.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldPriceFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldPriceFocusLost(evt);
            }
        });
        fieldPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fieldPriceKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldPriceKeyTyped(evt);
            }
        });

        labelQuantity.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelQuantity.setText("Quantity");
        labelQuantity.setMaximumSize(new java.awt.Dimension(67, 20));
        labelQuantity.setMinimumSize(new java.awt.Dimension(67, 20));
        labelQuantity.setPreferredSize(new java.awt.Dimension(67, 20));

        fieldQuantity.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldQuantity.setForeground(new java.awt.Color(153, 153, 153));
        fieldQuantity.setText("Enter Quantity");
        fieldQuantity.setBorder(null);
        fieldQuantity.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldQuantity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldQuantityFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldQuantityFocusLost(evt);
            }
        });
        fieldQuantity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fieldQuantityKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldQuantityKeyTyped(evt);
            }
        });

        labelTotalPrice.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelTotalPrice.setText("Total Price");

        fieldTotalPrice.setEditable(false);
        fieldTotalPrice.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldTotalPrice.setForeground(new java.awt.Color(153, 153, 153));
        fieldTotalPrice.setText("Total Price");
        fieldTotalPrice.setBorder(null);
        fieldTotalPrice.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        fieldTotalPrice.setFocusable(false);

        labelTotalPriceUOM.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelTotalPriceUOM.setText("Unit of Measure");

        comboUOM.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboUOM.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PIECES", "LITERS", "KILOGRAMS" }));

        labelDOD.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelDOD.setText("Date of Delivery");

        fieldDOD.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldDOD.setBorder(null);
        fieldDOD.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldDOD.setFocusable(false);
        fieldDOD.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldDODFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldDODFocusLost(evt);
            }
        });

        btnDOD.setText("...");
        btnDOD.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDOD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDODActionPerformed(evt);
            }
        });

        labelDOE.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelDOE.setText("Date of Expiry");

        fieldDOE.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldDOE.setBorder(null);
        fieldDOE.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldDOE.setFocusable(false);
        fieldDOE.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldDOEFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldDOEFocusLost(evt);
            }
        });

        btnDOE.setText("...");
        btnDOE.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDOE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDOEActionPerformed(evt);
            }
        });

        btnImage.setText("Select Image");
        btnImage.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addGap(167, 167, 167)
                                .addComponent(separatorName))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(100, 100, 100)
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(comboCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fieldName))))
                        .addGap(528, 528, 528))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(separatorCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(labelQuantity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelTotalPrice, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelPrice, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelTotalPriceUOM, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelDOE, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(separatorDOD)
                                    .addComponent(separatorPriceUOM)
                                    .addComponent(comboUOM, 0, 177, Short.MAX_VALUE)
                                    .addComponent(separatorTotalPrice)
                                    .addComponent(fieldTotalPrice)
                                    .addComponent(separatorQuantity)
                                    .addComponent(separatorPrice)
                                    .addComponent(fieldPrice)
                                    .addComponent(fieldQuantity)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                                        .addComponent(fieldDOD)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDOD))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                                        .addComponent(fieldDOE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDOE))
                                    .addComponent(separatorDOE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(btnImage, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(labelImgIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(separatorCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(separatorName, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fieldTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTotalPriceUOM, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboUOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorPriceUOM, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDOD))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldDOE, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDOE)
                    .addComponent(labelDOE, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(separatorDOE, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(btnImage))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelImgIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        scrollProduct.setBorder(null);

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Category", "Name", "Price", "Quantity", "Total Price", "Unit of Measure", "Delivery Date", "Expiry Date", "Image", "Employee"
            }
        ));
        tableProduct.setRowHeight(124);
        scrollProduct.setViewportView(tableProduct);
        if (tableProduct.getColumnModel().getColumnCount() > 0) {
            tableProduct.getColumnModel().getColumn(0).setHeaderValue("ID");
            tableProduct.getColumnModel().getColumn(1).setHeaderValue("Category");
            tableProduct.getColumnModel().getColumn(2).setHeaderValue("Name");
            tableProduct.getColumnModel().getColumn(3).setHeaderValue("Price");
            tableProduct.getColumnModel().getColumn(4).setHeaderValue("Quantity");
            tableProduct.getColumnModel().getColumn(5).setHeaderValue("Total Price");
            tableProduct.getColumnModel().getColumn(6).setHeaderValue("Unit of Measure");
            tableProduct.getColumnModel().getColumn(7).setHeaderValue("Delivery Date");
            tableProduct.getColumnModel().getColumn(8).setHeaderValue("Expiry Date");
            tableProduct.getColumnModel().getColumn(9).setHeaderValue("Image");
            tableProduct.getColumnModel().getColumn(10).setHeaderValue("Employee");
        }

        panelButtons.setOpaque(false);

        btnAddProduct.setText("Add");
        btnAddProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddProduct.setEnabled(false);
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        btnUpdateProduct.setText("Update");
        btnUpdateProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdateProduct.setEnabled(false);
        btnUpdateProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateProductActionPerformed(evt);
            }
        });

        btnDeleteProduct.setText("Delete");
        btnDeleteProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDeleteProduct.setEnabled(false);
        btnDeleteProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteProductActionPerformed(evt);
            }
        });

        btnClearProduct.setText("Clear");
        btnClearProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClearProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearProductActionPerformed(evt);
            }
        });

        labelSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelSearch.setText("Search for Products");

        comboSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Everything", "Under a Category", "Delivered after a date", "Delivered before a date", "Delivered between two dates", "Expired after a date", "Expired before a date", "Expired between two dates", "Delivered and Expired between two dates" }));
        comboSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelButtonsLayout.createSequentialGroup()
                        .addComponent(btnAddProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClearProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdateProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDeleteProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelButtonsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddProduct)
                    .addComponent(btnClearProduct)
                    .addComponent(btnUpdateProduct)
                    .addComponent(btnDeleteProduct))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)))
        );

        panelSearch.setMaximumSize(new java.awt.Dimension(520, 35));
        panelSearch.setMinimumSize(new java.awt.Dimension(520, 35));
        panelSearch.setOpaque(false);
        panelSearch.setPreferredSize(new java.awt.Dimension(520, 35));
        panelSearch.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollProduct)
                    .addComponent(panelSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelBodyLayout.createSequentialGroup()
                        .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 17, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fieldNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldNameFocusGained
        if (fieldName.getText().equals("Enter Product Name")) {
            fieldName.setText("");
            fieldName.setForeground(new Color(0, 0, 0));
        }
    }//GEN-LAST:event_fieldNameFocusGained

    private void fieldNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldNameFocusLost
        if (fieldName.getText().equals("")) {
            fieldName.setText("Enter Product Name");
            fieldName.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldNameFocusLost

    private void fieldPriceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPriceFocusGained
        if (fieldPrice.getText().equals("Enter Price")) {
            fieldPrice.setText("");
            fieldPrice.setForeground(new Color(0, 0, 0));
        }
    }//GEN-LAST:event_fieldPriceFocusGained

    private void fieldPriceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPriceFocusLost
        if (fieldPrice.getText().equals("")) {
            fieldPrice.setText("Enter Price");
            fieldPrice.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldPriceFocusLost

    private void fieldQuantityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldQuantityFocusGained
        if (fieldQuantity.getText().equals("Enter Quantity")) {
            fieldQuantity.setText("");
            fieldQuantity.setForeground(new Color(0, 0, 0));
        }
    }//GEN-LAST:event_fieldQuantityFocusGained

    private void fieldQuantityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldQuantityFocusLost
        if (fieldQuantity.getText().equals("")) {
            fieldQuantity.setText("Enter Quantity");
            fieldQuantity.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldQuantityFocusLost

    private void fieldDODFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldDODFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldDODFocusGained

    private void fieldDODFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldDODFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldDODFocusLost

    private void fieldDOEFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldDOEFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldDOEFocusGained

    private void fieldDOEFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldDOEFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldDOEFocusLost

    private void btnDODActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDODActionPerformed
        dateDOD.showPopup();
    }//GEN-LAST:event_btnDODActionPerformed

    private void btnDOEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDOEActionPerformed
        dateDOE.showPopup();
    }//GEN-LAST:event_btnDOEActionPerformed

    private void fieldPriceKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldPriceKeyTyped
        char c = evt.getKeyChar();

        if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldPriceKeyTyped

    private void fieldPriceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldPriceKeyReleased
        calculateTotalPrice();
    }//GEN-LAST:event_fieldPriceKeyReleased

    private void fieldQuantityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldQuantityKeyReleased
        calculateTotalPrice();
    }//GEN-LAST:event_fieldQuantityKeyReleased

    private void fieldQuantityKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldQuantityKeyTyped
        char c = evt.getKeyChar();

        if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldQuantityKeyTyped

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        if (imgPath != null) {
            try {
                addProduct();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PageDeliver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Missing Image!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnUpdateProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateProductActionPerformed
        updateProduct();
    }//GEN-LAST:event_btnUpdateProductActionPerformed

    private void btnDeleteProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProductActionPerformed
        deleteProduct();
    }//GEN-LAST:event_btnDeleteProductActionPerformed

    private void btnClearProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearProductActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearProductActionPerformed

    private void btnImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImageActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home") + "/Downloads"));
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.images", "jpg", "png");
        fc.addChoosableFileFilter(filter);
        int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            imgPath = selectedFile.getAbsolutePath();
            labelImgIcon.setIcon(ResizeImage(imgPath, null));
        }
    }//GEN-LAST:event_btnImageActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String startDateString;
        String endDateString;
        switch (String.valueOf(comboSearch.getSelectedItem())) {
            case "Everything":
                refreshTableStockAll(tableProduct);
                break;
            case "Under a Category":
                String selectedCategory = SearchCategory.getSelectedCategory();
                refreshTableStock(tableProduct, "SELECT * FROM " + tbName_Product + " WHERE product_category = '" + selectedCategory + "'");
                break;
            case "Delivered between two dates":
                startDateString = SearchDeliveryDateBetween.getFieldSearchDateStart().getText();
                endDateString = SearchDeliveryDateBetween.getFieldSearchDateEnd().getText();

                LocalDate startDate = LocalDate.parse(startDateString);
                LocalDate endDate = LocalDate.parse(endDateString);

                if (startDate.isBefore(endDate) || startDate.equals(endDate)) {
                    String query = "SELECT * FROM " + tbName_Product + " WHERE product_deliveryDate BETWEEN '"
                            + startDate + "' AND '" + endDate + "'";
                    refreshTableStock(tableProduct, query);
                } else {
                    JOptionPane.showMessageDialog(this, "Start Date must come before the End Date!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void comboSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSearchActionPerformed
        switch (String.valueOf(comboSearch.getSelectedItem())) {
            case "Everything":
//                setForm(SearchCategory);
                break;
            case "Under a Category":
                setForm(SearchCategory);
                break;
            case "Delivered between two dates":
                setForm(SearchDeliveryDateBetween);
                break;
            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_comboSearchActionPerformed

    public ImageIcon ResizeImage(String imgPath, byte[] pic) {
        ImageIcon resizeImg;

        if (imgPath != null) {
            resizeImg = new ImageIcon(imgPath);
        } else {
            resizeImg = new ImageIcon(pic);
        }

        Image finalImg = resizeImg.getImage().getScaledInstance(labelImgIcon.getWidth(), labelImgIcon.getHeight(), Image.SCALE_SMOOTH);

        return new ImageIcon(finalImg);
    }

    public ImageIcon ResizeImage(ImageIcon imgIcon) {
        // Get the image from the ImageIcon
        Image img = imgIcon.getImage();

        // Resize the image to fit the label dimensions
        Image finalImg = img.getScaledInstance(labelImgIcon.getWidth(), labelImgIcon.getHeight(), Image.SCALE_SMOOTH);

        // Return the resized ImageIcon
        return new ImageIcon(finalImg);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClearProduct;
    private javax.swing.JButton btnDOD;
    private javax.swing.JButton btnDOE;
    private javax.swing.JButton btnDeleteProduct;
    private javax.swing.JButton btnImage;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdateProduct;
    private javax.swing.JComboBox<String> comboCategory;
    private javax.swing.JComboBox<String> comboSearch;
    private javax.swing.JComboBox<String> comboUOM;
    private project.date.DateChooser dateDOD;
    private project.date.DateChooser dateDOE;
    private javax.swing.JTextField fieldDOD;
    private javax.swing.JTextField fieldDOE;
    private javax.swing.JTextField fieldID;
    private javax.swing.JTextField fieldName;
    private javax.swing.JTextField fieldPrice;
    private javax.swing.JTextField fieldQuantity;
    private javax.swing.JTextField fieldTotalPrice;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel labelCategory;
    private javax.swing.JLabel labelDOD;
    private javax.swing.JLabel labelDOE;
    private javax.swing.JLabel labelImgIcon;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelPrice;
    private javax.swing.JLabel labelQuantity;
    private javax.swing.JLabel labelSearch;
    private javax.swing.JLabel labelTotalPrice;
    private javax.swing.JLabel labelTotalPriceUOM;
    private project.component.ShadowPanel panelBody;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JScrollPane scrollProduct;
    private javax.swing.JSeparator separatorCategory;
    private javax.swing.JSeparator separatorDOD;
    private javax.swing.JSeparator separatorDOE;
    private javax.swing.JSeparator separatorName;
    private javax.swing.JSeparator separatorPrice;
    private javax.swing.JSeparator separatorPriceUOM;
    private javax.swing.JSeparator separatorQuantity;
    private javax.swing.JSeparator separatorTotalPrice;
    private project.swing.Table tableProduct;
    // End of variables declaration//GEN-END:variables
}
