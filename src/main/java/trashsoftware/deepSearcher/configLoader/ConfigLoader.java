package trashsoftware.deepSearcher.configLoader;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public abstract class ConfigLoader {

    private final static String configFilePath = "UserSettings" + File.separator + "pref.ini";

    private final static String excludeDirFilePath = "UserSettings" + File.separator + "excludeDirectories.ini";

    private final static String excludeFormatFilePath = "UserSettings" + File.separator + "excludeFormats.ini";

    public final static String searchHistoryFilePath = "UserSettings" + File.separator + "searchHistory.txt";

    public static void main(String[] args) {
//        System.out.println(getConfig());
    }


    /**
     * Check the existence of all config and record files.
     * <p>
     * Create new if any of them does not exist.
     */
    public static void checkFiles() {
        try {

            // Check pref.ini
            File pref = new File(configFilePath);
            if (!pref.exists()) {
                boolean suc = pref.createNewFile();
                if (!suc) {
                    throw new IOException();
                }
                setConfig("language", "chs");
                setConfig("case_sen", "false");
                setConfig("keep_orig", "true");
                setConfig("scale", "125");
                setConfig("override_font", "false");
                setConfig("UserSettings/theme", "default");
                setConfig("and_sep", "&");
                setConfig("not_ext", "true");
                setConfig("dir_sep", "@");
                setConfig("show_hidden", "false");
                setConfig("custom_chooser", "true");
            }

            // Check excludeDirectories.ini
            File exDir = new File(excludeDirFilePath);
            if (!exDir.exists()) {
                boolean suc = exDir.createNewFile();
                if (!suc) {
                    throw new IOException();
                }
            }

            // Check excludeFormats.ini
            File exFormat = new File(excludeFormatFilePath);
            if (!exFormat.exists()) {
                boolean suc = exFormat.createNewFile();
                if (!suc) {
                    throw new IOException();
                }
            }

            // Check searchHistory.txt
            File seHis = new File(searchHistoryFilePath);
            if (!seHis.exists()) {
                boolean suc = seHis.createNewFile();
                if (!suc) {
                    throw new IOException();
                }
            }

        } catch (IOException ioe) {
            //
        }
    }


    /**
     * Return a HashMap object containing all current configs read from config file.
     *
     * @return current configs.
     * @throws IOException if the config file cannot be read.
     */
    public static HashMap<String, String> getConfig() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        File configFile = new File(configFilePath);
        InputStreamReader reader = new InputStreamReader(
                new FileInputStream(configFile));
        BufferedReader br = new BufferedReader(reader);

        String line = br.readLine();
        while (line != null) {
            if (line.contains("=")) {
                String[] lineData = line.split("=");
                map.put(lineData[0], lineData[1]);
            }
            line = br.readLine();
        }

        br.close();

        return map;

    }


    /**
     * Set a config to the config file.
     *
     * @param key   the config key.
     * @param value the config value.
     * @throws IOException if the config file cannot be read or cannot be written.
     */
    public static void setConfig(String key, String value) throws IOException {
        HashMap<String, String> map = getConfig();
        map.put(key, value);
        StringBuilder sb = new StringBuilder();
        for (String k : map.keySet()) {
            sb.append(k).append("=").append(map.get(k)).append("\n");
        }
        File configFile = new File(configFilePath);

        writeFile(configFile.getAbsolutePath(), sb.toString());

    }


    /**
     * Return a String object containing all content of the given text file.
     *
     * @param fileName the full path or relative path of the text file which will be read.
     * @return all content of the tet file.
     * @throws IOException if the file does not exist or cannot be read.
     */
    public static String readAllFromText(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        File configFile = new File(fileName);
        InputStreamReader reader = new InputStreamReader(
                new FileInputStream(configFile));
        BufferedReader br = new BufferedReader(reader);

        String line = br.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = br.readLine();
        }

        br.close();

        return sb.toString();
    }


    /**
     * Return an ArrayList containing all directory paths that will not be searched.
     *
     * @return all excluded directories.
     */
    public static List<String> getExcludeDirs() {
        return readListFromFile(excludeDirFilePath);
    }


    /**
     * Add a new excluded directory.
     *
     * @param dirName the full path of the new excluded directory.
     * @throws IOException if the directory record file cannot be read of written.
     */
    public static void addExcludeDir(String dirName) throws IOException {
        List<String> current = getExcludeDirs();
        if (!current.contains(dirName)) {
            current.add(dirName);
        }

        StringBuilder sb = new StringBuilder();
        for (String line : current) {
            sb.append(line).append("\n");
        }

        writeFile(excludeDirFilePath, sb.toString());

    }


    /**
     * Remove a directory from the excluded directory list.
     *
     * @param dirName the full path of the directory which will be removed.
     * @throws IOException if the directory record file cannot be read of written.
     */
    public static void removeExcludeDir(String dirName) throws IOException {
        List<String> current = getExcludeDirs();
        current.remove(dirName);

        StringBuilder sb = new StringBuilder();
        for (String line : current) {
            sb.append(line).append("\n");
        }

        writeFile(excludeDirFilePath, sb.toString());

    }


    /**
     * Return an ArrayList containing all formats' extensions that will not be searched.
     *
     * @return all excluded extensions.
     */
    public static List<String> getExclusionFormats() {
        return readListFromFile(excludeFormatFilePath);
    }


    private static List<String> readListFromFile(String filePath) {
        ArrayList<String> list = new ArrayList<>();
        File configFile = new File(filePath);

        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(configFile));
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }

            br.close();
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }

        return list;
    }


    /**
     * Add a new excluded format.
     *
     * @param format the extension of the new excluded format.
     * @throws IOException if the format record file cannot be read of written.
     */
    public static void addExcludeFormat(String format) throws IOException {
        List<String> current = getExclusionFormats();
        if (!current.contains(format.toLowerCase())) {
            current.add(format.toLowerCase());
        }

        StringBuilder sb = new StringBuilder();
        for (String line : current) {
            sb.append(line).append("\n");
        }

        writeFile(excludeFormatFilePath, sb.toString());

    }


    /**
     * Remove a format from the excluded format list.
     *
     * @param format the extension of the format which will be removed.
     * @throws IOException if the format record file cannot be read of written.
     */
    public static void removeExcludeFormat(String format) throws IOException {
        List<String> current = getExclusionFormats();
        current.remove(format);

        StringBuilder sb = new StringBuilder();
        for (String line : current) {
            sb.append(line).append("\n");
        }

        writeFile(excludeFormatFilePath, sb.toString());

    }


    /**
     * Write String "content" to the given file.
     * <p>
     * The original content of file will be cleared.
     *
     * @param fileName file's name or pull path.
     * @param content  String to write.
     * @throws IOException if file is not writable.
     */
    public static void writeFile(String fileName, String content) throws IOException {
        File configFile = new File(fileName);
        BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
        out.write(content);
        out.flush();
        out.close();
    }


    /**
     * Delete the file.
     *
     * @param fileName the name or the full path of the file which will be deleted.
     * @throws IOException if the deleting is not successful.
     */
    public static void deleteFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            boolean suc = file.delete();
            if (!suc) {
                throw new IOException();
            }
        }
    }

    public static String readTextFromResource(InputStream resource) {
        String text = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(resource));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            br.close();
            text = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }

}
