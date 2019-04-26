package trashsoftware.deepSearcher.searcher;

import java.io.*;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hwpf.extractor.*;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmlbeans.XmlException;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;


public class FileReader {

    private File file;

    private String ext;

    private String[] textFormats = new String[]{".bat", ".cmd", ".ini", ".java", ".log", ".py", ".pyw", ".txt"};

    private String[] wordFormats = new String[]{".doc", ".docx"};

    private String[] excelFormats = new String[]{".xls", ".xlsx"};

    private String[] powerPointFormats = new String[]{".ppt", ".pptx"};

    private String[] pdfFormats = new String[]{".pdf"};

    private String[] rtfFormats = new String[]{".rtf"};

    public static void main(String[] args) {
        FileReader fr = new FileReader(new File("E:\\test\\2333.pdf"), ".pdf");
//        System.out.println(fr.read());
        System.out.println(fr.read());

    }

    /**
     * Constructor of a FileReader object.
     * <p>
     * Creates a new FileReader object of the given File, with the given extension.
     *
     * @param file the File object to read.
     * @param ext  the extension of this File.
     */
    FileReader(File file, String ext) {
        this.file = file;
        this.ext = ext;
    }


    /**
     * Returns a string containing all text content in the opened file.
     * <p>
     * This method will check the format of the file and call the appropriate method to read this file.
     *
     * @return file's string content.
     */
    public String read() {
        try {
            if (arrayContains(textFormats, ext)) {
                return textFileReader();
            } else if (arrayContains(wordFormats, ext)) {
                return microsoftWordFileReader();
            } else if (arrayContains(excelFormats, ext)) {
                return microsoftExcelReader();
            } else if (arrayContains(powerPointFormats, ext)) {
                return microsoftPowerPointReader();
            } else if (arrayContains(pdfFormats, ext)) {
                return pdfReader();
            } else if (arrayContains(rtfFormats, ext)) {
                return rtfReader();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    private static boolean arrayContains(String[] source, String target) {
        for (String item : source) {
            if (item.equals(target)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the string content if this.file is a text file.
     *
     * @return the content of file.
     * @throws IOException if the file is not readable.
     */
    private String textFileReader() throws IOException {
        InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file));
        BufferedReader br = new BufferedReader(reader);

        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        return sb.toString();
    }

    private String microsoftWordFileReader() throws IOException {
        String text = "";
        String filePath = file.getAbsolutePath();
        if (file.getName().endsWith(".doc")) {

            FileInputStream stream = new FileInputStream(file);
            WordExtractor word = new WordExtractor(stream);
            text = word.getText();
            stream.close();

        } else if (file.getName().endsWith(".docx")) {

            OPCPackage opc = POIXMLDocument.openPackage(filePath);
            XWPFDocument xd = new XWPFDocument(opc);
            POIXMLTextExtractor ex = new XWPFWordExtractor(xd);
            text = ex.getText();

        }
        return text;
    }

    private String microsoftExcelReader() throws IOException {
        String text = "";
        if (file.getName().endsWith("xls")) {
            InputStream ips = new FileInputStream(file);
            HSSFWorkbook wb = new HSSFWorkbook(ips);
            ExcelExtractor ex = new ExcelExtractor(wb);
            ips.close();
            ex.setFormulasNotResults(true);
            ex.setIncludeSheetNames(true);

            text = ex.getText();

        } else if (file.getName().endsWith("xlsx")) {
            try {
                String filePath = file.getAbsolutePath();
                OPCPackage opc = POIXMLDocument.openPackage(filePath);
                XSSFExcelExtractor ex = new XSSFExcelExtractor(opc);
                ex.setFormulasNotResults(true);
                ex.setIncludeSheetNames(true);

                text = ex.getText();
            } catch (XmlException | OpenXML4JException xe) {
                //
            }
        }
        return text;

    }


    private String microsoftPowerPointReader() throws IOException {

        String text = "";
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith("ppt")) {

            InputStream ips = new FileInputStream(file);
            HSLFSlideShowImpl wb = new HSLFSlideShowImpl(ips);
            PowerPointExtractor ex = new PowerPointExtractor(wb);
            ips.close();

            text = ex.getText();

        } else if (filePath.endsWith(".pptx")) {
            try {
                InputStream ips = new FileInputStream(file);
                XSLFSlideShow wb = new XSLFSlideShow(filePath);
                XSLFPowerPointExtractor ex = new XSLFPowerPointExtractor(wb);
                ips.close();

                text = ex.getText();
            } catch (XmlException | OpenXML4JException xe) {
                //
            }
        }


        return text;
    }


    private String pdfReader() throws IOException {

        PDDocument document = PDDocument.load(file);
        if (!document.isEncrypted()) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } else {

            return null;
        }

    }


    private String rtfReader() throws IOException {
        String result = null;
        try {
            DefaultStyledDocument styledDoc = new DefaultStyledDocument();
            InputStream is = new FileInputStream(file);
            new RTFEditorKit().read(is, styledDoc, 0);
            result = new String(styledDoc.getText(0, styledDoc.getLength())
                    .getBytes("ISO8859-1"), "GBK");
        } catch (BadLocationException | IndexOutOfBoundsException e) {
            //
        }
        return result;
    }
}
