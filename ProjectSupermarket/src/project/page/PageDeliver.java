package project.page;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import project.Main;
import project.Queries;
import project.TableUtils;
import static project.TableUtils.refreshTable;
import static project.MainUtils.*;
import project.WindowUtils;
import project.search.*;
import project.search.SearchComboBox.EnumComboBox;
import project.search.SearchComboBoxTwo.EnumComboBoxTwo;

/**
 *
 * @author Dric
 */
public class PageDeliver extends javax.swing.JPanel {

    public String currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock;

    // Pages
    private final SearchEmpty SearchEmpty = new SearchEmpty();
    private final SearchComboBox SearchComboBox = new SearchComboBox();
    private final SearchComboBoxTwo SearchComboBoxTwo = new SearchComboBoxTwo();
    private final SearchDeliveryDateSingle SearchDeliveryDateSingle = new SearchDeliveryDateSingle();
    private final SearchDeliveryDateBetween SearchDeliveryDateBetween = new SearchDeliveryDateBetween();

    /**
     * Creates new form FormBody
     */
    public PageDeliver() {
        initComponents();
        WindowUtils.setTransparentFrame(fieldName, fieldPrice, fieldQuantity, fieldTotalPrice, fieldDOD);

        setForm(SearchEmpty);

        fieldName.getDocument().addDocumentListener(new PageDeliver.FieldChangeListener());
        fieldPrice.getDocument().addDocumentListener(new PageDeliver.FieldChangeListener());
        fieldQuantity.getDocument().addDocumentListener(new PageDeliver.FieldChangeListener());

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(125);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(125);
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(200);
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableProduct.getColumnModel().getColumn(5).setPreferredWidth(80);
        tableProduct.getColumnModel().getColumn(6).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(7).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(8).setPreferredWidth(120);
        tableProduct.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableProduct.isCellEditable(ERROR, WIDTH);
        TableUtils.refreshTableAll(tableProduct, Main.tbName_ProductStock, TableUtils.TableEnum.STOCK_DELIVERY);

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

        switch (com) {
            case SearchComboBox search:
                search.repopulateComboBox(search.selectedSearch);
                break;
            case SearchComboBoxTwo search:
                search.repopulateComboBox(search.selectedSearch);
                break;
            default:
                break;
        }
    }

    public void refreshTableProduct() {
        refreshTable(tableProduct, currentSearchQuery, TableUtils.TableEnum.STOCK_DELIVERY);
    }

    public void selectTableStock(int selectedRow) {
        String id = (String) tableProduct.getValueAt(selectedRow, 0);
        String category = (String) tableProduct.getValueAt(selectedRow, 1);
        String brand = (String) tableProduct.getValueAt(selectedRow, 2);
        String name = (String) tableProduct.getValueAt(selectedRow, 3);
        String price = (String) tableProduct.getValueAt(selectedRow, 4);
        String quantity = (String) tableProduct.getValueAt(selectedRow, 5);
        String[] parts = quantity.split("/");
        String remaining_quantity = parts[0];
        quantity = remaining_quantity;
        String dod = (String) tableProduct.getValueAt(selectedRow, 7);

        fieldID.setText(id);
        comboCategory.setSelectedItem(category);
        comboBrand.setSelectedItem(brand);
        comboName.setSelectedItem(name);
        fieldPrice.setText(price);
        fieldQuantity.setText(quantity);
        calculateTotalPrice();
        fieldDOD.setText(dod);

        fieldName.setForeground(new Color(0, 0, 0));
        fieldPrice.setForeground(new Color(0, 0, 0));
        fieldQuantity.setForeground(new Color(0, 0, 0));

        setUpdateDeleteEnable();
    }

    public void clearFields() {
        fieldID.setText("");
        setUpdateDeleteEnable();
        comboCategory.setSelectedIndex(0);
        comboBrand.setSelectedIndex(0);
        fieldName.setText("Enter Product Name");
        fieldName.setForeground(new Color(153, 153, 153));
        fieldPrice.setText("Enter Price");
        fieldPrice.setForeground(new Color(153, 153, 153));
        fieldQuantity.setText("Enter Quantity");
        fieldQuantity.setForeground(new Color(153, 153, 153));
        fieldTotalPrice.setText("Total Price");
        fieldTotalPrice.setForeground(new Color(153, 153, 153));
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        fieldDOD.setText(formattedDate);
    }

