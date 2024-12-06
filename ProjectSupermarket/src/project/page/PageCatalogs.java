package project.page;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import static java.awt.image.ImageObserver.ERROR;
import static java.awt.image.ImageObserver.WIDTH;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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
import javax.swing.table.TableModel;
import project.Main;
import static project.MainUtils.*;
import project.Queries;
import static project.TableUtils.*;
import project.WindowUtils;
import project.search.*;
import project.search.SearchComboBox.EnumComboBox;
import project.search.SearchComboBoxTwo.EnumComboBoxTwo;
import project.search.SearchComboBoxField.EnumComboBoxField;
import project.swing.ImageRenderer;

/**
 *
 * @author Dric
 */
public class PageCatalogs extends javax.swing.JPanel {

    String imgPath = null;
    public String currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductItem;

    // Pages
    private final SearchEmpty SearchEmpty = new SearchEmpty();
    private final SearchComboBox SearchComboBox = new SearchComboBox();
    private final SearchComboBoxTwo SearchComboBoxTwo = new SearchComboBoxTwo();
    private final SearchComboBoxField SearchComboBoxField = new SearchComboBoxField();

    /**
     * Creates new form FormBody
     */
    public PageCatalogs() {
        initComponents();

        WindowUtils.setTransparentFrame(fieldCategoryName, fieldName, fieldRetail);
        fieldCategoryName.getDocument().addDocumentListener(new PageCatalogs.FieldChangeListener());
        fieldName.getDocument().addDocumentListener(new PageCatalogs.FieldChangeListener());
        fieldRetail.getDocument().addDocumentListener(new PageCatalogs.FieldChangeListener());

        tableCategories.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableCategories.getColumnModel().getColumn(1).setPreferredWidth(650);
//        tableCategories.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableCategories.isCellEditable(ERROR, WIDTH);

        tableCategories.setDefaultEditor(Object.class, null);
        tableCategories.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableCategories.getSelectedRow();
                if (selectedRow >= 0) {
                    selectTableCategory(selectedRow);
                }
            }
        });
        tableCategories.setAutoCreateRowSorter(true);

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(25);
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(50);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(75);
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(220);
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(50);
        tableProduct.getColumnModel().getColumn(5).setCellRenderer(new ImageRenderer());
        tableProduct.setDefaultEditor(Object.class, null);
        tableProduct.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableProduct.getSelectedRow();
                if (selectedRow >= 0) {
                    selectTableProduct(selectedRow);
                }
            }
        });
        tableProduct.setAutoCreateRowSorter(true);
    }

    public void refreshTableProduct() {
        refreshTable(tableProduct, currentSearchQuery, TableEnum.CATALOG_PRODUCT);
    }

    public void refreshTableCategories() {
        String query = "SELECT * FROM " + Main.tbName_ProductCategory;

        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQuery(conn, query); ResultSet rs = pst.executeQuery()) {
            DefaultTableModel model = (DefaultTableModel) tableCategories.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                try {
                    String categoryID = rs.getString("product_category_ID");
                    String category = rs.getString("product_category");

                    model.addRow(new Object[]{
                        categoryID, category
                    });

                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            }

        } catch (SQLException e) {
            paneDatabaseError(e);
        }

        repopulateCategoryComboBox();
    }

    public JTable getTableProduct() {
        return tableProduct;
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
            btnAddCategory.setEnabled(!fieldCategoryName.getText().trim().isEmpty()
                    && !fieldCategoryName.getText().trim().equals("Enter Category Name"));

            btnAddProduct.setEnabled(!fieldName.getText().trim().isEmpty()
                    && !fieldName.getText().trim().equals("Enter Product Name")
                    && !fieldRetail.getText().trim().isEmpty()
                    && !fieldRetail.getText().trim().equals("Enter Retail Price"));
        }
    }

    public void clearCategoryFields() {
        fieldID.setText("");
        fieldCategoryName.setText("Enter Category Name");
        fieldCategoryName.setForeground(new Color(153, 153, 153));
        tableCategories.getSelectionModel().clearSelection();
        setUpdateDeleteEnable();
    }

    public void clearProductFields() {
        fieldProductID.setText("");
        comboCategory.setSelectedIndex(0);
        comboBrand.setSelectedIndex(0);
        fieldName.setText("Enter Product Name");
        fieldName.setForeground(new Color(153, 153, 153));
        fieldRetail.setText("Enter Retail Price");
        fieldRetail.setForeground(new Color(153, 153, 153));
        tableProduct.getSelectionModel().clearSelection();
        labelImgIcon.setIcon(null);
        setUpdateDeleteEnableProduct();
    }

    public void setUpdateDeleteEnable() {
        if (fieldID.getText().equals("")) {
            btnUpdateCategory.setEnabled(false);
            btnDeleteCategory.setEnabled(false);
        } else {
            btnUpdateCategory.setEnabled(true);
            btnDeleteCategory.setEnabled(true);
        }
    }

    public void setUpdateDeleteEnableProduct() {
        if (fieldProductID.getText().equals("")) {
            btnUpdateProduct.setEnabled(false);
            btnDeleteProduct.setEnabled(false);
        } else {
            btnUpdateProduct.setEnabled(true);
            btnDeleteProduct.setEnabled(true);
        }
    }

    public void selectTableCategory(int selectedRow) {
        String category_id = (String) tableCategories.getValueAt(selectedRow, 0);
        String category_name = (String) tableCategories.getValueAt(selectedRow, 1);

        fieldID.setText(category_id);
        fieldCategoryName.setText(category_name);

        fieldCategoryName.setForeground(new Color(0, 0, 0));

        setUpdateDeleteEnable();
    }

    public void selectTableProduct(int selectedRow) {
        String product_id = (String) tableProduct.getValueAt(selectedRow, 0);
        String product_category = (String) tableProduct.getValueAt(selectedRow, 1);
        String product_brand = (String) tableProduct.getValueAt(selectedRow, 2);
        String product_name = (String) tableProduct.getValueAt(selectedRow, 3);
        String product_retail = (String) tableProduct.getValueAt(selectedRow, 4);
        ImageIcon imgIcon = (ImageIcon) tableProduct.getValueAt(selectedRow, 5);

        fieldProductID.setText(product_id);
        comboCategory.setSelectedItem(product_category);
        comboBrand.setSelectedItem(product_brand);
        fieldName.setText(product_name);
        fieldRetail.setText(product_retail);

        fieldName.setForeground(new Color(0, 0, 0));
        fieldRetail.setForeground(new Color(0, 0, 0));

        ImageIcon resizedIcon = ResizeImage(imgIcon);
        labelImgIcon.setIcon(resizedIcon);

        setUpdateDeleteEnableProduct();
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
            case SearchComboBoxField search:
                search.repopulateComboBox(search.selectedSearch);
                break;
            default:
                break;
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

        fieldID = new javax.swing.JLabel();
        fieldProductID = new javax.swing.JLabel();
        panelBody = new project.component.ShadowPanel();
        panelCategories = new project.component.ShadowPanel();
        labelCategories = new javax.swing.JLabel();
        scrollCategories = new javax.swing.JScrollPane();
        tableCategories = new project.swing.Table();
        labelCategoryName = new javax.swing.JLabel();
        fieldCategoryName = new javax.swing.JTextField();
        separatorPrice = new javax.swing.JSeparator();
        panelButtons = new javax.swing.JPanel();
        btnClearCategory = new javax.swing.JButton();
        btnDeleteCategory = new javax.swing.JButton();
        btnUpdateCategory = new javax.swing.JButton();
        btnAddCategory = new javax.swing.JButton();
        panelProducts = new project.component.ShadowPanel();
        labelItems = new javax.swing.JLabel();
        labelCategory = new javax.swing.JLabel();
        separatorCategory = new javax.swing.JSeparator();
        labelName = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        separatorName = new javax.swing.JSeparator();
        comboCategory = new javax.swing.JComboBox<>();
        labelImgIcon = new javax.swing.JLabel();
        btnImage = new javax.swing.JButton();
        scrollProduct = new javax.swing.JScrollPane();
        tableProduct = new project.swing.Table();
        panelButtonsProducts = new javax.swing.JPanel();
        btnAddProduct = new javax.swing.JButton();
        btnClearProduct = new javax.swing.JButton();
        btnDeleteProduct = new javax.swing.JButton();
        btnUpdateProduct = new javax.swing.JButton();
        labelBrand = new javax.swing.JLabel();
        comboBrand = new javax.swing.JComboBox<>();
        separatorBrand = new javax.swing.JSeparator();
        separatorRetail = new javax.swing.JSeparator();
        labelRetail = new javax.swing.JLabel();
        fieldRetail = new javax.swing.JTextField();
        labelSearch = new javax.swing.JLabel();
        comboSearch = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        panelSearch = new javax.swing.JPanel();

        fieldID.setText("jLabel1");

        fieldProductID.setText("jLabel1");

        setMaximumSize(new java.awt.Dimension(1389, 844));
        setMinimumSize(new java.awt.Dimension(1389, 844));
        setPreferredSize(new java.awt.Dimension(1389, 844));

        panelBody.setMaximumSize(new java.awt.Dimension(1389, 844));
        panelBody.setMinimumSize(new java.awt.Dimension(1389, 844));
        panelBody.setPreferredSize(new java.awt.Dimension(1389, 844));

        panelCategories.setMaximumSize(new java.awt.Dimension(601, 815));
        panelCategories.setMinimumSize(new java.awt.Dimension(601, 815));
        panelCategories.setShadowSize(5);

        labelCategories.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        labelCategories.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCategories.setText("Manage Categories");

        scrollCategories.setBorder(null);

        tableCategories.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Category ID", "Category"
            }
        ));
        tableCategories.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 18)); // NOI18N
        scrollCategories.setViewportView(tableCategories);

        labelCategoryName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelCategoryName.setText("Category");

        fieldCategoryName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldCategoryName.setForeground(new java.awt.Color(153, 153, 153));
        fieldCategoryName.setText("Enter Category Name");
        fieldCategoryName.setBorder(null);
        fieldCategoryName.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldCategoryName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldCategoryNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldCategoryNameFocusLost(evt);
            }
        });

        panelButtons.setOpaque(false);

        btnClearCategory.setText("Clear");
        btnClearCategory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClearCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearCategoryActionPerformed(evt);
            }
        });

        btnDeleteCategory.setText("Delete");
        btnDeleteCategory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDeleteCategory.setEnabled(false);
        btnDeleteCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCategoryActionPerformed(evt);
            }
        });

        btnUpdateCategory.setText("Update");
        btnUpdateCategory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdateCategory.setEnabled(false);
        btnUpdateCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateCategoryActionPerformed(evt);
            }
        });

        btnAddCategory.setText("Add");
        btnAddCategory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddCategory.setEnabled(false);
        btnAddCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCategoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddCategory, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(btnUpdateCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDeleteCategory, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(btnClearCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClearCategory)
                    .addComponent(btnAddCategory))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeleteCategory)
                    .addComponent(btnUpdateCategory))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelCategoriesLayout = new javax.swing.GroupLayout(panelCategories);
        panelCategories.setLayout(panelCategoriesLayout);
        panelCategoriesLayout.setHorizontalGroup(
            panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCategoriesLayout.createSequentialGroup()
                .addGroup(panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCategoriesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelCategoriesLayout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelCategoriesLayout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addComponent(labelCategoryName, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(labelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 655, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(separatorPrice, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(fieldCategoryName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 667, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        panelCategoriesLayout.setVerticalGroup(
            panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCategoriesLayout.createSequentialGroup()
                .addGroup(panelCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCategoriesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCategoryName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldCategoryName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCategoriesLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(scrollCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelProducts.setMaximumSize(new java.awt.Dimension(601, 815));
        panelProducts.setMinimumSize(new java.awt.Dimension(601, 815));
        panelProducts.setPreferredSize(new java.awt.Dimension(601, 815));
        panelProducts.setShadowSize(5);

        labelItems.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        labelItems.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelItems.setText("Manage Products");

        labelCategory.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelCategory.setText("Category");

        labelName.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelName.setText("Name");

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

        comboCategory.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BEVERAGES", "BREADS AND BAKERY", "CANNED AND JARRED", "DAIRY", "DRY GOODS AND BAKING", "FRESH PRODUCE", "FROZEN", "HEALTH AND WELLNESS", "HOUSEHOLD", "MEAT", "PERSONAL CARE", "PET CARE", "SNACKS" }));
        comboCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboCategoryActionPerformed(evt);
            }
        });

        btnImage.setText("Select Image");
        btnImage.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImageActionPerformed(evt);
            }
        });

        scrollProduct.setBorder(null);

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Category", "Brand", "Name", "Retail Price", "Image"
            }
        ));
        tableProduct.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 18)); // NOI18N
        tableProduct.setRowHeight(150);
        scrollProduct.setViewportView(tableProduct);

        panelButtonsProducts.setOpaque(false);

        btnAddProduct.setText("Add");
        btnAddProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddProduct.setEnabled(false);
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
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

        btnUpdateProduct.setText("Update");
        btnUpdateProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdateProduct.setEnabled(false);
        btnUpdateProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateProductActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsProductsLayout = new javax.swing.GroupLayout(panelButtonsProducts);
        panelButtonsProducts.setLayout(panelButtonsProductsLayout);
        panelButtonsProductsLayout.setHorizontalGroup(
            panelButtonsProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsProductsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(btnUpdateProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelButtonsProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnClearProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(btnDeleteProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );
        panelButtonsProductsLayout.setVerticalGroup(
            panelButtonsProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsProductsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddProduct)
                    .addComponent(btnClearProduct))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelButtonsProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeleteProduct)
                    .addComponent(btnUpdateProduct))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labelBrand.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelBrand.setText("Brand");

        comboBrand.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboBrand.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BEVERAGES", "BREADS AND BAKERY", "CANNED AND JARRED", "DAIRY", "DRY GOODS AND BAKING", "FRESH PRODUCE", "FROZEN", "HEALTH AND WELLNESS", "HOUSEHOLD", "MEAT", "PERSONAL CARE", "PET CARE", "SNACKS" }));

        labelRetail.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelRetail.setText("Retail Price");

        fieldRetail.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldRetail.setForeground(new java.awt.Color(153, 153, 153));
        fieldRetail.setText("Enter Retail Price");
        fieldRetail.setBorder(null);
        fieldRetail.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldRetail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldRetailFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldRetailFocusLost(evt);
            }
        });
        fieldRetail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldRetailKeyTyped(evt);
            }
        });

        labelSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelSearch.setText("Search for Products");

        comboSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Everything", "Under a Category", "Under a Brand", "Under a Category and Brand", "According to Retail Price" }));
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

        panelSearch.setMaximumSize(new java.awt.Dimension(520, 35));
        panelSearch.setMinimumSize(new java.awt.Dimension(520, 35));
        panelSearch.setOpaque(false);
        panelSearch.setPreferredSize(new java.awt.Dimension(520, 35));
        panelSearch.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout panelProductsLayout = new javax.swing.GroupLayout(panelProducts);
        panelProducts.setLayout(panelProductsLayout);
        panelProductsLayout.setHorizontalGroup(
            panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductsLayout.createSequentialGroup()
                .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelProductsLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelItems, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelProductsLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(panelButtonsProducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelProductsLayout.createSequentialGroup()
                                            .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(labelName, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                                .addComponent(labelBrand, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGap(17, 17, 17)
                                            .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(separatorBrand, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(fieldName)
                                                .addComponent(comboBrand, 0, 245, Short.MAX_VALUE)))
                                        .addGroup(panelProductsLayout.createSequentialGroup()
                                            .addComponent(labelRetail)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(separatorName, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(separatorRetail)
                                                .addComponent(fieldRetail, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))))
                                    .addGroup(panelProductsLayout.createSequentialGroup()
                                        .addComponent(labelCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(comboCategory, 0, 245, Short.MAX_VALUE)
                                            .addComponent(separatorCategory))))))
                        .addGap(18, 18, 18))
                    .addGroup(panelProductsLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(btnImage, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(49, 49, 49)
                        .addComponent(labelImgIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 975, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelProductsLayout.createSequentialGroup()
                        .addComponent(labelSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(91, 91, 91)
                        .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSearch)))
                .addGap(92, 92, 92))
        );
        panelProductsLayout.setVerticalGroup(
            panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelItems, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelSearch))
                    .addGroup(panelProductsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnSearch)
                            .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelProductsLayout.createSequentialGroup()
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addComponent(separatorCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorName, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fieldRetail, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRetail, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorRetail, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(panelProductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelProductsLayout.createSequentialGroup()
                                .addGap(58, 58, 58)
                                .addComponent(btnImage))
                            .addComponent(labelImgIcon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(101, 101, 101)
                        .addComponent(panelButtonsProducts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 514, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 1355, Short.MAX_VALUE)
                    .addComponent(panelCategories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelProducts, javax.swing.GroupLayout.PREFERRED_SIZE, 591, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fieldCategoryNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldCategoryNameFocusGained
        WindowUtils.setDefaultField(fieldCategoryName, "Enter Category Name", WindowUtils.FieldFocus.GAINED, Color.BLACK);
    }//GEN-LAST:event_fieldCategoryNameFocusGained

    private void fieldCategoryNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldCategoryNameFocusLost
        WindowUtils.setDefaultField(fieldCategoryName, "Enter Category Name", WindowUtils.FieldFocus.LOST, Color.BLACK);
    }//GEN-LAST:event_fieldCategoryNameFocusLost

    private void btnAddCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCategoryActionPerformed
        if (isLoggedIn()) {
            String product_category_name = fieldCategoryName.getText().toUpperCase();
            if (!isAlreadyInColumn(tableCategories, product_category_name, 1)) {
                String query = "INSERT INTO " + Main.tbName_ProductCategory + " (product_category) VALUES (?)";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    pst.setString(1, product_category_name);
                    pst.executeUpdate();

                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        Queries.addSupplyHistoryEntry(Queries.EnumHistory.CATALOG_CATEGORY_ADD, "[" + id + "] " + product_category_name);
                    }

                    clearCategoryFields();
                    refreshTableCategories();

                    JOptionPane.showMessageDialog(this, "Category Added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            } else {
                JOptionPane.showMessageDialog(this, "This category already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            paneNotLoggedIn();
        }
    }//GEN-LAST:event_btnAddCategoryActionPerformed

    private void btnUpdateCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateCategoryActionPerformed
        if (isLoggedIn()) {
            int id = Integer.parseInt(fieldID.getText());
            String product_category_name = fieldCategoryName.getText().toUpperCase();

            if (!isAlreadyInColumn(tableCategories, product_category_name, 1)) {
                int warnUser = JOptionPane.showConfirmDialog(
                        null,
                        "Updating this Category's name will also update the corresponding category name in other related tables. Do you want to proceed?",
                        "Warning: Category Update",
                        JOptionPane.YES_NO_OPTION
                );

                if (warnUser == JOptionPane.YES_OPTION) {
                    String queryHistory = "SELECT * FROM " + Main.tbName_ProductCategory + " WHERE product_category_ID = ?";
                    try (Connection conn = Queries.getConnection(Main.dbName);) {
                        PreparedStatement pst = conn.prepareStatement(queryHistory);
                        pst.setInt(1, id);

                        ResultSet rs = pst.executeQuery();

                        if (rs.next()) {
                            String old_category = rs.getString("product_category");
                            int num_changes = 0;

                            StringBuilder changeLog = new StringBuilder("[" + id + "] | ");
                            if (!old_category.equals(product_category_name)) {
                                changeLog.append("Category: ").append(old_category).append(" -> ").append(product_category_name).append("; ");
                                num_changes++;
                            }

                            // add entry if there are changes
                            if (num_changes > 0) {
                                Queries.addSupplyHistoryEntry(Queries.EnumHistory.CATALOG_CATEGORY_UPDATE, changeLog.toString());
                            }
                        }
                    } catch (SQLException e) {
                        paneDatabaseError(e);
                    }

                    String query = "UPDATE " + Main.tbName_ProductCategory + " SET product_category = ? WHERE product_category_ID = ?";

                    try (Connection conn = Queries.getConnection(Main.dbName);) {
                        PreparedStatement pst = conn.prepareStatement(query);
                        pst.setString(1, product_category_name);
                        pst.setInt(2, id);
                        pst.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Category Updated!", "Success", JOptionPane.INFORMATION_MESSAGE);

                        clearCategoryFields();
                        refreshTableCategories();
                    } catch (SQLException e) {
                        paneDatabaseError(e);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "This category already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            paneNotLoggedIn();
        }
    }//GEN-LAST:event_btnUpdateCategoryActionPerformed

    private void btnClearCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCategoryActionPerformed
        clearCategoryFields();
    }//GEN-LAST:event_btnClearCategoryActionPerformed

    private void btnDeleteCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCategoryActionPerformed
        if (isLoggedIn()) {
            int warnUser = JOptionPane.showConfirmDialog(
                    null,
                    "Confirm Delete?",
                    "Warning: Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (warnUser == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(fieldID.getText());

                String queryHistory = "SELECT * FROM " + Main.tbName_ProductCategory + " WHERE product_category_ID = ?";
                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(queryHistory);
                    pst.setInt(1, id);

                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String product_category = rs.getString("product_category");

                        Queries.addSupplyHistoryEntry(Queries.EnumHistory.CATALOG_CATEGORY_DELETE, "[" + id + "] " + product_category);
                    }
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }

                String query = "DELETE FROM " + Main.tbName_ProductCategory + " WHERE product_category_ID = ?";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setInt(1, id);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Category Removed!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearCategoryFields();
                    refreshTableCategories();
                    Queries.resetPrimaryKey(Main.tbName_ProductCategory);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1451) {
                        JOptionPane.showMessageDialog(this,
                                "This category cannot be deleted because there are products associated with it in other tables.",
                                "Deletion Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        paneDatabaseError(e);
                    }
                }
            }

        } else {
            paneNotLoggedIn();
        }
    }//GEN-LAST:event_btnDeleteCategoryActionPerformed

    private void fieldNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldNameFocusGained
        WindowUtils.setDefaultField(fieldName, "Enter Product Name", WindowUtils.FieldFocus.GAINED, Color.BLACK);
    }//GEN-LAST:event_fieldNameFocusGained

    private void fieldNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldNameFocusLost
        WindowUtils.setDefaultField(fieldName, "Enter Product Name", WindowUtils.FieldFocus.LOST, Color.BLACK);
    }//GEN-LAST:event_fieldNameFocusLost

    private void comboCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboCategoryActionPerformed
        repopulateBrandComboBox();
    }//GEN-LAST:event_comboCategoryActionPerformed

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

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        if (isLoggedIn()) {
            int product_category_ID = 0;
            String product_category = comboCategory.getSelectedItem().toString();
            String product_brand = comboBrand.getSelectedItem().toString();
            String product_retail_price = fieldRetail.getText();
            String query = "SELECT product_category_ID FROM " + Main.tbName_ProductCategory + " WHERE product_category = ?";

            try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQueryWithParameters(conn, query, product_category); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    product_category_ID = rs.getInt("product_category_ID");
                }
            } catch (SQLException e) {
                paneDatabaseError(e);
            }

            String product_name = fieldName.getText();
            String product_full_name = product_category + " / " + product_brand + " / " + product_name;
            
            if (!isAlreadyInColumn(tableProduct, product_name, 3)) {
                query = "INSERT INTO " + Main.tbName_ProductItem + " (product_category_ID, product_category, product_brand, product_name, product_retail_price, product_image) VALUES (?, ?, ?, ?, ?, ?)";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    pst.setInt(1, product_category_ID);
                    pst.setString(2, product_category);
                    pst.setString(3, product_brand);
                    pst.setString(4, product_name);
                    pst.setString(5, product_retail_price);

                    if (labelImgIcon.getIcon() == null) {
                        InputStream img = new FileInputStream(new File(imgPath));
                        pst.setBlob(6, img);
                    } else {
                        ImageIcon icon = (ImageIcon) labelImgIcon.getIcon();
                        Image image = icon.getImage();

                        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = bufferedImage.createGraphics();
                        g2d.drawImage(image, 0, 0, null);
                        g2d.dispose();

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                        } catch (IOException e) {
                            e.printStackTrace(System.out);
                        }

                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        InputStream img = new ByteArrayInputStream(imageBytes);

                        pst.setBlob(6, img);
                    }

                    pst.executeUpdate();

                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        Queries.addSupplyHistoryEntry(Queries.EnumHistory.CATALOG_PRODUCT_ADD, "[" + id + "] " + product_full_name + "  -  (Retail: " + product_retail_price + ")");
                    }

                    clearProductFields();
                    refreshTableProduct();

                    JOptionPane.showMessageDialog(this, "Product Added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException e) {
                    paneDatabaseError(e);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PageCatalogs.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(this, "This product already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            paneNotLoggedIn();
        }
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnUpdateProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateProductActionPerformed
        if (isLoggedIn()) {
            int warnUser = JOptionPane.showConfirmDialog(
                    null,
                    "Confirm Update?",
                    "Warning: Update",
                    JOptionPane.YES_NO_OPTION
            );

            if (warnUser == JOptionPane.YES_OPTION) {
                String path = imgPath;

                int id = Integer.parseInt(fieldProductID.getText());
                String product_category = comboCategory.getSelectedItem().toString();
                String product_name = fieldName.getText();
                String product_retail = fieldRetail.getText();

                String queryHistory = "SELECT * FROM " + Main.tbName_ProductItem + " WHERE ID = ?";
                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(queryHistory);
                    pst.setInt(1, id);

                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String old_category = rs.getString("product_category");
                        String old_brand = rs.getString("product_brand");
                        String old_name = rs.getString("product_name");
                        String old_price = rs.getString("product_retail_price");
                        String product_full_name = old_category + " / " + old_brand + " / " + old_name;
                        int num_changes = 0;

                        StringBuilder changeLog = new StringBuilder("[" + id + "] " + product_full_name + " | ");
                        if (!old_category.equals(product_category)) {
                            changeLog.append("Category: ").append(old_category).append(" -> ").append(product_category).append("; ");
                            num_changes++;
                        }
                        if (!old_name.equals(product_name)) {
                            changeLog.append("Name: ").append(old_name).append(" -> ").append(product_name).append("; ");
                            num_changes++;
                        }
                        if (!old_price.equals(product_retail)) {
                            changeLog.append("Price: ").append(old_price).append(" -> ").append(product_retail).append("; ");
                            num_changes++;
                        }

                        // add entry if there are changes
                        if (num_changes > 0) {
                            Queries.addSupplyHistoryEntry(Queries.EnumHistory.CATALOG_PRODUCT_UPDATE, changeLog.toString());
                        }
                    }
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }

                String query = "UPDATE " + Main.tbName_ProductItem + " SET product_category = ?, product_name = ?, product_retail_price = ?  WHERE ID = ?";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setString(1, product_category);
                    pst.setString(2, product_name);
                    pst.setString(3, product_retail);
                    pst.setInt(4, id);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product Updated!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearProductFields();
                    refreshTableProduct();
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }

                if (path != null) {
                    try {
                        InputStream img = new FileInputStream(new File(path));
                        query = "UPDATE " + Main.tbName_ProductItem + " SET product_image = ? WHERE ID = ?";

                        try (Connection conn = Queries.getConnection(Main.dbName);) {
                            PreparedStatement pst = conn.prepareStatement(query);
                            pst.setBlob(1, img);
                            pst.setInt(2, id);
                            pst.executeUpdate();

                            clearProductFields();
                            refreshTableProduct();
                        } catch (SQLException e) {
                            paneDatabaseError(e);
                        }
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                }
            }

        } else {
            paneNotLoggedIn();
        }
    }//GEN-LAST:event_btnUpdateProductActionPerformed

    private void btnClearProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearProductActionPerformed
        clearProductFields();
    }//GEN-LAST:event_btnClearProductActionPerformed

    private void btnDeleteProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProductActionPerformed
        if (isLoggedIn()) {
            int warnUser = JOptionPane.showConfirmDialog(
                    null,
                    "Confirm Delete?",
                    "Warning: Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (warnUser == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(fieldProductID.getText());

                String queryHistory = "SELECT * FROM " + Main.tbName_ProductItem + " WHERE ID = ?";
                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(queryHistory);
                    pst.setInt(1, id);

                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String product_category = rs.getString("product_category");
                        String product_brand = rs.getString("product_brand");
                        String product_name = rs.getString("product_name");
                        String product_full_name = product_category + " / " + product_brand + " / " + product_name;

                        Queries.addSupplyHistoryEntry(Queries.EnumHistory.CATALOG_PRODUCT_DELETE, "[" + id + "] " + product_full_name);
                    }
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }

                String query = "DELETE FROM " + Main.tbName_ProductItem + " WHERE ID = ?";

                try (Connection conn = Queries.getConnection(Main.dbName);) {
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setInt(1, id);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product Removed!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearProductFields();
                    refreshTableProduct();
                    Queries.resetPrimaryKey(Main.tbName_ProductItem);
                } catch (SQLException e) {
                    paneDatabaseError(e);
                }
            }

        } else {
            paneNotLoggedIn();
        }
    }//GEN-LAST:event_btnDeleteProductActionPerformed

    private void fieldRetailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldRetailFocusGained
        WindowUtils.setDefaultField(fieldRetail, "Enter Retail Price", WindowUtils.FieldFocus.GAINED, Color.BLACK);
    }//GEN-LAST:event_fieldRetailFocusGained

    private void fieldRetailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldRetailFocusLost
        WindowUtils.setDefaultField(fieldRetail, "Enter Retail Price", WindowUtils.FieldFocus.LOST, Color.BLACK);
    }//GEN-LAST:event_fieldRetailFocusLost

    private void fieldRetailKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldRetailKeyTyped
        WindowUtils.enforceDigits(evt);
    }//GEN-LAST:event_fieldRetailKeyTyped

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String selectedComboBox;
        String selectedComboBox2;
        String selectedTextField;

        switch (String.valueOf(comboSearch.getSelectedItem())) {
            case "Everything":
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductItem;
                refreshTableProduct();
                break;
            case "Under a Category":
                selectedComboBox = SearchComboBox.getSelectedComboBox();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductItem + " WHERE product_category = '" + selectedComboBox + "'";
                refreshTableProduct();
                break;
            case "Under a Brand":
                selectedComboBox = SearchComboBox.getSelectedComboBox();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductItem + " WHERE product_brand = '" + selectedComboBox + "'";
                refreshTableProduct();
                break;
            case "Under a Category and Brand":
                selectedComboBox = SearchComboBoxTwo.getSelectedComboBox1();
                selectedComboBox2 = SearchComboBoxTwo.getSelectedComboBox2();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductItem + " WHERE product_category = '" + selectedComboBox + "' AND product_brand = '" + selectedComboBox2 + "'";
                refreshTableProduct();
                break;
            case "According to Retail Price":
                selectedComboBox = SearchComboBoxField.getLimitValueOperator(SearchComboBoxField.getSelectedComboBox());
                selectedTextField = SearchComboBoxField.getSelectedTextField();
                currentSearchQuery = "SELECT * FROM " + Main.tbName_ProductItem + " WHERE product_retail_price " + selectedComboBox + " " + selectedTextField;
                refreshTableProduct();
                break;
            default:
//                throw new AssertionError();
                break;
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void comboSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSearchActionPerformed
        switch (String.valueOf(comboSearch.getSelectedItem())) {
            case "Everything":
                setForm(SearchEmpty);
                break;
            case "Under a Category":
                SearchComboBox.selectedSearch = EnumComboBox.CATEGORY_ITEM;
                setForm(SearchComboBox);
                break;
            case "Under a Brand":
                SearchComboBox.selectedSearch = EnumComboBox.BRAND_ITEM;
                setForm(SearchComboBox);
                break;
            case "Under a Category and Brand":
                SearchComboBoxTwo.selectedSearch = EnumComboBoxTwo.CATEGORY_BRAND_ITEM;
                setForm(SearchComboBoxTwo);
                break;
            case "According to Retail Price":
                SearchComboBoxField.selectedSearch = EnumComboBoxField.LIMIT_VALUE;
                setForm(SearchComboBoxField);
                break;
            default:
//                throw new AssertionError();
                break;
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

    public static boolean isAlreadyInColumn(JTable table, String name, int column_position) {
        TableModel model = table.getModel();

        for (int row = 0; row < model.getRowCount(); row++) {
            Object valueInColumn = model.getValueAt(row, column_position);

            if (valueInColumn != null && valueInColumn.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void repopulateCategoryComboBox() {
        Queries.repopulateComboBox(comboCategory, "product_category", "SELECT DISTINCT product_category FROM " + Main.tbName_ProductCategory);
    }

    public void repopulateBrandComboBox() {
        Queries.repopulateComboBox(comboBrand, "brand_name", "SELECT brand_name FROM " + Main.tbName_CategoryBrands + " WHERE product_category = '" + comboCategory.getSelectedItem() + "'");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCategory;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnClearCategory;
    private javax.swing.JButton btnClearProduct;
    private javax.swing.JButton btnDeleteCategory;
    private javax.swing.JButton btnDeleteProduct;
    private javax.swing.JButton btnImage;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdateCategory;
    private javax.swing.JButton btnUpdateProduct;
    private javax.swing.JComboBox<String> comboBrand;
    private javax.swing.JComboBox<String> comboCategory;
    private javax.swing.JComboBox<String> comboSearch;
    private javax.swing.JTextField fieldCategoryName;
    private javax.swing.JLabel fieldID;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel fieldProductID;
    private javax.swing.JTextField fieldRetail;
    private javax.swing.JLabel labelBrand;
    private javax.swing.JLabel labelCategories;
    private javax.swing.JLabel labelCategory;
    private javax.swing.JLabel labelCategoryName;
    private javax.swing.JLabel labelImgIcon;
    private javax.swing.JLabel labelItems;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelRetail;
    private javax.swing.JLabel labelSearch;
    private project.component.ShadowPanel panelBody;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelButtonsProducts;
    private project.component.ShadowPanel panelCategories;
    private project.component.ShadowPanel panelProducts;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JScrollPane scrollCategories;
    private javax.swing.JScrollPane scrollProduct;
    private javax.swing.JSeparator separatorBrand;
    private javax.swing.JSeparator separatorCategory;
    private javax.swing.JSeparator separatorName;
    private javax.swing.JSeparator separatorPrice;
    private javax.swing.JSeparator separatorRetail;
    private project.swing.Table tableCategories;
    private project.swing.Table tableProduct;
    // End of variables declaration//GEN-END:variables
}
