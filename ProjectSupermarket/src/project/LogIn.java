package project;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static project.Main.tbName_Employee;

/**
 *
 * @author Dric
 */
public class LogIn extends javax.swing.JFrame {

    private final String login_button_address;
    private boolean signup_selected;
    private boolean signup_and_login;
    private final Main main;

    /**
     * Creates new form LogIn
     *
     * @param main
     */
    public LogIn(Main main) {
        this.main = main;
        initComponents();

        setBackground(new Color(0, 0, 0, 0));
        WindowUtils.setTransparentFrame(panelHeader);
        WindowUtils.initMoving(LogIn.this, panelHeader);

        login_button_address = "/project/gfx/interface/login_selected.png";
        WindowUtils.setBtnIcon(btnLogIn, login_button_address);
        WindowUtils.setBtnIcon(btnSignUp, login_button_address);
        WindowUtils.resetBtnIcon(btnSignUp);
        signup_selected = false;
        loginFieldsRefresh();

        WindowUtils.setTransparentFrame(fieldUsername);
        WindowUtils.setTransparentFrame(fieldPassword);
        WindowUtils.setTransparentFrame(fieldConfirmPassword);
        WindowUtils.setTransparentFrame(fieldSecurityAnswer);

        fieldUsername.getDocument().addDocumentListener(new FieldChangeListener());
        fieldPassword.getDocument().addDocumentListener(new FieldChangeListener());
        fieldConfirmPassword.getDocument().addDocumentListener(new FieldChangeListener());
        fieldSecurityAnswer.getDocument().addDocumentListener(new FieldChangeListener());
        comboSecurityQuestion.addActionListener(new FieldChangeListener());
    }

    public void clearFields() {
        fieldUsername.setText("Enter Username");
        fieldUsername.setForeground(new Color(153, 153, 153));
        fieldPassword.setText("Enter Password");
        fieldPassword.setForeground(new Color(153, 153, 153));
        fieldConfirmPassword.setText("Enter Password");
        fieldConfirmPassword.setForeground(new Color(153, 153, 153));
        fieldSecurityAnswer.setText("Enter Answer");
        fieldSecurityAnswer.setForeground(new Color(153, 153, 153));
    }

