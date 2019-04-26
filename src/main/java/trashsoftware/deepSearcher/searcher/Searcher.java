package trashsoftware.deepSearcher.searcher;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.ObservableList;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;
import trashsoftware.deepSearcher.itemUnits.FileCell;
import trashsoftware.deepSearcher.itemUnits.TemporarySearchUnit;
import trashsoftware.deepSearcher.util.HelperFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Searcher {

    private boolean searchContent;

    private ArrayList<String> extensions;

    private boolean searchDir;

    private boolean searchFile;

    private boolean notSearchExt;

    private boolean showHidden;

    private boolean caseSensitive;

    private String target;

    private File startDir;

    private ObservableList<FileCell> tableList;

    private List<String> excludeDirs = ConfigLoader.getExcludeDirs();

    private List<String> excludeFormats = ConfigLoader.getExclusionFormats();

    private LanguageLoader lanLoader;

    private String andSep;

    private String dirSep;

    private String[] allTargets;

    private String pathDir;

    private Thread thisThread;

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();


    /**
     * Constructor of a searcher.
     *
     * @param searchThread the parent thread of this search action.
     */
    public Searcher(Thread searchThread) {
        this.thisThread = searchThread;
    }


    /**
     * Sets up searching area and content.
     * <p>
     * This method should be called after all other settings were done.
     *
     * @param startDir the root directory of search.
     * @param targets  the contents for searching.
     */
    public void setSearch(File startDir, String targets) {

        String front = targets;

        // The front is searching content, back is directory, if directory separator is in targets.
        if (targets.contains(dirSep)) {
            String[] a = targets.split(Pattern.quote(dirSep));
            front = a[0];
            if (a.length > 1) {
                pathDir = HelperFunctions.stripSpaces(a[1]);
                if (pathDir.length() == 0) {
                    pathDir = null;
                } else if (!caseSensitive) {
                    pathDir = pathDir.toLowerCase();
                }
            }
        }


        String[] all = front.split(Pattern.quote(andSep));
        allTargets = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            allTargets[i] = HelperFunctions.stripSpaces(all[i]);
        }
        this.target = allTargets[0];

        // Not case sensitive
        if (!caseSensitive) {
            this.target = target.toLowerCase();
        }

        this.startDir = startDir;
    }


    /**
     * Sets up the LanguageLoader object uses to create showing language on the resulting FileCell Objects.
     *
     * @param lanLoader a LanguageLoader object to show languages on GUI.
     */
    public void setLanLoader(LanguageLoader lanLoader) {
        this.lanLoader = lanLoader;
    }


    /**
     * Sets up the ObservableList Object to collect the search result.
     *
     * @param list the ObservableList Object to collect the search result.
     */
    public void setTableList(ObservableList<FileCell> list) {
        this.tableList = list;
    }


    /**
     * Recursively searches all files and directories under the root directory.
     *
     * @param dir the root directory.
     */
    private void search(File dir) {

        if (thisThread.isInterrupted()) {
            return;
        }

        String dirName = dir.getName();

        // Not case sensitive
        if (!caseSensitive) {
            dirName = dirName.toLowerCase();
        }

        // If it is a directory
        if (dir.isDirectory()) {
            if (excludeDirs.contains(dir.getAbsolutePath())) {
                return;
            }
            if (searchDir && (showHidden || !dir.isHidden())) {
                if (pathDir == null || isUnderDir(dir)) {
                    // Send to compare

                    compareName(dirName, dir);


                }
            }

            // Recursion part
            File[] subFiles = dir.listFiles();
            if (subFiles == null) {
                return;
            }
            for (File d : subFiles) {
                search(d);
            }
        } else {

            // Check if this file is hidden
            if (!showHidden && dir.isHidden()) {
                return;
            }

            // Check if the file is under the directory that the user want or the user does not regulate the
            // directory.
            if (pathDir != null && !isUnderDir(dir)) {
                return;
            }

            // Not searching extensions
            if (notSearchExt) {
                if (dirName.contains(".")) {
                    dirName = dirName.substring(0, dirName.lastIndexOf("."));
                }
            }

            // Search file names
            if (searchFile) {
                if (dir.getAbsolutePath().contains(".")) {
                    String ext1 = dir.getAbsolutePath().substring(dir.getAbsolutePath().lastIndexOf("."));
                    if (excludeFormats.contains(ext1.toLowerCase())) {
                        return;
                    }
                }

                compareName(dirName, dir);


            }

            // Search file content
            if (searchContent) {
                for (String ext : extensions) {
                    if (dir.getAbsolutePath().endsWith(ext)) {
                        searchContent(dir, ext);
                    }
                }
            }
        }

    }


    /**
     * Checks if the name of the file or directory contains some of the target.
     * <p>
     * If the name contains all targets, this method will directly add this file to the GUI. For names that matches
     * some of targets, they will be sent to search content if the searchContent is true. Otherwise, do noting.
     *
     * @param fileOrDirName the name of the file or directory.
     * @param dir           the File object of the file or directory.
     */
    private void compareName(String fileOrDirName, File dir) {
        if (fileOrDirName.contains(target)) {
            TemporarySearchUnit tsu = new TemporarySearchUnit(fileOrDirName, allTargets);
            if (tsu.isSatisfied()) {
                FileCell show = new FileCell(dir.getAbsolutePath(), "n");
                if (notInResult(show)) {
                    show.setLanguageLoader(lanLoader);
                    tableList.add(show);
                    progress.set(tableList.size());
                }
            } else if (searchContent) {
                String[] unSatisfies = tsu.getUnSatisfies();
                for (String ext : extensions) {
                    if (dir.getAbsolutePath().endsWith(ext)) {
                        searchContent(dir, ext, unSatisfies);
                    }
                }
            }
        }
    }


    /**
     * Checks if the name of the file or directory contains all String objects in String Array "remaining".
     * <p>
     * If the name contains all targets, this method will directly add this file to the GUI. For names that matches
     * some of targets, they will not be sent back to compare their contents.
     * This method will only be called after a search in a file's content was done.
     *
     * @param fileName  the name of the file or directory.
     * @param dir       the File object of the file or directory.
     * @param remaining the un-satisfied targets from the first search.
     */
    private void compareName(String fileName, File dir, String[] remaining) {
        TemporarySearchUnit tsu = new TemporarySearchUnit(fileName, remaining);
        if (tsu.isSatisfied()) {
            FileCell show = new FileCell(dir.getAbsolutePath(), "b");
            if (notInResult(show)) {
                show.setLanguageLoader(lanLoader);
                tableList.add(show);
                progress.set(tableList.size());
            }
        }

    }


    /**
     * Goes inside the files which have given extensions to search their contents.
     *
     * @param file the target file.
     * @param ext  extension of the target file.
     */
    private void searchContent(File file, String ext) {
        FileReader fr = new FileReader(file, ext);
        String content = fr.read();

        if (content != null) {

            // Not case sensitive
            if (!caseSensitive) {
                content = content.toLowerCase();
            }

            TemporarySearchUnit tsu = new TemporarySearchUnit(content, allTargets);
            if (tsu.isSatisfied()) {
                FileCell show = new FileCell(file.getAbsolutePath(), "c");
                if (notInResult(show)) {
                    show.setLanguageLoader(lanLoader);
                    tableList.add(show);
                }
            } else if (searchFile) {
                String[] notSat = tsu.getUnSatisfies();
                compareName(file.getName(), file, notSat);
            }
        }
    }


    /**
     * Checks if the content of the file contains all String objects in String Array remaining.
     * <p>
     * This method will only be called after a search for file names was done. If boolean searchContent of this
     * search is true, after the method compareName(String fileOrDirName, File dir) found some but not all the
     * matches in their names, it will then call this method.
     *
     * @param file      the File object of the file to be search.
     * @param ext       the extension of this file.
     * @param remaining the un-satisfied targets from the first search.
     */
    private void searchContent(File file, String ext, String[] remaining) {
        FileReader fr = new FileReader(file, ext);
        String content = fr.read();

        if (content != null) {

            // Not case sensitive
            if (!caseSensitive) {
                content = content.toLowerCase();
            }

            TemporarySearchUnit tsu = new TemporarySearchUnit(content, remaining);
            if (tsu.isSatisfied()) {
                FileCell show = new FileCell(file.getAbsolutePath(), "b");
                if (notInResult(show)) {
                    show.setLanguageLoader(lanLoader);
                    tableList.add(show);
                }
            }
        }
    }


    /**
     * Returns whether the given File object is under the directory that contains the String that the
     * user has restricted.
     *
     * @param file the File object of the current scanning file.
     * @return true iff "file" is under the directory that contains the String that the user has restricted.
     */
    private boolean isUnderDir(File file) {
        String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
        if (!caseSensitive) {
            path = path.toLowerCase();
        }
        return path.contains(pathDir);
    }


    /**
     * Starts the searching thread.
     */
    public void startSearch() {
        search(startDir);
    }


    /**
     * Sets up whether this search will include file names.
     *
     * @param isSearchingFile whether to search file names.
     */
    public void setSearchFile(boolean isSearchingFile) {
        searchFile = isSearchingFile;
    }


    /**
     * Sets up whether to include directory names.
     *
     * @param searchDir whether to search directory names.
     */
    public void setSearchDir(boolean searchDir) {
        this.searchDir = searchDir;
    }


    /**
     * Sets up whether to go inside files and search their contents.
     *
     * @param searchContent whether to search files contents.
     */
    public void setSearchContent(boolean searchContent) {
        this.searchContent = searchContent;
    }


    /**
     * Sets up the files extensions to search inside.
     *
     * @param extensions files extensions.
     */
    public void setExtensions(ArrayList<String> extensions) {
        this.extensions = extensions;
    }


    /**
     * Sets up whether the search is case-sensitive.
     *
     * @param caseSensitive whether the search is case-sensitive.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    /**
     * Returns whether this file is already in the result TableView.
     * <p>
     * This method will change the showing mode of the file to "both" if this file is already in the result.
     *
     * @param cell the FileCell object of this file.
     * @return true iff the file is already in result.
     */
    private boolean notInResult(FileCell cell) {
        if (!tableList.isEmpty()) {
            FileCell fc = tableList.get(tableList.size() - 1);
//        for (FileCell fc : tableList) {
            if (fc.getFullPath().equals(cell.getFullPath())) {
                fc.setMode("b");
                return false;
            }
//        }
        }
        return true;
    }


    /**
     * Sets up the separator for this search action.
     *
     * @param separator the andSeparator.
     */
    public void setSeparator(String separator) {
        this.andSep = separator;
    }


    /**
     * Sets up the directory separator of this search.
     *
     * @param dirSep the directory separator.
     */
    public void setDirSep(String dirSep) {
        this.dirSep = dirSep;
    }


    /**
     * Sets up whether not to include file extensions in this search.
     *
     * @param notSearchExt whether not to include file extensions.
     */
    public void setNotSearchExt(boolean notSearchExt) {
        this.notSearchExt = notSearchExt;
    }


    /**
     * Sets up whether to show hidden files in this search action.
     *
     * @param showHidden whether to search hidden files.
     */
    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    /**
     * Returns the progress property of files counting.
     * <p>
     * Used to real-time update the Label of files counting.
     *
     * @return the progressProperty of files counting.
     */
    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

}
