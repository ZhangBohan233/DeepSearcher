package trashsoftware.deepSearcher.itemUnits;

import java.io.File;

public class FileRoot extends File {


    /**
     * Constructor of a new FileRoot object.
     * <p>
     * This class represents the system root path of the Windows operating system.
     */
    public FileRoot() {
        super("root");
    }


    /**
     * Always return true since the root path always exists under Windows.
     *
     * @return true.
     */
    @Override
    public boolean exists() {
        return true;
    }


    /**
     * Always return true since it contains the Windows roots.
     *
     * @return true.
     */
    @Override
    public boolean isDirectory() {
        return true;
    }


    /**
     * Returns an empty String object since it does not have an absolute path.
     *
     * @return an empty String object.
     */
    @Override
    public String getAbsolutePath() {
        return "";
    }


    /**
     * Lists the Windows roots.
     *
     * @return a File Array of Windows roots.
     */
    @Override
    public File[] listFiles() {
        return File.listRoots();
    }
}
