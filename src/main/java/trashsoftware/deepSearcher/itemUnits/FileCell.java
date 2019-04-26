package trashsoftware.deepSearcher.itemUnits;

import trashsoftware.deepSearcher.configLoader.LanguageLoader;

import java.io.File;

public class FileCell extends Cell {

    private String fullPath;

    private String name;

    private String type;

    private String mode;

    private LanguageLoader lanLoader;

    private String ext;


    /**
     * Constructor of FileCell object.
     * <p>
     * Create a new cell to display the file's information in the TableView.
     *
     * @param path the absolute path of the file.
     * @param mode the display mode, "c" for content, "n" for file name, "b" for both.
     */
    public FileCell(String path, String mode) {
        super(path.substring(0, path.lastIndexOf(File.separator) + 1), mode);
        fullPath = path;
        File thisFile = new File(path);
        if (thisFile.isDirectory()) {
            type = "d";
        } else {
            type = "f";
            try {
                this.ext = path.substring(path.lastIndexOf(".") + 1).toUpperCase();
            } catch (StringIndexOutOfBoundsException e) {
                this.ext = "";
            }
        }
        this.name = thisFile.getName();
        this.mode = mode;
    }


    /**
     * Sets up the LanguageLoader object to display the text properly on screen.
     *
     * @param lanLoader the current LanguageLoader.
     */
    public void setLanguageLoader(LanguageLoader lanLoader) {
        this.lanLoader = lanLoader;
    }


    /**
     * Returns the file's name.
     * <p>
     * This method is used to show the "Name" column on the TableView fileTable.
     *
     * @return the file's name.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the file's type, in the language of the settled LanguageLoader.
     * <p>
     * This method is used to show the "Type" column on the TableView fileTable.
     *
     * @return the file's type in the language of current LanguageLoader.
     */
    public String getType() {
        if (type.equals("d")) {
            return lanLoader.show(3);
        } else {
            return ext + lanLoader.show(2);
        }
    }


    /**
     * Returns the absolute path of this file.
     *
     * @return the absolute path of file.
     */
    public String getFullPath() {
        return fullPath;
    }


    /**
     * Change the display mode.
     *
     * @param newMode the target mode.
     */
    public void setMode(String newMode) {
        mode = newMode;
    }


    /**
     * Returns the second name.
     * <p>
     * In this class this method returns the mode of this FileCell object in the language of LanguageLoader.
     *
     * @return the mode in the settled LanguageLoader's language.
     */
    @Override
    public String getName2() {
        switch (mode) {
            case "c":
                return lanLoader.show(8);
            case "n":
                return lanLoader.show(9);
            default:
                return lanLoader.show(8) + "/" + lanLoader.show(9);
        }
    }
}
