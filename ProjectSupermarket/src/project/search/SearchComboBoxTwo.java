package project.search;

import project.Main;
import project.Queries;

/**
 *
 * @author Dric
 */
public class SearchComboBoxTwo extends javax.swing.JPanel {

    public enum EnumComboBoxTwo {
        CATEGORY_BRAND_DELIVERY,
        CATEGORY_BRAND_ITEM,
    }

    public EnumComboBoxTwo selectedSearch;

    private String tbName;
    private String column_name1 = null, column_name2 = null;

    /**
     * Creates new form FormBody
     */
    public SearchComboBoxTwo() {
        initComponents();
    }

    public void repopulateComboBox(EnumComboBoxTwo searchEnum) {
        if (null != searchEnum) {
            switch (searchEnum) {
                case CATEGORY_BRAND_DELIVERY: {
                    tbName = Main.tbName_ProductStock;
                    column_name1 = "product_category";
                    column_name2 = "product_brand";
                    break;
                }
                case CATEGORY_BRAND_ITEM: {
                    tbName = Main.tbName_ProductItem;
                    column_name1 = "product_category";
                    column_name2 = "product_brand";
                    break;
                }
                default:
                    break;
            }
            repopulateComboBoxQueries();
        }
    }

    public void repopulateComboBoxQueries() {
        Queries.repopulateComboBox(comboBox1, column_name1, "SELECT DISTINCT " + column_name1 + " FROM " + tbName);
        Queries.repopulateComboBox(comboBox2, column_name2, "SELECT " + column_name2 + " FROM " + tbName + " WHERE " + column_name1 + " = '" + comboBox1.getSelectedItem() + "'");
    }

    public String getSelectedComboBox1() {
        return (String) comboBox1.getSelectedItem();
    }

    public String getSelectedComboBox2() {
        return (String) comboBox2.getSelectedItem();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateSearchStart = new project.date.DateChooser();
        dateSearchEnd = new project.date.DateChooser();
        panelMain = new javax.swing.JPanel();
        comboBox1 = new javax.swing.JComboBox<>();
        comboBox2 = new javax.swing.JComboBox<>();

        dateSearchStart.setDateFormat("yyyy-MM-dd");

        dateSearchEnd.setDateFormat("yyyy-MM-dd");

        setMaximumSize(new java.awt.Dimension(393, 35));
        setMinimumSize(new java.awt.Dimension(393, 35));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(393, 35));

        panelMain.setMaximumSize(new java.awt.Dimension(520, 35));
        panelMain.setMinimumSize(new java.awt.Dimension(520, 35));
        panelMain.setOpaque(false);

        comboBox1.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBox1ActionPerformed(evt);
            }
        });

        comboBox2.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        comboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(comboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(comboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 394, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox1ActionPerformed
        Queries.repopulateComboBox(comboBox2, "product_brand", "SELECT product_brand FROM " + tbName + " WHERE product_category = '" + comboBox1.getSelectedItem() + "'");
    }//GEN-LAST:event_comboBox1ActionPerformed

    private void comboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboBox2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboBox1;
    private javax.swing.JComboBox<String> comboBox2;
    private project.date.DateChooser dateSearchEnd;
    private project.date.DateChooser dateSearchStart;
    private javax.swing.JPanel panelMain;
    // End of variables declaration//GEN-END:variables
}