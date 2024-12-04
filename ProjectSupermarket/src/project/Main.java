package project;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

import javax.swing.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;
import project.page.*;

/**
 *
 * @author Dric
 */
public class Main extends javax.swing.JFrame {

    public static String dbName = "DB_Supermarket";
    public static String tbName_Employee = "TB_Employee";
    public static String tbName_CategoryBrands = "TB_CategoryBrands";
    public static String tbName_ProductStock = "TB_ProductStock";
    public static String tbName_ProductCategory = "TB_ProductCategory";
    public static String tbName_ProductBrand = "TB_ProductBrand";
    public static String tbName_ProductItem = "TB_ProductItems";
    private static int userSessionID = -1;
    private String userSessionName;

    private LogIn LogInFrame;

    // Pages
    private PageStock PageStock = new PageStock();
    private PageDeliver PageDeliver = new PageDeliver();
    private PageCheckout PageCheckout = new PageCheckout();
    private PageCatalogs PageCatalogs = new PageCatalogs();

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        setBackground(new Color(0, 0, 0, 0));
        WindowUtils.initMoving(Main.this, panelMenu);
        WindowUtils.initMoving(Main.this, panelHeader);

        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
        updateDateTime();

        setForm(PageStock); // starting page
        panelMenu.addEventMenuSelected((int index) -> {
            //System.out.println("index " + index);
            switch (index) {
                case 1:
                    PageStock.refreshTableProduct();
                    setForm(PageStock);
                    break;
                case 2:
                    PageDeliver.repopulateCategoryComboBox();
                    setForm(PageDeliver);
                    break;
                case 3:
                    PageCatalogs.refreshTableCategories();
                    PageCatalogs.refreshTableProduct();
                    PageCatalogs.repopulateCategoryComboBox();
                    setForm(PageCatalogs);
                    break;
                case 4:
                    setForm(PageCheckout);
                    break;
                case 9:
                    if (!isUserLogged() && !LogInFrame.isVisible()) {
                        LogInFrame.setVisible(true);
                    }
                    break;
                case 10:
                    if (isUserLogged()) {
                        int log_out = JOptionPane.showConfirmDialog(
                                this,
                                "Are you sure you wish to log out?",
                                "Log Out",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (log_out == JOptionPane.YES_OPTION) {
                            userLogOut();
                        }
                    }
                    break;
                default:
                    break;
            }
        }); // every possible pages
        
        setAdmin(true);
    }
    
    private void setAdmin(boolean admin){
        if (admin) {
            String employee_name = "Admin";
            String employee_password = "123";
            String query = "SELECT * FROM " + tbName_Employee + " WHERE employee_name = ? AND employee_password = ?";

            try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQueryWithParameters(conn, query, employee_name, employee_password); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Login success
                    int userID = rs.getInt("employee_ID");
                    setUserSessionID(userID);
                    updateUserSession();
                    dispose();
                } else {
                    // Login failed
                    JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(System.out);
            }
        }
    }

    private void setForm(JComponent com) {
        panelBody.removeAll();
        panelBody.add(com);
        panelBody.repaint();
        panelBody.revalidate();
    }

    private void updateDateTime() {
        // Format the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        labelDateTime.setText(currentDateTime);
    }

    public void setUserSessionID(int userSessionID) {
        this.userSessionID = userSessionID;
    }

    public static int getUserSessionID() {
        return userSessionID;
    }

    public void updateUserSession() {
        String userSessionIDString = Integer.toString(userSessionID);
        labelUserSession.setText(userSessionIDString);
//        System.out.println("employee_ID : " + userSessionID);

        if (userSessionID == -1) {
            userSessionName = "";
            labelUserSession.setText("No Account");
//            System.out.println("No Log : " + userSessionID);
        } else {
//            System.out.println("Log : " + userSessionID);
            String query = "SELECT employee_name FROM " + tbName_Employee + " WHERE employee_ID = ?";

            try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQueryWithParameters(conn, query, userSessionIDString); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String userName = rs.getString("employee_name");
                    userSessionName = userName;
                    labelUserSession.setText("Welcome, " + userSessionName);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(System.out);
            }
        }
    }

    public void userLogOut() {
        setUserSessionID(-1);
        updateUserSession();

        LogInFrame.clearFields();
    }

    public boolean isUserLogged() {
        boolean isUserLogged = false;
        if (userSessionID > 0) {
            isUserLogged = true;
        }
//        System.out.println("Is Logged : " + isUserLogged + "(" + userSessionID + ")");
        return isUserLogged;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu2 = new project.component.Menu();
        panelBorder = new project.swing.PanelBorder();
        panelMenu = new project.component.Menu();
        panelBody = new project.component.ShadowPanel();
        panelHeader = new javax.swing.JPanel();
        labelUserSession = new javax.swing.JLabel();
        labelDateTime = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();

        javax.swing.GroupLayout menu2Layout = new javax.swing.GroupLayout(menu2);
        menu2.setLayout(menu2Layout);
        menu2Layout.setHorizontalGroup(
            menu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        menu2Layout.setVerticalGroup(
            menu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dashboard");
        setUndecorated(true);

        panelBorder.setMaximumSize(new java.awt.Dimension(1600, 900));
        panelBorder.setMinimumSize(new java.awt.Dimension(1600, 900));

        panelBody.setMaximumSize(new java.awt.Dimension(1389, 844));
        panelBody.setMinimumSize(new java.awt.Dimension(1389, 844));
        panelBody.setPreferredSize(new java.awt.Dimension(1389, 844));
        panelBody.setLayout(new java.awt.BorderLayout());

        panelHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelUserSession.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        labelUserSession.setText("No Account");
        labelUserSession.setFocusable(false);

        labelDateTime.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        labelDateTime.setText("Date Time");
        labelDateTime.setFocusable(false);

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelUserSession, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(labelDateTime, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(406, 406, 406))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelUserSession, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelDateTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/project/gfx/interface/cross2.png"))); // NOI18N
        btnClose.setBorder(null);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBorderLayout = new javax.swing.GroupLayout(panelBorder);
        panelBorder.setLayout(panelBorderLayout);
        panelBorderLayout.setHorizontalGroup(
            panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorderLayout.createSequentialGroup()
                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBorderLayout.createSequentialGroup()
                        .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)
                        .addContainerGap())
                    .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        panelBorderLayout.setVerticalGroup(
            panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelBorderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClose)
                    .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelBorder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBorder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnCloseActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        Main main = new Main();
        java.awt.EventQueue.invokeLater(() -> {
            main.LogInFrame = new LogIn(main);
//            main.LogInFrame.setVisible(true);
            main.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JLabel labelDateTime;
    private javax.swing.JLabel labelUserSession;
    private project.component.Menu menu2;
    private project.component.ShadowPanel panelBody;
    private project.swing.PanelBorder panelBorder;
    private javax.swing.JPanel panelHeader;
    private project.component.Menu panelMenu;
    // End of variables declaration//GEN-END:variables

}
