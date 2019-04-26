package trashsoftware.deepSearcher.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

public abstract class HelperFunctions {

    public static void main(String[] args) {
        System.out.println(stripSpaces("       ").length());
        System.out.println(stringCount("x|y||z", "|"));
    }


    /**
     * Returns the url-form of a file with its path given.
     * <p>
     * The returned url depends on the operating system.
     *
     * @param filePath the path of the file.
     * @return the url-form of this path.
     * @throws IOException if the path is not valid.
     */
    public static String toURLForm(String filePath) throws IOException {
        String formedPath;
        if (filePath.contains("\\")) {
            formedPath = filePath.replaceAll(Matcher.quoteReplacement("\\"), Matcher.quoteReplacement(File.separator));
        } else if (filePath.contains("/")) {
            formedPath = filePath.replaceAll(Matcher.quoteReplacement("/"), Matcher.quoteReplacement(File.separator));
        } else {
            throw new IOException();
        }

        File f = new File(formedPath);

        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return "file:\\" + f.getAbsolutePath();
        } else {
            return "file://" + f.getAbsolutePath();
        }
    }


    /**
     * Returns the String without spaces at beginning and end.
     *
     * @param target the String to be stripped.
     * @return the String with spaces at beginning and end stripped.
     */
    public static String stripSpaces(String target) {
        if (target.length() == 0) {
            return "";
        }
        int i = 0;
        while (target.charAt(i) == ' ') {
            i += 1;
            if (i == target.length()) {
                return "";
            }
        }
        int j = target.length() - 1;
        while (target.charAt(j) == ' ') {
            j -= 1;
        }
        return target.substring(i, j + 1);

    }


    /**
     * Returns the total appearance times of a substring in a string.
     *
     * @param source the source string.
     * @param target the substring which will be counted.
     * @return the appearance time of "target" in "source".
     */
    public static int stringCount(String source, String target) {
        int c = 0;
        int length = target.length();
        for (int i = 0; i < source.length() - length + 1; i++) {
            if (source.substring(i, i + length).equals(target)) {
                c += 1;
            }
        }
        return c;
    }
}
