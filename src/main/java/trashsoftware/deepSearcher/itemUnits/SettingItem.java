package trashsoftware.deepSearcher.itemUnits;

import javafx.scene.layout.Pane;

public class SettingItem {

    private String name;

    private Pane pane;


    /**
     * The constructor of a SettingItem object which has a Pane object.
     *
     * @param name the name of the SettingItem.
     * @param pane the Pane object of this SettingItem.
     */
    public SettingItem(String name, Pane pane) {
        this.name = name;
        this.pane = pane;
    }


    /**
     * The constructor of a SettingItem object which does not have a Pane object.
     *
     * @param name the name of the SettingItem.
     */
    public SettingItem(String name) {
        this.name = name;
    }


    /**
     * Returns whether this SettingItem object has a Pane object.
     *
     * @return whether this SettingItem has a Pane object and the pane can be shown on the screen.
     */
    public boolean showAble() {
        return pane != null;
    }


    /**
     * Returns a readable String representing this SettingItem object to the user.
     *
     * @return a readable String representing this SettingItem object.
     */
    @Override
    public String toString() {
        return name;
    }


    /**
     * Returns the Pane object of this SettingItem.
     *
     * @return the Pane object of this SettingItem.
     */
    public Pane getPane() {
        return pane;
    }
}
