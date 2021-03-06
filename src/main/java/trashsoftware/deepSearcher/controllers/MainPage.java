package trashsoftware.deepSearcher.controllers;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.EventLogger;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;
import trashsoftware.deepSearcher.configLoader.Recorder;
import trashsoftware.deepSearcher.itemUnits.FileCell;
import trashsoftware.deepSearcher.itemUnits.FileRoot;
import trashsoftware.deepSearcher.itemUnits.FormatCell;
import trashsoftware.deepSearcher.itemUnits.SearchHistoryItem;
import trashsoftware.deepSearcher.searcher.Searcher;
import trashsoftware.deepSearcher.util.HelperFunctions;

import java.awt.*;
import java.io.File;
import java.io.IOException;
//import java.lang.management.ManagementFactory;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
//import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class MainPage implements Initializable {

    @FXML
    GridPane rootPane;

    @FXML
    CheckBox searchFile;

    @FXML
    CheckBox searchDir;

    @FXML
    CheckBox searchCont;

    @FXML
    TableView<FormatCell> formatTable;

    @FXML
    TableColumn<FormatCell, String> checkCol;

    @FXML
    TableColumn<FormatCell, String> extCol;

    @FXML
    TableColumn<FormatCell, String> desCol;

    @FXML
    Button selectButton;

    @FXML
    Button search;

    @FXML
    Button openFileButton;

    @FXML
    Button openDirButton;

    @FXML
    TextField searchField;

    @FXML
    Label currentRootDirText;

    @FXML
    CheckBox selectAllBox;

    @FXML
    Label fileCount;

    @FXML
    Label timeCount;

    @FXML
    Menu mainMenu;

    @FXML
    Menu help;

    @FXML
    TableView<FileCell> fileTable;

    @FXML
    TableColumn<FileCell, String> c1;

    @FXML
    TableColumn<FileCell, String> c2;

    @FXML
    TableColumn<FileCell, String> c3;

    @FXML
    TableColumn<FileCell, String> c4;

    @FXML
    MenuItem settings;

    @FXML
    MenuItem exit;

    @FXML
    Menu searchHis;

    @FXML
    MenuItem helpMenuItem;

    @FXML
    MenuItem about;

    @FXML
    MenuItem licence;

    @FXML
    HBox h1;

    @FXML
    HBox h2;

    @FXML
    HBox h3;

    @FXML
    ProgressIndicator proInd;

    @FXML
    Button cancelSearch;

    private LanguageLoader lanLoader = new LanguageLoader();

    private File currentRootDir;

    private HashMap<String, String> extensions = new HashMap<>();

    private Stage stage;

    private long start;

    private boolean isReSearching;

    private SearchHistoryItem reSearchItem;

    private double ratio;

    private Thread currentThread;

    private boolean isInSearchThread;

    private float lastTime;

    private boolean isCanceled;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            ratio = Double.valueOf(ConfigLoader.getConfig().get("scale")) / 100.0;
        } catch (IOException ioe) {
            EventLogger.log(ioe, ioe.getMessage(), Level.WARNING);
        }

        fillLanguage();
        setUIScale();

        addContentBoxListener();
        addFileTableListener();
        addDesColListener();
        addSelectAllBoxListener();

        c1.setCellValueFactory(new PropertyValueFactory<>("Name"));
        c2.setCellValueFactory(new PropertyValueFactory<>("Name1"));
        c3.setCellValueFactory(new PropertyValueFactory<>("Type"));
        c4.setCellValueFactory(new PropertyValueFactory<>("Name2"));

        checkCol.setCellValueFactory(new PropertyValueFactory<>("Box"));
        extCol.setCellValueFactory(new PropertyValueFactory<>("Name1"));
        desCol.setCellValueFactory(new PropertyValueFactory<>("Name2"));

        fillFormatTable();

        addSubMenuToHistory();
        proInd.setScaleX(0.5);
        proInd.setScaleY(0.5);
        proInd.setVisible(false);

        searchFile.setSelected(true);
        addCheckBoxesListener();

        cancelSearch.managedProperty().bind(cancelSearch.visibleProperty());
    }


    /**
     * The action event handler for "search" Button.
     * <p>
     * This method sets up and starts a search thread.
     */
    @FXML
    private void searchAction() {

        // Check if is now in another searching thread.
        if (isInSearchThread) {
            ContextMenu cm = new ContextMenu();
            cm.getItems().add(new MenuItem(lanLoader.show(37)));
            search.setOnMouseClicked(event -> cm.show(stage, event.getScreenX(), event.getScreenY()));
            return;
        } else {
            search.setOnMouseClicked(null);
        }

        // Check if is re-searching.
        if (!isReSearching) {
            if (searchField.getText().length() == 0 || searchField.getText().length() == HelperFunctions.
                    stringCount(searchField.getText(), " ") || (selectedCheckBoxesCount() == 1 &&
                    searchCont.isSelected() && getSelectedInFormatTable().size() == 0)) {

                fileTable.getItems().clear();
                fileCount.setText(null);
                timeCount.setText(null);
                return;
            }

            if (!checkValidInput()) {
                ContextMenu cm = new ContextMenu();
                cm.getItems().add(new MenuItem(lanLoader.show(34)));
                search.setOnMouseClicked(event -> cm.show(stage, event.getScreenX(), event.getScreenY()));
                return;
            } else {
                search.setOnMouseClicked(null);
            }
        }

        // Searching task.
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws IOException {
                updateMessage(lanLoader.show(28));
                start = System.currentTimeMillis();
                fileTable.getItems().clear();

                Searcher se = new Searcher(currentThread);
                se.progressProperty().addListener((obs, oldProgress, newProgress) ->
                        updateTitle((int) newProgress.doubleValue() + lanLoader.show(26)));

                if (isReSearching) {

                    reSearchThread(se);
                } else {

                    searchThread(se);
                }

                return null;
            }
        };

        // Bind UI texts.
        timeCount.textProperty().bind(task.messageProperty());
        fileCount.textProperty().bind(task.titleProperty());
        cancelSearch.visibleProperty().bind(task.runningProperty());
        proInd.visibleProperty().bind(task.runningProperty());

        task.setOnFailed(e -> EventLogger.log(e.getSource().getException(), e.getSource().getMessage(), Level.INFO));

        task.setOnSucceeded(e -> {
            isInSearchThread = false;
            timeCount.textProperty().unbind();
            fileCount.textProperty().unbind();

            forcedRefreshFileTable();

            if (isCanceled) {
                timeCount.setText(lanLoader.show(36) + " " + calculateTime() + lanLoader.show(30));
            } else {
                timeCount.setText(lanLoader.show(29) + " " + calculateTime() + lanLoader.show(30));
            }
            fileCount.setText(fileTable.getItems().size() + lanLoader.show(26));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);

        currentThread = thread;
        isInSearchThread = true;
        isCanceled = false;
        thread.start();

    }


    /**
     * The main thread of search process.
     * <p>
     * This method will be called when a new search is started.
     *
     * @param se the searcher object of this thread, created in the search Task.
     * @throws IOException if the config file cannot be load.
     */
    private void searchThread(Searcher se) throws IOException {

        String target = searchField.getText();
        File directory = currentRootDir;
        boolean searchFileName = searchFile.isSelected();
        boolean searchDirectory = searchDir.isSelected();
        boolean searchContent = searchCont.isSelected();
        boolean caseSense = Boolean.valueOf(ConfigLoader.getConfig().get("case_sen"));
        boolean notSearchExt = Boolean.valueOf(ConfigLoader.getConfig().get("not_ext"));
        boolean showHidden = Boolean.valueOf(ConfigLoader.getConfig().get("show_hidden"));
        ArrayList<String> ext = getSelectedInFormatTable();
        String sep = ConfigLoader.getConfig().get("and_sep");
        String dirSep = ConfigLoader.getConfig().get("dir_sep");
        pureSearchThread(se, target, directory, searchFileName, searchDirectory, searchContent, caseSense,
                notSearchExt, showHidden, ext, sep, dirSep);
        recordSearch();
        addSubMenuToHistory();

    }


    /**
     * The parameter setter and starter of the search thread.
     *
     * @param searcher        the searcher object.
     * @param target          the contents for searching.
     * @param directory       the start directory of this search action.
     * @param searchFileName  whether to search file names.
     * @param searchDirectory whether to include path names in result.
     * @param searchContent   whether to search file contents.
     * @param caseSensitive   whether the search is case-sensitive.
     * @param notSearchExt    whether to not include the files' extensions.
     * @param showHidden      whether to show hidden files.
     * @param extensions      the list of file formats which will be opened and searched.
     * @param separator       the and-separator of this search.
     * @param dirSep          the directory-separator.
     */
    private void pureSearchThread(Searcher searcher, String target, File directory, boolean searchFileName,
                                  boolean searchDirectory, boolean searchContent, boolean caseSensitive,
                                  boolean notSearchExt, boolean showHidden, ArrayList<String> extensions,
                                  String separator, String dirSep) {
        if (target.length() == 0) {
            return;
        }

        searcher.setSeparator(separator);
        searcher.setDirSep(dirSep);
        searcher.setSearchDir(searchDirectory);
        searcher.setSearchFile(searchFileName);
        searcher.setNotSearchExt(notSearchExt);
        searcher.setShowHidden(showHidden);
        if (searchContent) {
            searcher.setSearchContent(true);
            searcher.setExtensions(extensions);
        }
        searcher.setCaseSensitive(caseSensitive);

        searcher.setTableList(fileTable.getItems());
        searcher.setLanLoader(lanLoader);
        searcher.setSearch(directory, target);
        searcher.startSearch();
    }


    /**
     * The action event handler of "selectButton" Button.
     * <p>
     * This method creates a directory chooser dialog to the user and let him/her choose a root directory to search.
     */
    @FXML
    public void selectAction() {

        try {
            boolean useCustom = Boolean.valueOf(ConfigLoader.getConfig().get("custom_chooser"));
            if (useCustom && System.getProperties().getProperty("os.name").contains("Windows")) {
                customChooser();
            } else {
                nativeChooser();
            }
        } catch (IOException ioe) {
            nativeChooser();
        }

        if (searchFile.isSelected() || searchDir.isSelected() || searchCont.isSelected()) {
            search.setDisable(false);
            searchField.setOnAction(event -> searchAction());
        }

    }


    /**
     * Shows the customized directory chooser.
     */
    private void customChooser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/trashsoftware/deepSearcher/fxml/explorer.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(lanLoader.show(1003));
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.getIcons().add(this.stage.getIcons().get(0));

            ExplorerClient s = loader.getController();
            s.setController(this);
            s.setStage(stage);

            stage.show();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * Shows the system's native directory chooser.
     */
    private void nativeChooser() {
        DirectoryChooser dirChoose = new DirectoryChooser();

        File selectedDirectory = dirChoose.showDialog(null);
        if (selectedDirectory != null) {
            currentRootDir = selectedDirectory;
            currentRootDirText.setText(lanLoader.show(22) + "\n" + currentRootDir.getAbsolutePath());

        }
    }

    /**
     * Sets up the current searching directory.
     *
     * If the dir is null, the currentRootDir will represent the system root directory.
     *
     * @param dir the current searching directory.
     */
    public void setStartDirectory(File dir) {
        this.currentRootDir = dir;
        if (dir instanceof FileRoot) {
            currentRootDirText.setText(lanLoader.show(22) + "\n" + lanLoader.show(1001));

        } else {
            currentRootDirText.setText(lanLoader.show(22) + "\n" + currentRootDir.getAbsolutePath());
        }
    }


    /**
     * The action event handler of the "OpenFileButton" Button and the double click event on the TableRow of
     * the TableView "fileTable".
     * <p>
     * This method calls the system's default method to open the selected file.
     */
    @FXML
    public void openFile() {
        FileCell currentFc = fileTable.getSelectionModel().getSelectedItem();
        try {
            Desktop.getDesktop().open(new File(currentFc.getFullPath()));
        } catch (IOException ioe) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lanLoader.show(201));
            alert.setHeaderText(lanLoader.show(202));
            alert.setContentText(lanLoader.show(203));
            alert.showAndWait();
        }

    }


    /**
     * The action event handler of the "openDirectory" Button.
     * <p>
     * This method calls the system's default file manager to open parent directory of the selected file.
     */
    @FXML
    public void openDirectory() {
        FileCell currentFc = fileTable.getSelectionModel().getSelectedItem();
        try {
            Desktop.getDesktop().open(new File(currentFc.getName1()));
        } catch (IOException ioe) {
            EventLogger.log(ioe, ioe.getMessage(), Level.SEVERE);
        }

    }


    /**
     * Action event handler of the Button "cancelSearch".
     *
     * This method interrupts the current searching thread.
     */
    @FXML
    public void cancelSearchAction() {
        currentThread.interrupt();
        isInSearchThread = false;
        isCanceled = true;
    }


    /**
     * The action event handler of the MenuItem "setting".
     * <p>
     * This method creates a new settings window.
     */
    @FXML
    public void settingsAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/trashsoftware/deepSearcher/fxml/settings.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(lanLoader.show(15));
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.getIcons().add(this.stage.getIcons().get(0));

            Settings s = loader.getController();
            s.setStage(stage);
            s.setController(this);

            stage.show();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * The action event handler of clicking on Button "exit".
     */
    @FXML
    public void quit() {
        stage.close();
    }


    /**
     * The action event handler of MenuItem "about".
     * <p>
     * This method creates a new window to show the information of this program.
     */
    @FXML
    public void aboutAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/trashsoftware/deepSearcher/fxml/about.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(lanLoader.show(16));
            stage.setScene(new Scene(root));
            stage.getIcons().add(this.stage.getIcons().get(0));
            stage.show();
        } catch (IOException ioe) {
            //
        }
    }


    /**
     * The action event handler of MenuItem "licence".
     * <p>
     * This method creates a new window to show the GNU General Public Licence of this program.
     */
    @FXML
    public void licenceAction() {
        Pane root = new Pane();
        Stage dialog = new Stage();
        Scene scene = new Scene(root);
        dialog.setTitle(lanLoader.show(403));
        dialog.setScene(scene);
        dialog.setHeight(300.0 * ratio);
        dialog.setWidth(450.0 * ratio);
        dialog.setResizable(false);
        dialog.getIcons().add(this.stage.getIcons().get(0));

        VBox pane = new VBox();
        pane.setFillWidth(true);
        pane.setAlignment(Pos.CENTER);

//        String licText = ConfigLoader.readAllFromText("resources" + File.separator + "licence.txt");
        String licText = ConfigLoader.readTextFromResource(getClass()
                .getResourceAsStream("/trashsoftware/deepSearcher/licence.txt"));
        Label label = new Label(licText);
        label.setTextAlignment(TextAlignment.CENTER);
        pane.getChildren().add(label);
        root.getChildren().add(pane);

        dialog.show();
    }


    /**
     * The action event handler of MenuItem "helpMenuItem".
     * <p>
     * This method creates a new window to show help information.
     */
    @FXML
    public void showHelpPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/trashsoftware/deepSearcher/fxml/helpPane.fxml"));

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(lanLoader.show(14));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.getIcons().add(this.stage.getIcons().get(0));

            stage.show();
        } catch (IOException ioe) {
            EventLogger.log(ioe, ioe.getMessage(), Level.INFO);
        }
    }


    /**
     * Forcibly refresh the TableView object "fileTable".
     * <p>
     * This method is used to resolve a bug of javafx that, while dynamic filling the formatTable, sometimes the
     * last item not shows up until the user clicks on it.
     */
    private void forcedRefreshFileTable() {
        ArrayList<FileCell> list = new ArrayList<>(fileTable.getItems());
        fileTable.getItems().clear();
        fileTable.getItems().addAll(list);
    }


    /**
     * Change the existing contents language of the TableView object "fileTable".
     */
    private void changeTableLanguage() {
        ArrayList<FileCell> list = new ArrayList<>(fileTable.getItems());
        for (FileCell fc : fileTable.getItems()) {
            fc.setLanguageLoader(lanLoader);
        }
        fileTable.getItems().clear();
        fileTable.getItems().addAll(list);
    }


    /**
     * Set up the primary stage of the main thread.
     *
     * @param stage the primary stage.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    /**
     * Fills the TableView object "formatTable" with all supported formats and their description.
     */
    private void fillFormatTable() {
        ArrayList<String> checked = new ArrayList<>();
        if (formatTable.getItems().size() > 0) {
            for (FormatCell fc : formatTable.getItems()) {
                if (fc.getBox().isSelected()) {
                    checked.add(fc.getName1());
                }
            }
            formatTable.getItems().clear();
        }

        createFormatMap();
        ArrayList<FormatCell> cells = new ArrayList<>();
        for (String ext : extensions.keySet()) {
            FormatCell cell = new FormatCell(ext, extensions.get(ext));
            if (checked.contains(ext)) {
                cell.getBox().setSelected(true);
            }
            cells.add(cell);
        }
        Collections.sort(cells);
        formatTable.getItems().addAll(cells);
    }


    /**
     * Returns all the formats' extensions that are selected in the TableView object "formatTable".
     *
     * @return all extensions that are selected.
     */
    private ArrayList<String> getSelectedInFormatTable() {
        ArrayList<String> result = new ArrayList<>();
        for (FormatCell cell : formatTable.getItems()) {
            if (cell.getBox().isSelected()) {
                result.add(cell.getName1());
            }
        }
        return result;
    }


    /**
     * Adds all supported file extensions to the HashMap "extensions".
     */
    private void createFormatMap() {
        extensions.put(".txt", lanLoader.show(101));
        extensions.put(".log", lanLoader.show(102));
        extensions.put(".ini", lanLoader.show(103));
        extensions.put(".py", lanLoader.show(104));
        extensions.put(".pyw", lanLoader.show(105));
        extensions.put(".java", lanLoader.show(106));
        extensions.put(".doc", lanLoader.show(107));
        extensions.put(".docx", lanLoader.show(108));
        extensions.put(".pdf", lanLoader.show(109));
        extensions.put(".cmd", lanLoader.show(110));
        extensions.put(".bat", lanLoader.show(111));
        extensions.put(".xlsx", lanLoader.show(112));
        extensions.put(".xls", lanLoader.show(113));
        extensions.put(".pptx", lanLoader.show(114));
        extensions.put(".ppt", lanLoader.show(115));
        extensions.put(".rtf", lanLoader.show(116));
    }


    /*
     * Change Listeners.
     */

    /**
     * Adds the listener of the CheckBox object "searchCont".
     * <p>
     * If this CheckBox is selected, then enable the TableView object "formatTable".
     */
    private void addContentBoxListener() {
        searchCont.selectedProperty().addListener((ov, old_val, new_val) -> {
            formatTable.setDisable(old_val);
            selectAllBox.setDisable(old_val);
        });
    }


    /**
     * Adds the change listener of CheckBox object "selectAllBox".
     */
    private void addSelectAllBoxListener() {
        selectAllBox.selectedProperty().addListener((ov, old_val, new_val) -> {
            for (FormatCell fc : formatTable.getItems()) {
                fc.getBox().setSelected(new_val);
            }
        });
    }


    /**
     * Adds the change listener of the TableView object "fileTable" to respond the user's selection.
     */
    private void addFileTableListener() {
        fileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                openFileButton.setDisable(false);
                openDirButton.setDisable(false);
            } else {
                openFileButton.setDisable(true);
                openDirButton.setDisable(true);
            }
        });

        setFileTableRowFactory();
        addC1Listener();
        addC2Listener();

    }


    /**
     * Sets up the callback function of the TableView object "fileTable".
     * <p>
     * This method adds the row factory to respond the user's double click on a table row.
     */
    private void setFileTableRowFactory() {
        fileTable.setRowFactory(new Callback<TableView<FileCell>, TableRow<FileCell>>() {
            @Override
            public TableRow<FileCell> call(TableView<FileCell> param) {
                return new TableRow<FileCell>() {
                    @Override
                    protected void updateItem(FileCell item, boolean empty) {
                        super.updateItem(item, empty);

                        setOnMouseClicked(click -> {
                            if (click.getClickCount() == 2) {
                                openFile();
                            }
                        });
                    }
                };
            }
        });
    }


    /**
     * Adds listener for the TableColumn object "c1", which shows the the names of files.
     * <p>
     * This method adds the hovering listener of this table column.
     */
    private void addC1Listener() {
        c1.setCellFactory(new Callback<TableColumn<FileCell, String>, TableCell<FileCell, String>>() {
            @Override
            public TableCell<FileCell, String> call(TableColumn<FileCell, String> param) {
                return new TableCell<FileCell, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);

                            hoverProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasHovered,
                                                         Boolean isNowHovered) -> {
                                if (isNowHovered && !isEmpty()) {
                                    Tooltip tp = new Tooltip();
                                    tp.setText(getText());

                                    fileTable.setTooltip(tp);
                                } else {
                                    fileTable.setTooltip(null);
                                }
                            });
                        }
                    }
                };
            }
        });
    }


    /**
     * Adds listener for the TableColumn object "c2", which shows the the paths of files.
     * <p>
     * This method adds the hovering listener of this table column.
     */
    private void addC2Listener() {
        c2.setCellFactory(new Callback<TableColumn<FileCell, String>, TableCell<FileCell, String>>() {
            @Override
            public TableCell<FileCell, String> call(TableColumn<FileCell, String> param) {
                return new TableCell<FileCell, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);

                            hoverProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasHovered,
                                                         Boolean isNowHovered) -> {
                                if (isNowHovered && !isEmpty()) {
                                    Tooltip tp = new Tooltip();
                                    tp.setText(getText());

                                    fileTable.setTooltip(tp);
                                } else {
                                    fileTable.setTooltip(null);
                                }
                            });
                        }
                    }
                };
            }
        });
    }


    /**
     * Adds listener for the TableColumn object "desCol", which shows the descriptions of file formats.
     * <p>
     * This method adds the hovering listener of this table column.
     */
    private void addDesColListener() {
        desCol.setCellFactory(new Callback<TableColumn<FormatCell, String>, TableCell<FormatCell, String>>() {
            @Override
            public TableCell<FormatCell, String> call(TableColumn<FormatCell, String> param) {
                return new TableCell<FormatCell, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);

                            hoverProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasHovered,
                                                         Boolean isNowHovered) -> {
                                if (isNowHovered && !isEmpty()) {
                                    Tooltip tp = new Tooltip();
                                    tp.setText(getText());

                                    formatTable.setTooltip(tp);
                                } else {
                                    formatTable.setTooltip(null);
                                }
                            });
                        }
                    }
                };
            }
        });
    }


    /**
     * Adds change listeners for CheckBox objects "searchFile", "searchDir", "searchCont".
     */
    private void addCheckBoxesListener() {
        searchFile.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedCheckBoxesCount() > 0 && currentRootDir != null) {
                search.setDisable(false);
            } else {
                search.setDisable(true);
            }
        });

        searchDir.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedCheckBoxesCount() > 0 && currentRootDir != null) {
                search.setDisable(false);
            } else {
                search.setDisable(true);
            }
        });

        searchCont.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedCheckBoxesCount() > 0 && currentRootDir != null) {
                search.setDisable(false);
            } else {
                search.setDisable(true);
            }
        });
    }


    /**
     * Returns the count of current-selecting CheckBox objects among "searchFile", "searchDir", "searchCont".
     *
     * @return the count of selected CheckBox object.
     */
    private int selectedCheckBoxesCount() {
        int i = 0;
        for (CheckBox cb : new CheckBox[]{searchFile, searchDir, searchCont}) {
            if (cb.isSelected()) {
                i += 1;
            }
        }
        return i;
    }


    /**
     * Fills all components in this window wil the language of the LanguageLoader object "lanLoader".
     */
    private void fillLanguage() {
        mainMenu.setText(lanLoader.show(13));
        help.setText(lanLoader.show(14));

        searchFile.setText(lanLoader.show(33));
        searchDir.setText(lanLoader.show(10));
        searchCont.setText(lanLoader.show(11));

        selectButton.setText(lanLoader.show(17));
        search.setText(lanLoader.show(1));
        searchField.setPromptText(lanLoader.show(12));
        openFileButton.setText(lanLoader.show(23));
        openDirButton.setText(lanLoader.show(24));
        cancelSearch.setText(lanLoader.show(302));

        currentRootDirText.setText(lanLoader.show(22));

        selectAllBox.setText(lanLoader.show(18));

        fileTable.setPlaceholder(new Label());

        c1.setText(lanLoader.show(4));
        c2.setText(lanLoader.show(5));
        c3.setText(lanLoader.show(6));
        c4.setText(lanLoader.show(7));

        checkCol.setText(lanLoader.show(19));
        extCol.setText(lanLoader.show(20));
        desCol.setText(lanLoader.show(21));

        settings.setText(lanLoader.show(15));
        exit.setText(lanLoader.show(25));
        about.setText(lanLoader.show(16));

        searchHis.setText(lanLoader.show(31));

        helpMenuItem.setText(lanLoader.show(801));
        licence.setText(lanLoader.show(403));

    }


    /**
     * Refresh the search history submenu.
     */
    public void addSubMenuToHistory() {
        try {
            searchHis.getItems().clear();
            ArrayList<SearchHistoryItem> history = Recorder.readSearchHistory();
            Collections.reverse(history);
            if (history.size() == 0) {
                searchHis.getItems().add(new MenuItem(lanLoader.show(32)));
            } else {
                int i = history.size() - 1;
                for (SearchHistoryItem shi : history) {
                    MenuItem cmi = new MenuItem(shi.toString());
                    cmi.setId(String.valueOf(i));
                    cmi.setOnAction(event -> searchHisAction(cmi.getId()));

                    searchHis.getItems().add(cmi);
                    i -= 1;
                }
            }
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }
    }



    /**
     * Returns the time that the current-done search action took.
     *
     * @return the time that the search took, in ms.
     */
    private float calculateTime() {
        lastTime = ((float) (System.currentTimeMillis() - start)) / 1000;
        return lastTime;
    }


    /**
     * To record this search once any search is done.
     */
    private void recordSearch() throws IOException {
        boolean caseSense = Boolean.valueOf(ConfigLoader.getConfig().get("case_sen"));
        boolean notSearchExt = Boolean.valueOf(ConfigLoader.getConfig().get("not_ext"));
        boolean showHidden = Boolean.valueOf(ConfigLoader.getConfig().get("show_hidden"));
        SearchHistoryItem thisSearch = new SearchHistoryItem(searchField.getText(), currentRootDir,
                searchFile.isSelected(), searchDir.isSelected(), searchCont.isSelected(),
                caseSense, notSearchExt, showHidden, getSelectedInFormatTable(),
                ConfigLoader.getConfig().get("and_sep"), ConfigLoader.getConfig().get("dir_sep"));
        Recorder.recordSearch(thisSearch);
    }


    /**
     * Action of Re-Search in search history menu.
     *
     * @param id a String id to let the function know which CheckMenuItem called it.
     */
    private void searchHisAction(String id) {
        try {
            ScrollPane root = new ScrollPane();

            Stage dialog = new Stage();
            Scene scene = new Scene(root);
            dialog.setTitle(lanLoader.show(502));
            dialog.setScene(scene);
            dialog.setHeight(300.0 * ratio);
            dialog.setWidth(300.0 * ratio);

            File fontFile = new File("UserSettings/theme/font.css");
            File mainFile = new File("UserSettings/theme/main.css");
            scene.getStylesheets().add("file:///" + fontFile.getAbsolutePath().replace("\\", "/"));
            scene.getStylesheets().add("file:///" + mainFile.getAbsolutePath().replace("\\", "/"));

            SearchHistoryItem item = Recorder.readSearchHistory().get(Integer.valueOf(id));

            VBox pane = new VBox();

            pane.setPadding(new Insets(10.0 * ratio));
            pane.setSpacing(20.0 * ratio);

            GridPane gp = new GridPane();
            gp.setLayoutX(20.0 * ratio);
            gp.setVgap(10.0 * ratio);
            gp.setHgap(10.0 * ratio);

            gp.add(new Label(lanLoader.show(503)), 0, 0);
            gp.add(new Label(item.getContent()), 1, 0);

            gp.add(new Label(lanLoader.show(504)), 0, 1);
            if (item.getDirectory() instanceof FileRoot) {
                gp.add(new Label(lanLoader.show(1001)), 1, 1);
            } else {
                gp.add(new Label(item.getDirectory().getAbsolutePath()), 1, 1);
            }
            gp.add(new Label(lanLoader.show(511)), 0, 2);
            gp.add(new Label(translateBoolean(item.isSearchFile())), 1, 2);
            gp.add(new Label(lanLoader.show(505)), 0, 3);
            gp.add(new Label(translateBoolean(item.isSearchDir())), 1, 3);
            gp.add(new Label(lanLoader.show(506)), 0, 4);
            gp.add(new Label(translateBoolean(item.isSearchCont())), 1, 4);
            gp.add(new Label(lanLoader.show(507)), 0, 5);
            gp.add(new Label(translateBoolean(item.isCaseSense())), 1, 5);
            gp.add(new Label(lanLoader.show(512)), 0, 6);
            gp.add(new Label(translateBoolean(item.isShowingHidden())), 1, 6);

            gp.add(new Label(lanLoader.show(508)), 0, 7);
            SimpleDateFormat formatter = new SimpleDateFormat(lanLoader.show(601));
            gp.add(new Label(formatter.format(item.getRecordTime())), 1, 7);


            Button resConfirm = new Button(lanLoader.show(501));
            resConfirm.setOnAction(event -> {
                File resStartDir = item.getDirectory();
                if (resStartDir.exists()) {

                    isReSearching = true;
                    reSearchItem = item;
                    searchAction();

                    dialog.close();
                } else {
                    showPathNotExistDialog();
                }

            });

            HBox buttonBox = new HBox();

            buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
            buttonBox.getChildren().add(resConfirm);

            pane.getChildren().add(gp);
            pane.getChildren().add(buttonBox);
            root.setContent(pane);
//            root.setLayoutX(20.0 * ratio);
//            root.setLayoutY(10.0 * ratio);

            dialog.getIcons().add(this.stage.getIcons().get(0));

            dialog.show();
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }
    }


    /**
     * The thread of search history.
     * <p>
     * This method will be called only when the user had chosen a search history item and clicked "Re-Search" button.
     *
     * @param se the searcher created in the task of search.
     * @throws IOException if the config file cannot be loaded.
     */
    private void reSearchThread(Searcher se) throws IOException {
        String target = reSearchItem.getContent();
        File directory = reSearchItem.getDirectory();
        boolean searchFileName = searchFile.isSelected();
        boolean searchDirectory = searchDir.isSelected();
        boolean searchContent = searchCont.isSelected();
        boolean caseSense = Boolean.valueOf(ConfigLoader.getConfig().get("case_sen"));
        boolean notSearchExt = Boolean.valueOf(ConfigLoader.getConfig().get("not_ext"));
        boolean showHidden = Boolean.valueOf(ConfigLoader.getConfig().get("show_hidden"));
        ArrayList<String> ext = getSelectedInFormatTable();
        if (Boolean.valueOf(ConfigLoader.getConfig().get("keep_orig"))) {
            searchFileName = reSearchItem.isSearchFile();
            searchDirectory = reSearchItem.isSearchDir();
            searchContent = reSearchItem.isSearchCont();
            caseSense = reSearchItem.isCaseSense();
            ext = reSearchItem.getExtensions();
            notSearchExt = reSearchItem.isNotSearchExtensions();
            showHidden = reSearchItem.isShowingHidden();
        }
        String sep = reSearchItem.getSep();
        String dirSep = reSearchItem.getDirSep();
        pureSearchThread(se, target, directory, searchFileName, searchDirectory, searchContent, caseSense,
                notSearchExt, showHidden, ext, sep, dirSep);

        isReSearching = false;
        reSearchItem = null;

    }


    /**
     * Sets up the size scale of the GUI.
     */
    private void setUIScale() {

        c1.setPrefWidth(c1.getPrefWidth() * ratio);
        c2.setPrefWidth(c2.getPrefWidth() * ratio);
        c3.setPrefWidth(c3.getPrefWidth() * ratio);
        c4.setPrefWidth(c4.getPrefWidth() * ratio);

        checkCol.setPrefWidth(checkCol.getPrefWidth() * ratio);
        desCol.setPrefWidth(desCol.getPrefWidth() * ratio);
        extCol.setPrefWidth(extCol.getPrefWidth() * ratio);

        rootPane.setPrefWidth(rootPane.getPrefWidth() * ratio);
        rootPane.setPrefHeight(rootPane.getPrefHeight() * ratio);

        h1.setPrefWidth(h1.getPrefWidth() * ratio);
        h2.setPrefWidth(h2.getPrefWidth() * ratio);
        h2.setSpacing(10.0 * ratio);
        h3.setPrefWidth(h3.getPrefWidth() * ratio);

    }


    /**
     * Translates the boolean value to the language given by the LanguageLoader object lanLoader.
     *
     * @param value the boolean value which will be translated.
     * @return the String representing this boolean value in the language of lanLoader.
     */
    private String translateBoolean(boolean value) {
        if (value) {
            return lanLoader.show(509);
        } else {
            return lanLoader.show(510);
        }
    }


    /**
     * Returns whether the text inside TextField object "searchField" is valid for search.
     * <p>
     * This method will return true if there is no more than one directory identifiers in the search field.
     *
     * @return true iff the text inside TextField object "searchField" is valid for search.
     */
    private boolean checkValidInput() {
        try {
            return HelperFunctions.stringCount(searchField.getText(), ConfigLoader.getConfig().get("dir_sep")) <= 1;
        } catch (IOException ioe) {
            return false;
        }

    }


    /**
     * Re-load the language loader.
     */
    public void reloadLanLoader() {
        this.lanLoader = new LanguageLoader();
        fillLanguage();
        fillFormatTable();
        changeTableLanguage();
        if (isCanceled) {
            timeCount.setText(lanLoader.show(36) + " " + lastTime + lanLoader.show(30));
        } else {
            timeCount.setText(lanLoader.show(29) + " " + lastTime + lanLoader.show(30));
        }
        fileCount.setText(fileTable.getItems().size() + lanLoader.show(26));
        stage.setTitle(lanLoader.show(402));
    }


    /**
     * Shows a dialog to user that the path in this history item does not exist.
     */
    private void showPathNotExistDialog() {
        Alert info = new Alert(Alert.AlertType.ERROR);
        DialogPane dp = info.getDialogPane();

        File fontFile = new File("UserSettings/theme/font.css");
        File mainFile = new File("UserSettings/theme/main.css");
        dp.getStylesheets().add("file:///" + fontFile.getAbsolutePath().replace("\\", "/"));
        dp.getStylesheets().add("file:///" + mainFile.getAbsolutePath().replace("\\", "/"));

        dp.getStyleClass().add("myDialog");

        info.setTitle(lanLoader.show(203));
        info.setHeaderText(lanLoader.show(206));
        info.setContentText(lanLoader.show(207));
        info.showAndWait();
    }
}