    private void loginFieldsRefresh() {
        signup_and_login = signup_selected;

        labelConfirmPassword.setVisible(signup_selected);
        fieldConfirmPassword.setVisible(signup_selected);
        separatorConfirmPassword.setVisible(signup_selected);
        labelSecurityQuestion.setVisible(signup_selected);
        comboSecurityQuestion.setVisible(signup_selected);
        labelSecurityAnswer.setVisible(signup_selected);
        fieldSecurityAnswer.setVisible(signup_selected);
        separatorSecurityAnswer.setVisible(signup_selected);

        btnForgotPassword.setVisible(!signup_selected);
        btnLogInProceed.setVisible(!signup_selected);
        btnCreateAccount.setVisible(signup_selected);
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
            String employee_name = fieldUsername.getText().trim();
            String employee_password = new String(fieldPassword.getPassword()).trim();
            String confirm_Password = new String(fieldConfirmPassword.getPassword()).trim();
            String employee_qsec_answer = fieldSecurityAnswer.getText().trim();

            if (signup_selected) {
                boolean isValid = !employee_name.isEmpty()
                        && !employee_name.equals("Enter Username")
                        && !employee_password.isEmpty()
                        && !employee_password.equals("Enter Password")
                        && !confirm_Password.isEmpty()
                        && !confirm_Password.equals("Enter Password")
                        && !employee_qsec_answer.isEmpty()
                        && !employee_qsec_answer.equals("Enter Answer");
                btnCreateAccount.setEnabled(isValid);
            } else {
                boolean isValid = !employee_name.isEmpty()
                        && !employee_name.equals("Enter Username")
                        && !employee_password.isEmpty()
                        && !employee_password.equals("Enter Password");
                btnLogInProceed.setEnabled(isValid);
            }
        }
    }

    private void createAccount() {
        String employee_name = fieldUsername.getText();
        String employee_password = new String(fieldPassword.getPassword()).trim();
        String confirm_Password = new String(fieldConfirmPassword.getPassword()).trim();
        int employee_qsec_index = comboSecurityQuestion.getSelectedIndex();
        String employee_qsec_answer = fieldSecurityAnswer.getText();

        if (!employee_password.equals(confirm_Password)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            saveAccountData(employee_name, employee_password, employee_qsec_index, employee_qsec_answer);
        }
    }

    private void saveAccountData(String employee_name, String employee_password, int employee_qsec_index, String employee_qsec_answer) {
        String query = "SELECT COUNT(*) FROM " + tbName_Employee + " WHERE employee_name = ?";

        try (Connection conn = Queries.getConnection(Main.dbName)) {
            // Check if the username already exists
            if (Queries.recordExists(conn, query, employee_name)) {
                JOptionPane.showMessageDialog(this, "The username is already taken. Please choose another username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If the username is unique, create the account
            query = "INSERT INTO " + tbName_Employee + " (employee_name, employee_password, employee_qsec_index, employee_qsec_answer)\n"
                    + "VALUES (?, ?, ?, ?)";
            Queries.executeUpdate(conn, query, employee_name, employee_password, String.valueOf(employee_qsec_index), employee_qsec_answer);
            JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            logInAccount();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(System.out);
        }
    }

    private void logInAccount() {
        String employee_name = fieldUsername.getText();
        String employee_password = new String(fieldPassword.getPassword()).trim();
        String query = "SELECT * FROM " + tbName_Employee + " WHERE employee_name = ? AND employee_password = ?";

        try (Connection conn = Queries.getConnection(Main.dbName); PreparedStatement pst = Queries.prepareQueryWithParameters(conn, query, employee_name, employee_password); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                // Login success
                int userID = rs.getInt("employee_ID");
                main.setUserSessionID(userID);
                main.updateUserSession();

                if (!signup_and_login) {
                    JOptionPane.showMessageDialog(this, "Log In Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    signup_and_login = false;
                }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelLogIn = new project.component.GradientPanel();
        panelHeader = new javax.swing.JPanel();
        labelTitle = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        btnLogIn = new javax.swing.JButton();
        btnSignUp = new javax.swing.JButton();
        labelUsername = new javax.swing.JLabel();
        fieldUsername = new javax.swing.JTextField();
        separatorUsername = new javax.swing.JSeparator();
        labelPassword = new javax.swing.JLabel();
        fieldPassword = new javax.swing.JPasswordField();
        separatorPassword = new javax.swing.JSeparator();
        labelConfirmPassword = new javax.swing.JLabel();
        fieldConfirmPassword = new javax.swing.JPasswordField();
        separatorConfirmPassword = new javax.swing.JSeparator();
        labelSecurityQuestion = new javax.swing.JLabel();
        comboSecurityQuestion = new javax.swing.JComboBox<>();
        labelSecurityAnswer = new javax.swing.JLabel();
        fieldSecurityAnswer = new javax.swing.JTextField();
        separatorSecurityAnswer = new javax.swing.JSeparator();
        btnForgotPassword = new javax.swing.JButton();
        btnLogInProceed = new javax.swing.JButton();
        btnCreateAccount = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Account");
        setAlwaysOnTop(true);
        setUndecorated(true);

        panelHeader.setBackground(new java.awt.Color(0, 0, 0));
        panelHeader.setOpaque(false);

        labelTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        labelTitle.setForeground(new java.awt.Color(255, 255, 255));
        labelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelTitle.setText("Account Sign Up");

        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/project/gfx/interface/cross.png"))); // NOI18N
        btnClose.setBorder(null);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnClose)
                .addContainerGap())
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClose)
                    .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnLogIn.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        btnLogIn.setForeground(new java.awt.Color(255, 255, 255));
        btnLogIn.setText("Log In");
        btnLogIn.setContentAreaFilled(false);
        btnLogIn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogInActionPerformed(evt);
            }
        });

        btnSignUp.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        btnSignUp.setForeground(new java.awt.Color(255, 255, 255));
        btnSignUp.setText("Sign Up");
        btnSignUp.setContentAreaFilled(false);
        btnSignUp.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSignUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSignUpActionPerformed(evt);
            }
        });

        labelUsername.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        labelUsername.setForeground(new java.awt.Color(255, 255, 255));
        labelUsername.setText("Username");

        fieldUsername.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldUsername.setForeground(new java.awt.Color(153, 153, 153));
        fieldUsername.setText("Enter Username");
        fieldUsername.setBorder(null);
        fieldUsername.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldUsername.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldUsernameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldUsernameFocusLost(evt);
            }
        });

        labelPassword.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        labelPassword.setForeground(new java.awt.Color(255, 255, 255));
        labelPassword.setText("Password");

        fieldPassword.setForeground(new java.awt.Color(153, 153, 153));
        fieldPassword.setText("Enter Password");
        fieldPassword.setBorder(null);
        fieldPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldPasswordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldPasswordFocusLost(evt);
            }
        });

        labelConfirmPassword.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        labelConfirmPassword.setForeground(new java.awt.Color(255, 255, 255));
        labelConfirmPassword.setText("Confirm Password");

        fieldConfirmPassword.setForeground(new java.awt.Color(153, 153, 153));
        fieldConfirmPassword.setText("Enter Password");
        fieldConfirmPassword.setBorder(null);
        fieldConfirmPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldConfirmPasswordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldConfirmPasswordFocusLost(evt);
            }
        });

        labelSecurityQuestion.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        labelSecurityQuestion.setForeground(new java.awt.Color(255, 255, 255));
        labelSecurityQuestion.setText("Security Question");

        comboSecurityQuestion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "What was your dream job as a child?", "What is the name of the first book you ever read?", "What was your favorite subject in high school?", "What was the first concert you ever attended?", "What is the first song you learned all the lyrics to?" }));
        comboSecurityQuestion.setBorder(null);

        labelSecurityAnswer.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N
        labelSecurityAnswer.setForeground(new java.awt.Color(255, 255, 255));
        labelSecurityAnswer.setText("Security Answer");

        fieldSecurityAnswer.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        fieldSecurityAnswer.setForeground(new java.awt.Color(153, 153, 153));
        fieldSecurityAnswer.setText("Enter Answer");
        fieldSecurityAnswer.setToolTipText("");
        fieldSecurityAnswer.setBorder(null);
        fieldSecurityAnswer.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldSecurityAnswer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldSecurityAnswerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldSecurityAnswerFocusLost(evt);
            }
        });

        btnForgotPassword.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Yellow"));
        btnForgotPassword.setText("Forgot Password");
        btnForgotPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnForgotPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnForgotPasswordActionPerformed(evt);
            }
        });

        btnLogInProceed.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Blue"));
        btnLogInProceed.setText("Log In");
        btnLogInProceed.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogInProceed.setEnabled(false);
        btnLogInProceed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogInProceedActionPerformed(evt);
            }
        });

        btnCreateAccount.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Blue"));
        btnCreateAccount.setText("Create Account and Log In");
        btnCreateAccount.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCreateAccount.setEnabled(false);
        btnCreateAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateAccountActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLogInLayout = new javax.swing.GroupLayout(panelLogIn);
        panelLogIn.setLayout(panelLogInLayout);
        panelLogInLayout.setHorizontalGroup(
            panelLogInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelLogInLayout.createSequentialGroup()
                .addGroup(panelLogInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLogInLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnForgotPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLogInProceed, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelLogInLayout.createSequentialGroup()
                        .addGroup(panelLogInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(btnLogIn, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(btnSignUp, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(labelUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(fieldUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(separatorUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(labelPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(fieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(separatorPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(labelConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(fieldConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(separatorConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(labelSecurityQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(comboSecurityQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(labelSecurityAnswer, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(fieldSecurityAnswer, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLogInLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(separatorSecurityAnswer, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 6, Short.MAX_VALUE))
            .addGroup(panelLogInLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnCreateAccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelLogInLayout.setVerticalGroup(
            panelLogInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogInLayout.createSequentialGroup()
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(panelLogInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogIn, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSignUp, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(labelUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(fieldUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(separatorUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(labelPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(fieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(separatorPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(labelConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(fieldConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(separatorConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(labelSecurityQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(comboSecurityQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(labelSecurityAnswer, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(fieldSecurityAnswer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(separatorSecurityAnswer, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLogInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnForgotPassword)
                    .addComponent(btnLogInProceed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCreateAccount))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLogIn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLogIn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnLogInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogInActionPerformed
        signup_selected = false;
        WindowUtils.setBtnIcon(btnLogIn, login_button_address);
        WindowUtils.resetBtnIcon(btnSignUp);

        loginFieldsRefresh();
    }//GEN-LAST:event_btnLogInActionPerformed

    private void btnSignUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSignUpActionPerformed
        signup_selected = true;
        WindowUtils.setBtnIcon(btnSignUp, login_button_address);
        WindowUtils.resetBtnIcon(btnLogIn);

        loginFieldsRefresh();
    }//GEN-LAST:event_btnSignUpActionPerformed

    private void fieldUsernameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldUsernameFocusGained
        if (fieldUsername.getText().equals("Enter Username")) {
            fieldUsername.setText("");
            fieldUsername.setForeground(new Color(255, 255, 255));
        }
    }//GEN-LAST:event_fieldUsernameFocusGained

    private void fieldUsernameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldUsernameFocusLost
        if (fieldUsername.getText().equals("")) {
            fieldUsername.setText("Enter Username");
            fieldUsername.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldUsernameFocusLost

    private void fieldSecurityAnswerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldSecurityAnswerFocusGained
        if (fieldSecurityAnswer.getText().equals("Enter Answer")) {
            fieldSecurityAnswer.setText("");
            fieldSecurityAnswer.setForeground(new Color(255, 255, 255));
        }
    }//GEN-LAST:event_fieldSecurityAnswerFocusGained

    private void fieldSecurityAnswerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldSecurityAnswerFocusLost
        if (fieldSecurityAnswer.getText().equals("")) {
            fieldSecurityAnswer.setText("Enter Answer");
            fieldSecurityAnswer.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldSecurityAnswerFocusLost

    private void btnForgotPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForgotPasswordActionPerformed
        new ForgotPassword(main).setVisible(true);
        dispose();
    }//GEN-LAST:event_btnForgotPasswordActionPerformed

    private void btnLogInProceedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogInProceedActionPerformed
        logInAccount();
    }//GEN-LAST:event_btnLogInProceedActionPerformed

    private void btnCreateAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateAccountActionPerformed
        createAccount();
    }//GEN-LAST:event_btnCreateAccountActionPerformed

    private void fieldConfirmPasswordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldConfirmPasswordFocusLost
        if (fieldConfirmPassword.getText().equals("")) {
            fieldConfirmPassword.setText("Enter Password");
            fieldConfirmPassword.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldConfirmPasswordFocusLost

    private void fieldConfirmPasswordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldConfirmPasswordFocusGained
        if (fieldConfirmPassword.getText().equals("Enter Password")) {
            fieldConfirmPassword.setText("");
            fieldConfirmPassword.setForeground(new Color(255, 255, 255));
        }
    }//GEN-LAST:event_fieldConfirmPasswordFocusGained

    private void fieldPasswordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPasswordFocusLost
        if (fieldPassword.getText().equals("")) {
            fieldPassword.setText("Enter Password");
            fieldPassword.setForeground(new Color(153, 153, 153));
        }
    }//GEN-LAST:event_fieldPasswordFocusLost

    private void fieldPasswordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPasswordFocusGained
        if (fieldPassword.getText().equals("Enter Password")) {
            fieldPassword.setText("");
            fieldPassword.setForeground(new Color(255, 255, 255));
        }
    }//GEN-LAST:event_fieldPasswordFocusGained

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(LogIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LogIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LogIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LogIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCreateAccount;
    private javax.swing.JButton btnForgotPassword;
    private javax.swing.JButton btnLogIn;
    private javax.swing.JButton btnLogInProceed;
    private javax.swing.JButton btnSignUp;
    private javax.swing.JComboBox<String> comboSecurityQuestion;
    private javax.swing.JPasswordField fieldConfirmPassword;
    private javax.swing.JPasswordField fieldPassword;
    private javax.swing.JTextField fieldSecurityAnswer;
    private javax.swing.JTextField fieldUsername;
    private javax.swing.JLabel labelConfirmPassword;
    private javax.swing.JLabel labelPassword;
    private javax.swing.JLabel labelSecurityAnswer;
    private javax.swing.JLabel labelSecurityQuestion;
    private javax.swing.JLabel labelTitle;
    private javax.swing.JLabel labelUsername;
    private javax.swing.JPanel panelHeader;
    private project.component.GradientPanel panelLogIn;
    private javax.swing.JSeparator separatorConfirmPassword;
    private javax.swing.JSeparator separatorPassword;
    private javax.swing.JSeparator separatorSecurityAnswer;
    private javax.swing.JSeparator separatorUsername;
    // End of variables declaration//GEN-END:variables
}
