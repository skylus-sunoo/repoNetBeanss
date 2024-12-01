
package project.swing;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JScrollBar;

public class ScrollbarCustom extends JScrollBar {

    public ScrollbarCustom() {
        setUI(new ModernScrollbar());
        setPreferredSize(new Dimension(8, 8));
        setForeground(new Color(48, 144, 216));
        setBackground(Color.WHITE);
    }
}
