package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WindowUtils {
    
    public enum FieldFocus {
        GAINED, LOST
    };

    public static void setTransparentFrame(JComponent component) {
        component.setBackground(new Color(0, 0, 0, 0));
    }

    public static void setTransparentFrame(JComponent... components) {
        for (JComponent component : components) {
            component.setBackground(new Color(0, 0, 0, 0));
        }
    }

    public static void setDefaultField(JTextField text_field, String default_text, FieldFocus field_focus, Color color) {
        if (field_focus == FieldFocus.GAINED) {
            if (text_field.getText().equals(default_text)) {
                text_field.setText("");
                text_field.setForeground(color);
            }
        } else if (field_focus == FieldFocus.LOST) {
            if (text_field.getText().equals("")) {
                text_field.setText(default_text);
                text_field.setForeground(new Color(153, 153, 153));
            }
        }
    }

    public static void setBtnIcon(JButton button, String location) {
        java.net.URL imgURL = WindowUtils.class.getResource(location);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            button.setIcon(icon);
        } else {
            System.err.println("Error: Image not found at " + location);
        }

        button.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    public static void resetBtnIcon(JButton button) {
        // Create a 1x1 transparent image
        ImageIcon transparentIcon = new ImageIcon(new ImageIcon(new byte[]{}).getImage().getScaledInstance(1, 1, Image.SCALE_DEFAULT));

        // Set the transparent icon to the button
        button.setIcon(transparentIcon);
    }

    public static void enforceDigits(KeyEvent evt) {
        char c = evt.getKeyChar();

        if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
            evt.consume();
        }
    }

    /**
     * Initializes the dragging behavior for a JFrame using a JLabel as the drag
     * handle.
     *
     * @param frame The JFrame to be moved
     * @param panel The JLabel that will act as the drag handle
     */
    public static void initMoving(JFrame frame, JPanel panel) {
        // Initialize the variables to store the position of the mouse
        int[] x = {0};
        int[] y = {0};

        // Add MouseListener to labelTitle to capture initial mouse press position
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                // Store the mouse position relative to the labelTitle
                x[0] = me.getX();
                y[0] = me.getY();
            }
        });

        // Add MouseMotionListener to labelTitle to move the frame when dragging
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                // Calculate the new position of the frame based on mouse movement
                int newX = me.getXOnScreen() - x[0];
                int newY = me.getYOnScreen() - y[0];

                // Get screen dimensions
                int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
                int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

                // Get the frame's dimensions
                int frameWidth = frame.getWidth();
                int frameHeight = frame.getHeight();

                // Prevent the frame from being dragged off-screen
                newX = Math.max(0, Math.min(newX, screenWidth - frameWidth));
                newY = Math.max(0, Math.min(newY, screenHeight - frameHeight));

                // Set the new position of the frame
                frame.setLocation(newX, newY);
            }
        });
    }
}
