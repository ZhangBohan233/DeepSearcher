package trashsoftware.deepSearcher.itemUnits;

import javafx.scene.control.CheckBox;

public class FormatCell extends Cell {

    private CheckBox box = new CheckBox();


    /**
     * The constructor of a FormatCell object.
     *
     * @param extension   the format's extension.
     * @param description the descriptive text of this format.
     */
    public FormatCell(String extension, String description) {
        super(extension, description);
    }


    /**
     * Returns the CheckBox object storing in this FormatCell.
     *
     * @return the CheckBox on the TableView formatTable.
     */
    public CheckBox getBox() {
        return box;
    }

}
