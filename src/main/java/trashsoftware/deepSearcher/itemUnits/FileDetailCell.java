package trashsoftware.deepSearcher.itemUnits;

import java.io.File;

public class FileDetailCell extends Cell {

    private String path;

    public FileDetailCell(String path, String type) {
        super(path, type);
        this.path = path;
    }


    @Override
    public String getName1() {
        String name = new File(path).getName();
        if (name.length() == 0) {
            return path;
        } else {
            return name;
        }
    }


    public String getFullPath() {
        return path;
    }
}
