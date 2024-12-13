package project.page;

import javax.swing.JComponent;
import javax.swing.JTable;
import project.Main;
import project.TableUtils;
import project.search.SearchComboBox;
import project.swing.ImageRenderer;
import project.search.SearchComboBox.EnumComboBox;
import project.search.SearchComboBoxField;
import project.search.SearchComboBoxTwo.EnumComboBoxTwo;
import project.search.SearchComboBoxField.EnumComboBoxField;
import project.search.SearchComboBoxTwo;
import project.search.SearchEmpty;

/**
 *
 * @author Dric
 */
public class PageStock extends javax.swing.JPanel {

    public String currentSearchQuery = "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity FROM "
            + Main.tbName_ProductStock + " GROUP BY product_category, product_brand, product_name";

    // Pages
    private final SearchEmpty SearchEmpty = new SearchEmpty();
    private final SearchComboBox SearchComboBox = new SearchComboBox();
    private final SearchComboBoxTwo SearchComboBoxTwo = new SearchComboBoxTwo();
    private final SearchComboBoxField SearchComboBoxField = new SearchComboBoxField();

    /**
     * Creates new form FormBody
     */
    public PageStock() {
        initComponents();

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(225);
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(300);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(458);
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableProduct.getColumnModel().getColumn(5).setPreferredWidth(150);
        tableProduct.getColumnModel().getColumn(5).setCellRenderer(new ImageRenderer());
        tableProduct.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableUtils.refreshTable(tableProduct, "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity FROM "
                + Main.tbName_ProductStock + " GROUP BY product_category, product_brand, product_name", TableUtils.TableEnum.STOCK_DISTINCT);

        tableProduct.setDefaultEditor(Object.class, null);
        tableProduct.setAutoCreateRowSorter(true);
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

    public void refreshTableProduct() {
        TableUtils.refreshTable(tableProduct, currentSearchQuery, TableUtils.TableEnum.STOCK_DISTINCT);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBody = new project.component.ShadowPanel();
        panelMain = new javax.swing.JPanel();
        scrollProduct = new javax.swing.JScrollPane();
        tableProduct = new project.swing.Table();
        labelSearch = new javax.swing.JLabel();
        comboSearch = new javax.swing.JComboBox<>();
        panelSearch = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1389, 844));
        setMinimumSize(new java.awt.Dimension(1389, 844));
        setPreferredSize(new java.awt.Dimension(1389, 844));

        panelBody.setMaximumSize(new java.awt.Dimension(1389, 844));
        panelBody.setMinimumSize(new java.awt.Dimension(1389, 844));
        panelBody.setPreferredSize(new java.awt.Dimension(1389, 844));

        panelMain.setOpaque(false);

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1389, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 844, Short.MAX_VALUE)
        );

        scrollProduct.setBackground(new java.awt.Color(255, 255, 255));
        scrollProduct.setBorder(null);

        tableProduct.setBackground(new java.awt.Color(245, 245, 245));
        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Category", "Brand", "Name", "Retail Price", "Quantity", "Image"
            }
        ));
        tableProduct.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 18)); // NOI18N
        tableProduct.setRowHeight(150);
        scrollProduct.setViewportView(tableProduct);

        labelSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        labelSearch.setText("Search for Products");

        comboSearch.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Everything", "Under a Category", "Under a Brand", "Under a Category and Brand", "According to Quantity" }));
        comboSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSearchActionPerformed(evt);
            }
        });

        panelSearch.setMaximumSize(new java.awt.Dimension(520, 35));
        panelSearch.setMinimumSize(new java.awt.Dimension(520, 35));
        panelSearch.setOpaque(false);
        panelSearch.setPreferredSize(new java.awt.Dimension(520, 35));
        panelSearch.setLayout(new java.awt.BorderLayout());

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
                .addGap(15, 15, 15)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 1350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelBodyLayout.createSequentialGroup()
                        .addComponent(labelSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch)
                    .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(comboSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelSearch)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 775, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String selectedComboBox;
        String selectedComboBox2;
        String selectedTextField;

        switch (String.valueOf(comboSearch.getSelectedItem())) {
            case "Everything":
                currentSearchQuery = "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity FROM "
                        + Main.tbName_ProductStock + " GROUP BY product_category, product_brand, product_name";
                refreshTableProduct();
                break;
            case "Under a Category":
                selectedComboBox = SearchComboBox.getSelectedComboBox();
                currentSearchQuery = "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity FROM "
                        + Main.tbName_ProductStock + " WHERE product_category = '" + selectedComboBox + "' GROUP BY product_category, product_brand, product_name";
                refreshTableProduct();
                break;
            case "Under a Brand":
                selectedComboBox = SearchComboBox.getSelectedComboBox();
                currentSearchQuery = "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity FROM "
                        + Main.tbName_ProductStock + " WHERE product_brand = '" + selectedComboBox + "' GROUP BY product_category, product_brand, product_name";
                refreshTableProduct();
                break;
            case "Under a Category and Brand":
                selectedComboBox = SearchComboBoxTwo.getSelectedComboBox1();
                selectedComboBox2 = SearchComboBoxTwo.getSelectedComboBox2();
                currentSearchQuery = "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity FROM "
                        + Main.tbName_ProductStock + " WHERE product_category = '" + selectedComboBox + "' AND product_brand = '" + selectedComboBox2 + "' GROUP BY product_category, product_brand, product_name";
                refreshTableProduct();
                break;
            case "According to Quantity":
                selectedComboBox = SearchComboBoxField.getLimitValueOperator(SearchComboBoxField.getSelectedComboBox());
                selectedTextField = SearchComboBoxField.getSelectedTextField();
                currentSearchQuery = "SELECT product_category, product_brand, product_name, SUM(product_remaining_quantity) AS product_remaining_quantity "
                        + "FROM " + Main.tbName_ProductStock + " "
                        + "GROUP BY product_category, product_brand, product_name "
                        + "HAVING SUM(product_remaining_quantity) " + selectedComboBox + " " + selectedTextField;
                refreshTableProduct();
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
            case "According to Quantity":
                SearchComboBoxField.selectedSearch = EnumComboBoxField.LIMIT_VALUE;
                setForm(SearchComboBoxField);
                break;
            default:
//                throw new AssertionError();
                break;
        }
    }//GEN-LAST:event_comboSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> comboSearch;
    private javax.swing.JLabel labelSearch;
    private project.component.ShadowPanel panelBody;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JScrollPane scrollProduct;
    private project.swing.Table tableProduct;
    // End of variables declaration//GEN-END:variables
}