    public void addProduct() throws FileNotFoundException {
        if (isLoggedIn()) {
            String product_category = comboCategory.getSelectedItem().toString();
            String product_brand = comboBrand.getSelectedItem().toString();
            String product_name = comboName.getSelectedItem().toString();
            String product_price = fieldPrice.getText();
            String product_quantity = fieldQuantity.getText();
            String product_deliveryDate = fieldDOD.getText();

            String employee_id = String.valueOf(project.Main.getUserSessionID());

            String query = "INSERT INTO " + Main.tbName_ProductStock + " (product_category, product_brand, product_name, product_price, product_remaining_quantity, product_quantity, product_deliveryDate, employee_id)\n"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = Queries.getConnection(Main.dbName);) {
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, product_category);
                pst.setString(2, product_brand);
                pst.setString(3, product_name);
                pst.setString(4, product_price);
                pst.setString(5, product_quantity); // remaining qty
                pst.setString(6, product_quantity);
                pst.setString(7, product_deliveryDate);
                pst.setString(8, employee_id);
                pst.executeUpdate();

                clearFields();
                refreshTableProduct();

                JOptionPane.showMessageDialog(this, "Product Added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                paneDatabaseError(e);
            }
        } else {
            paneNotLoggedIn();
        }
    }

    public void updateProduct() {
        if (isLoggedIn()) {
            int warnUser = JOptionPane.showConfirmDialog(
                    null,
                    "Confirm Update?",
                    "Warning: Update",
                    JOptionPane.YES_NO_OPTION
            );

            if (warnUser == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(fieldID.getText());
                String product_category = comboCategory.getSelectedItem().toString();
                String product_brand = comboBrand.getSelectedItem().toString();
                String product_name = comboName.getSelectedItem().toString();
                String product_price = fieldPrice.getText();
                String product_quantity = fieldQuantity.getText();
                String product_deliveryDate = fieldDOD.getText();

                String employee_id = String.valueOf(project.Main.getUserSessionID());

                String query = "UPDATE " + Main.tbName_ProductStock + " SET product_category = ?, product_brand = ?, product_name = ?, product_price = ?, product_remaining_quantity = ?, product_deliveryDate = ?, employee_id = ? WHERE product_ID = ?";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setString(1, product_category);
                    pst.setString(2, product_brand);
                    pst.setString(3, product_name);
                    pst.setString(4, product_price);
                    pst.setString(5, product_quantity);
                    pst.setString(6, product_deliveryDate);
                    pst.setString(7, employee_id);
                    pst.setInt(8, id);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product Updated!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearFields();
                    refreshTableProduct();
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            }
        } else {
            paneNotLoggedIn();
        }
    }

    public void deleteProduct() {
        if (isLoggedIn()) {
            int warnUser = JOptionPane.showConfirmDialog(
                    null,
                    "Confirm Delete?",
                    "Warning: Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (warnUser == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(fieldID.getText());
                String query = "DELETE FROM " + Main.tbName_ProductStock + " WHERE product_ID = ?";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setInt(1, id);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product Removed!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearFields();
                    refreshTableProduct();
                    Queries.resetPrimaryKey(Main.tbName_ProductStock);
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            }
        } else {
            paneNotLoggedIn();
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
            String product_price = fieldPrice.getText().trim();
            String product_quantity = fieldQuantity.getText().trim();
            Object selected_product = comboName.getSelectedItem();

            boolean isValid = selected_product != null
                    && !selected_product.toString().isEmpty()
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

    public void repopulateCategoryComboBox() {
        Queries.repopulateComboBox(comboCategory, "product_category", "SELECT DISTINCT product_category FROM " + Main.tbName_ProductCategory);
    }

    public String getSelectedCategory() {
        return (String) comboCategory.getSelectedItem();
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
        fieldID = new javax.swing.JTextField();
        jProgressBar1 = new javax.swing.JProgressBar();
        fieldName = new javax.swing.JTextField();
        panelBody = new project.component.ShadowPanel();
        panelMain = new javax.swing.JPanel();
        comboCategory = new javax.swing.JComboBox<>();
        separatorCategory = new javax.swing.JSeparator();
        labelName = new javax.swing.JLabel();
        comboName = new javax.swing.JComboBox<>();
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
        labelBrand = new javax.swing.JLabel();
        comboBrand = new javax.swing.JComboBox<>();
        separatorBrand = new javax.swing.JSeparator();
        labelDOD = new javax.swing.JLabel();
        fieldDOD = new javax.swing.JTextField();
        btnDOD = new javax.swing.JButton();
        separatorDOD = new javax.swing.JSeparator();
        panelButtons = new javax.swing.JPanel();
        btnAddProduct = new javax.swing.JButton();
        btnUpdateProduct = new javax.swing.JButton();
        btnClearProduct = new javax.swing.JButton();
        btnDeleteProduct = new javax.swing.JButton();
        panelSearch = new javax.swing.JPanel();
        labelSearch = new javax.swing.JLabel();
        comboSearch = new javax.swing.JComboBox<>();
        scrollProduct = new javax.swing.JScrollPane();
        tableProduct = new project.swing.Table();
        btnSearch = new javax.swing.JButton();

        dateDOD.setDateFormat("yyyy-MM-dd");
        dateDOD.setTextRefernce(fieldDOD);

        fieldID.setText("jTextField1");

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

        setMaximumSize(new java.awt.Dimension(1389, 844));
        setMinimumSize(new java.awt.Dimension(1389, 844));

        panelBody.setMaximumSize(new java.awt.Dimension(1389, 844));
        panelBody.setMinimumSize(new java.awt.Dimension(1389, 844));
        panelBody.setPreferredSize(new java.awt.Dimension(1389, 844));

        panelMain.setOpaque(false);

        comboCategory.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BEVERAGES", "BREADS AND BAKERY", "CANNED AND JARRED", "DAIRY", "DRY GOODS AND BAKING", "FRESH PRODUCE", "FROZEN", "HEALTH AND WELLNESS", "HOUSEHOLD", "MEAT", "PERSONAL CARE", "PET CARE", "SNACKS" }));
        comboCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboCategoryActionPerformed(evt);
            }
        });

        labelName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelName.setText("Name");
        labelName.setMaximumSize(new java.awt.Dimension(67, 20));
        labelName.setMinimumSize(new java.awt.Dimension(67, 20));
        labelName.setPreferredSize(new java.awt.Dimension(67, 20));

        comboName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboName.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1" }));

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

        labelBrand.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelBrand.setText("Brand");

        comboBrand.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboBrand.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PIECES", "LITERS", "KILOGRAMS" }));
        comboBrand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBrandActionPerformed(evt);
            }
        });

        labelDOD.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelDOD.setText("Date of Delivery");

        fieldDOD.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldDOD.setBorder(null);
        fieldDOD.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldDOD.setFocusable(false);

        btnDOD.setText("...");
        btnDOD.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDOD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDODActionPerformed(evt);
            }
        });

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

        btnClearProduct.setText("Clear");
        btnClearProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClearProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearProductActionPerformed(evt);
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

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnUpdateProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(btnAddProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClearProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(btnDeleteProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddProduct)
                    .addComponent(btnClearProduct))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpdateProduct)
                    .addComponent(btnDeleteProduct))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorName, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(fieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(fieldQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(fieldTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDOD))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(separatorDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(labelCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(labelBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBrand, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5)
                .addComponent(separatorCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(comboBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(separatorBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(comboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(separatorName, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(fieldPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(separatorPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(fieldQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(separatorQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(separatorTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fieldDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDOD))))
                .addGap(6, 6, 6)
                .addComponent(separatorDOD, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64)
                .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(380, 380, 380))
        );

        panelSearch.setMaximumSize(new java.awt.Dimension(520, 35));
        panelSearch.setMinimumSize(new java.awt.Dimension(520, 35));
        panelSearch.setOpaque(false);
        panelSearch.setPreferredSize(new java.awt.Dimension(520, 35));
        panelSearch.setLayout(new java.awt.BorderLayout());

        labelSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelSearch.setText("Search for Products");

        comboSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Everything", "Under a Category", "Under a Brand", "Under a Category and Brand", "Delivered after a date", "Delivered before a date", "Delivered between two dates" }));
        comboSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSearchActionPerformed(evt);
            }
        });

        scrollProduct.setBorder(null);

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Category", "Brand", "Name", "Price", "Quantity", "Total Price", "Delivery Date", "Employee"
            }
        ));
        tableProduct.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        scrollProduct.setViewportView(tableProduct);

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelBodyLayout.createSequentialGroup()
                        .addComponent(labelSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(95, 95, 95)
                        .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSearch))
                    .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 991, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBodyLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelBodyLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSearch))
                        .addGap(12, 12, 12)
                        .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
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
        WindowUtils.setDefaultField(fieldName, "Enter Product Name", WindowUtils.FieldFocus.GAINED, Color.BLACK);
    }//GEN-LAST:event_fieldNameFocusGained

    private void fieldNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldNameFocusLost
        WindowUtils.setDefaultField(fieldName, "Enter Product Name", WindowUtils.FieldFocus.LOST, Color.BLACK);
    }//GEN-LAST:event_fieldNameFocusLost

    private void fieldPriceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPriceFocusGained
        WindowUtils.setDefaultField(fieldPrice, "Enter Price", WindowUtils.FieldFocus.GAINED, Color.BLACK);
    }//GEN-LAST:event_fieldPriceFocusGained

    private void fieldPriceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPriceFocusLost
        WindowUtils.setDefaultField(fieldPrice, "Enter Price", WindowUtils.FieldFocus.LOST, Color.BLACK);
    }//GEN-LAST:event_fieldPriceFocusLost

    private void fieldQuantityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldQuantityFocusGained
        WindowUtils.setDefaultField(fieldQuantity, "Enter Quantity", WindowUtils.FieldFocus.GAINED, Color.BLACK);
    }//GEN-LAST:event_fieldQuantityFocusGained

    private void fieldQuantityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldQuantityFocusLost
        WindowUtils.setDefaultField(fieldQuantity, "Enter Quantity", WindowUtils.FieldFocus.LOST, Color.BLACK);
    }//GEN-LAST:event_fieldQuantityFocusLost

    private void btnDODActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDODActionPerformed
        dateDOD.showPopup();
    }//GEN-LAST:event_btnDODActionPerformed

    private void fieldPriceKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldPriceKeyTyped
        WindowUtils.enforceDigits(evt);
    }//GEN-LAST:event_fieldPriceKeyTyped

    private void fieldPriceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldPriceKeyReleased
        calculateTotalPrice();
    }//GEN-LAST:event_fieldPriceKeyReleased

    private void fieldQuantityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldQuantityKeyReleased
        calculateTotalPrice();
    }//GEN-LAST:event_fieldQuantityKeyReleased

    private void fieldQuantityKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldQuantityKeyTyped
        WindowUtils.enforceDigits(evt);
    }//GEN-LAST:event_fieldQuantityKeyTyped

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        try {
            addProduct();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageDeliver.class.getName()).log(Level.SEVERE, null, ex);
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
        tableProduct.getSelectionModel().clearSelection();
    }//GEN-LAST:event_btnClearProductActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String selectedComboBox;
        String selectedComboBox2;
        String startDateString;
        String endDateString;
        LocalDate startDate;
        LocalDate endDate;
        switch (String.valueOf(comboSearch.getSelectedItem())) {
            case "Everything":
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock;
                refreshTableProduct();
                break;
            case "Under a Category":
                selectedComboBox = SearchComboBox.getSelectedComboBox();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock + " WHERE product_category = '" + selectedComboBox + "'";
                refreshTableProduct();
                break;
            case "Under a Brand":
                selectedComboBox2 = SearchComboBox.getSelectedComboBox();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock + " WHERE product_brand = '" + selectedComboBox2 + "'";
                refreshTableProduct();
                break;
            case "Under a Category and Brand":
                selectedComboBox = SearchComboBoxTwo.getSelectedComboBox1();
                selectedComboBox2 = SearchComboBoxTwo.getSelectedComboBox2();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock + " WHERE product_category = '" + selectedComboBox + "' AND product_brand = '" + selectedComboBox2 + "'";
                refreshTableProduct();
                break;
            case "Delivered after a date":
                startDateString = SearchDeliveryDateSingle.getFieldSearchDateStart().getText();
                startDate = LocalDate.parse(startDateString);
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock + " WHERE product_deliveryDate >= '" + startDate + "'";
                refreshTableProduct();
                break;
            case "Delivered before a date":
                startDateString = SearchDeliveryDateSingle.getFieldSearchDateStart().getText();
                startDate = LocalDate.parse(startDateString);
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock + " WHERE product_deliveryDate <= '" + startDate + "'";
                refreshTableProduct();
                break;
            case "Delivered between two dates":
                startDateString = SearchDeliveryDateBetween.getFieldSearchDateStart().getText();
                endDateString = SearchDeliveryDateBetween.getFieldSearchDateEnd().getText();

                startDate = LocalDate.parse(startDateString);
                endDate = LocalDate.parse(endDateString);

                if (startDate.isBefore(endDate) || startDate.equals(endDate)) {
                    currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductStock + " WHERE product_deliveryDate BETWEEN '"
                            + startDate + "' AND '" + endDate + "'";
                    refreshTableProduct();
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
                setForm(SearchEmpty);
                break;
            case "Under a Category":
                SearchComboBox.selectedSearch = EnumComboBox.CATEGORY_DELIVERY;
                setForm(SearchComboBox);
                break;
            case "Under a Brand":
                SearchComboBox.selectedSearch = EnumComboBox.BRAND_DELIVERY;
                setForm(SearchComboBox);
                break;
            case "Under a Category and Brand":
                SearchComboBoxTwo.selectedSearch = EnumComboBoxTwo.CATEGORY_BRAND_DELIVERY;
                setForm(SearchComboBoxTwo);
                break;
            case "Delivered after a date":
            case "Delivered before a date":
                setForm(SearchDeliveryDateSingle);
                break;
            case "Delivered between two dates":
                setForm(SearchDeliveryDateBetween);
                break;
            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_comboSearchActionPerformed

    private void comboCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboCategoryActionPerformed
        Queries.repopulateComboBox(comboBrand, "brand_name", "SELECT brand_name FROM " + Main.tbName_CategoryBrands + " WHERE product_category = '" + comboCategory.getSelectedItem() + "'");
    }//GEN-LAST:event_comboCategoryActionPerformed

    private void comboBrandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBrandActionPerformed
        Queries.repopulateComboBox(comboName, "product_name", "SELECT product_name FROM " + Main.tbName_ProductItem + " WHERE product_category = '" + comboCategory.getSelectedItem() + "' AND product_brand = '" + comboBrand.getSelectedItem() + "'");
    }//GEN-LAST:event_comboBrandActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClearProduct;
    private javax.swing.JButton btnDOD;
    private javax.swing.JButton btnDeleteProduct;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdateProduct;
    private javax.swing.JComboBox<String> comboBrand;
    private javax.swing.JComboBox<String> comboCategory;
    private javax.swing.JComboBox<String> comboName;
    private javax.swing.JComboBox<String> comboSearch;
    private project.date.DateChooser dateDOD;
    private javax.swing.JTextField fieldDOD;
    private javax.swing.JTextField fieldID;
    private javax.swing.JTextField fieldName;
    private javax.swing.JTextField fieldPrice;
    private javax.swing.JTextField fieldQuantity;
    private javax.swing.JTextField fieldTotalPrice;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel labelBrand;
    private javax.swing.JLabel labelCategory;
    private javax.swing.JLabel labelDOD;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelPrice;
    private javax.swing.JLabel labelQuantity;
    private javax.swing.JLabel labelSearch;
    private javax.swing.JLabel labelTotalPrice;
    private project.component.ShadowPanel panelBody;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JScrollPane scrollProduct;
    private javax.swing.JSeparator separatorBrand;
    private javax.swing.JSeparator separatorCategory;
    private javax.swing.JSeparator separatorDOD;
    private javax.swing.JSeparator separatorName;
    private javax.swing.JSeparator separatorPrice;
    private javax.swing.JSeparator separatorQuantity;
    private javax.swing.JSeparator separatorTotalPrice;
    private project.swing.Table tableProduct;
    // End of variables declaration//GEN-END:variables
}
