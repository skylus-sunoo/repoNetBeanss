
package project.model;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Model_Menu {
    private String icon;
    private String name;
    private MenuType type;
    
    public static enum MenuType{TITLE, MENU, EMPTY}

    public Model_Menu(String name, String icon, MenuType type) {
        this.name = name;
        this.icon = icon;
        this.type = type;
    }

    public Model_Menu(String name, MenuType type) {
        this.name = name;
        this.type = type;
    }
    
    public Model_Menu(MenuType type) {
        this.type = type;
    }
    
    public Model_Menu() {
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }
    
    public Icon toIcon(){
        return new ImageIcon(getClass().getResource("/project/gfx/icons/" + icon + ".png"));
    }
}
