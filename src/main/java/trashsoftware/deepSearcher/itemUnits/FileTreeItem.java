package trashsoftware.deepSearcher.itemUnits;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<File> {

    private boolean isLeaf;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeaf = true;


    /**
     * The constructor of a new FileTreeItem object.
     *
     * @param file the content of the FileTreeItem object.
     */
    public FileTreeItem(File file) {
        super(file);
    }


    /**
     * Returns the children of this FileTreeItem object.
     * <p>
     * Overrides the "getChildren" method of the TreeItem<> class.
     *
     * @return the children of this FileTreeItem object.
     */
    @Override
    public ObservableList<TreeItem<File>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }


    /**
     * Returns whether this FileTreeItem object does not contain children..
     *
     * @return true if this is a leaf.
     */
    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            File f = getValue();
            isLeaf = f.isFile();
        }
        return isLeaf;
    }


    /**
     * Builds up children nodes when the given FileTreeItem object is expanded.
     * <p>
     * This method returns an ObservableList object containing FileTreeItem objects of all children that the
     * representing directory of the "TreeItem" has.
     *
     * @param TreeItem the FileTreeItem object which is being expanded.
     * @return children of this FileTreeItem object, if any.
     */
    private static ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
        File f = TreeItem.getValue();
        if (f != null && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                for (File childFile : files) {
                    if (childFile.isDirectory()) {
                        children.add(new FileTreeItem(new SpecialFile(childFile.getAbsolutePath())));
                    }
                }
                return children;
            }
        }
        return FXCollections.emptyObservableList();
    }
}