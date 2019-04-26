package trashsoftware.deepSearcher.itemUnits;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchHistoryItem {

    private String content;

    private File directory;

    private boolean searchFile;

    private boolean searchDir;

    private boolean searchCont;

    private boolean caseSense;

    private boolean notSearchExtensions;

    private boolean showHidden;

    private ArrayList<String> extensions;

    private String sep;

    private String dirSep;

    private String defaultDateFormat = "yyyy-MM-dd HH:mm:ss";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat(defaultDateFormat);

    private Date recordTime = new Date();


    /**
     * Constructor of a new SearchHistoryItem.
     *
     * @param content             the searching text.
     * @param directory           the start directory.
     * @param searchFile          whether to search file names.
     * @param searchDir           whether to search directory names.
     * @param searchCont          whether to search files' contents.
     * @param caseSense           is case-sensitive or not.
     * @param notSearchExtensions whether not to search files' extensions.
     * @param showHidden          whether to show hidden files.
     * @param ext                 extensions of files to open and search content.
     * @param separator           the and-separator.
     * @param dirSep              the directory separator.
     */
    public SearchHistoryItem(String content, File directory, boolean searchFile, boolean searchDir,
                             boolean searchCont, boolean caseSense, boolean notSearchExtensions,
                             boolean showHidden, ArrayList<String> ext, String separator, String dirSep) {
        this.content = content;
        this.directory = directory;
        this.searchFile = searchFile;
        this.searchDir = searchDir;
        this.searchCont = searchCont;
        this.caseSense = caseSense;
        this.extensions = ext;
        this.sep = separator;
        this.notSearchExtensions = notSearchExtensions;
        this.showHidden = showHidden;
        this.dirSep = dirSep;

    }


    /**
     * Sets up the record time while rebuilding.
     *
     * @param recordTime the recorded time.
     */
    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }


    /**
     * Returns a formatted string recorded all information of this SearchHistoryItem object.
     *
     * @return a formatted string of this SearchHistoryItem.
     */
    public String getWritable() {
        StringBuilder sb = new StringBuilder();
        sb.append("==========").append("\n");
        sb.append(content).append("\n");
        sb.append(directory.getAbsolutePath()).append("\n");
        sb.append(searchFile).append(" ").append(searchDir).append(" ").append(searchCont).append(" ").
                append(caseSense).append(" ").append(notSearchExtensions).append(" ").append(showHidden).append("\n");
        for (String e : extensions) {
            sb.append(e).append(" ");
        }
        sb.append("\n");
        sb.append(sep).append("\n");
        sb.append(dirSep).append("\n");
        String date = dateFormatter.format(recordTime);
        sb.append(date).append("\n");

        return sb.toString();
    }


    /**
     * Returns the recorded searching text.
     *
     * @return the searching text.
     */
    public String getContent() {
        return content;
    }


    /**
     * Returns the recorded root directory.
     *
     * @return the root directory.
     */
    public File getDirectory() {
        return directory;
    }


    /**
     * Returns the record time.
     *
     * @return the record time.
     */
    public Date getRecordTime() {
        return recordTime;
    }


    /**
     * Returns the recorded and-separator.
     *
     * @return the and-separator..
     */
    public String getSep() {
        return sep;
    }


    /**
     * Returns the recorded directory-separator.
     *
     * @return the directory-separator..
     */
    public String getDirSep() {
        return dirSep;
    }


    /**
     * Returns all recorded extensions that used to be searched file contents.
     *
     * @return the extensions.
     */
    public ArrayList<String> getExtensions() {
        return extensions;
    }


    /**
     * Returns whether the recorded search action is case-sensitive.
     *
     * @return whether is case-sensitive or not.
     */
    public boolean isCaseSense() {
        return caseSense;
    }


    /**
     * Returns whether the recorded search contains file names.
     *
     * @return whether the record contained file names or not.
     */
    public boolean isSearchFile() {
        return searchFile;
    }


    /**
     * Returns whether the recorded search contains file contents.
     *
     * @return whether the record contained file contents or not.
     */
    public boolean isSearchCont() {
        return searchCont;
    }


    /**
     * Returns whether the recorded search contains directory names.
     *
     * @return whether the record contained directory names or not.
     */
    public boolean isSearchDir() {
        return searchDir;
    }


    /**
     * Returns whether not the recorded search contains file extensions.
     *
     * @return whether the record did not contain file extensions.
     */
    public boolean isNotSearchExtensions() {
        return notSearchExtensions;
    }


    /**
     * Returns whether not the recorded search showed hidden files.
     *
     * @return whether the record showed hidden files.
     */
    public boolean isShowingHidden() {
        return showHidden;
    }

    /**
     * Returns a readable string to display on the search history menu list.
     *
     * @return a readable string representing this SearchHistoryItem object.
     */
    @Override
    public String toString() {
        return content;
    }

}
