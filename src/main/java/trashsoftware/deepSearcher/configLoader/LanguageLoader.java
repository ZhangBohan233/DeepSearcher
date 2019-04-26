package trashsoftware.deepSearcher.configLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class LanguageLoader {

    private HashMap<Integer, String> lanMap = new HashMap<>();

    public static void main(String[] args) {
        LanguageLoader ll = new LanguageLoader();
        System.out.println(ll.show(0));
    }


    /**
     * The constructor of a LanguageLoader object.
     * <p>
     * Creates a new LanguageLoader object.
     */
    public LanguageLoader() {
        try {
            String language = ConfigLoader.getConfig().get("language");
            String lanFilePath = "resources" + File.separator + "languages" + File.separator + language + ".txt";

            File lanFile = new File(lanFilePath);
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(lanFile), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(reader);

            String line = br.readLine();
            while (line != null) {
                if (line.length() != 0 && line.charAt(0) != '#') {
                    String[] lineData = line.split("=");
                    lanMap.put(Integer.valueOf(lineData[0]), lineData[1].replaceAll(Matcher.quoteReplacement("\\"),
                            Matcher.quoteReplacement("\n")));
                }
                line = br.readLine();
            }
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }
    }


    /**
     * Returns the text ready to show on the current applying language.
     * <p>
     * This method translates the text's id a the readable String. The language of this LanguageLoader object depends
     * on the information recorded in the config file. It is usually the "pref.ini".
     *
     * @param id the identifier representing the language.
     * @return the text will be shown to user.
     */
    public String show(int id) {
        return lanMap.get(id);
    }


    /**
     * Returns the code of current language.
     *
     * @return the code of current language.
     * @throws IOException if the config file is not readable.
     */
    public static String getCurrentLanguageCode() throws IOException {
        return ConfigLoader.getConfig().get("language");
    }


    /**
     * Returns a HashMap object all language's name and code.
     * <p>
     * The key is the language's name, the value is the language's code.
     *
     * @return name and code of all languages.
     * @throws IOException if the language file is not readable.
     */
    public static HashMap<String, String> getAllLanguages() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        File lanDir = new File("resources" + File.separator + "languages");
        File[] lanFiles = lanDir.listFiles();
        assert lanFiles != null;
        for (File lanFile : lanFiles) {
            String value = lanFile.getName().substring(0, lanFile.getName().length() - 4);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(lanFile));
            BufferedReader br = new BufferedReader(reader);
            String[] firstLine = br.readLine().split("=");
            String key = firstLine[1];
            map.put(key, value);
        }
        return map;
    }
}
