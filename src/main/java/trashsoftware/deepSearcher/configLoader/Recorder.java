package trashsoftware.deepSearcher.configLoader;

import trashsoftware.deepSearcher.itemUnits.FileRoot;
import trashsoftware.deepSearcher.itemUnits.SearchHistoryItem;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public abstract class Recorder {

    private final static String defaultDateFormat = "yyyy-MM-dd HH:mm:ss";

    private static SimpleDateFormat formatter = new SimpleDateFormat(defaultDateFormat);

    private final static String recordFileName = ConfigLoader.searchHistoryFilePath;


    /**
     * Returns an ArrayList containing SearchHistoryItem objects of all search records.
     *
     * @return all search history records.
     * @throws IOException if the record file cannot be read.
     */
    public static ArrayList<SearchHistoryItem> readSearchHistory() throws IOException {
        String text = ConfigLoader.readAllFromText(recordFileName);
        ArrayList<SearchHistoryItem> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i += 8) {
            if (!lines[i].equals("==========")) {
                return list;
            }
            String content = lines[i + 1];
            String directoryPath = lines[i + 2];
            File directory;
            if (directoryPath.length() == 0) {
                directory = new FileRoot();
            } else {
                directory = new File(directoryPath);
            }
            String[] prefs = lines[i + 3].split(" ");
            boolean searchFile = Boolean.valueOf(prefs[0]);
            boolean searchDir = Boolean.valueOf(prefs[1]);
            boolean searchCont = Boolean.valueOf(prefs[2]);
            boolean caseSense = Boolean.valueOf(prefs[3]);
            boolean notSearchExt = Boolean.valueOf(prefs[4]);
            boolean showHidden = Boolean.valueOf(prefs[5]);
            String[] ext = lines[i + 4].split(" ");
            ArrayList<String> extensions = new ArrayList<>();
            Collections.addAll(extensions, ext);
            String sep = lines[i + 5];
            String dirSep = lines[i + 6];

            try {
                Date recordTime = formatter.parse(lines[i + 7]);
                SearchHistoryItem shi = new SearchHistoryItem(content, directory, searchFile, searchDir, searchCont,
                        caseSense, notSearchExt, showHidden, extensions, sep, dirSep);
                shi.setRecordTime(recordTime);
                list.add(shi);
            } catch (ParseException pe) {
                list.clear();
                //
            }

        }

        return list;
    }


    /**
     * Records a new search action saved in a SearchHistoryItem object to the record file.
     *
     * @param shi the SearchHistoryItem object of a search action.
     * @throws IOException if the record file is not readable or is not writable.
     */
    public static void recordSearch(SearchHistoryItem shi) throws IOException {
        String orig = ConfigLoader.readAllFromText(recordFileName);
        String toWrite = orig + shi.getWritable();
        ConfigLoader.writeFile(recordFileName, toWrite);
    }
}
