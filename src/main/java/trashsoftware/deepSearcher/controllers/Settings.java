package trashsoftware.deepSearcher.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.EventLogger;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;
import trashsoftware.deepSearcher.itemUnits.SettingItem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class Settings implements Initializable {

    @FXML
    private TreeView<SettingItem> rootTree;

    @FXML
    private GridPane rootPane;

    @FXML
    private ScrollPane rightPane;

    @FXML
    private Button confirm;

    @FXML
    private Button cancel;

    private LanguageLoader lanLoader = new LanguageLoader();

    private Stage stage;

    private ComboBox<String> languageChooser;

    private ComboBox<String> uiScaleChooser;

//    private ComboBox<String> fontSizeChooser;

    private TextField addFormatText;

    private ListView<String> excludeDirList = new ListView<>();

    private ListView<String> excludeFormatList = new ListView<>();

    private HashMap<String, String> allLanguages;

    private Button addDir = new Button("+");

    private Button removeDir = new Button("-");

    private Button addFormat = new Button("+");

    private Button removeFormat = new Button("-");

    private CheckBox caseSensitive = new CheckBox();

    private CheckBox keepOrig = new CheckBox();

    private CheckBox showHidden = new CheckBox();

    private CheckBox notExtension = new CheckBox();

//    private CheckBox customFont = new CheckBox();

    private CheckBox useCustomChooser = new CheckBox();

    private double uiRatio;

    private MainPage ctrl;

    private Pane exclusionPane;

    private Pane otherSearchPrefPane;

    private TextField andField;

    private TextField dirSepField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            uiRatio = Double.valueOf(ConfigLoader.getConfig().get("scale")) / 100;
            setUIScale();

            fillLanguage();
            addTreeItemListener();

            exclusionPane = getExclusionPane();
            otherSearchPrefPane = getOtherSearchPrefPane();

            setTree();

            rightPane.setContent(getBlankPane());
            rootPane.setHgap(5.0 * uiRatio);
            rightPane.setStyle("-fx-background-color:transparent;");

        } catch (IOException ioe) {
            EventLogger.log(ioe, ioe.getMessage(), Level.WARNING);
        }

    }

    /**
     * Set up the primary stage of this controller.
     *
     * @param stage the primary stage.
     */
    void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Set up the controller of the parent window.
     *
     * @param ctrl the controller of main program.
     */
    void setController(MainPage ctrl) {
        this.ctrl = ctrl;
    }


    /**
     * Adds the mouse-click listener of the TreeView "rootTree" to respond the user's click.
     */
    private void addTreeItemListener() {
        rootTree.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                refreshPane(newSelection.getValue());
            }
        });
    }


    /**
     * Fills the ScrollPane object "rightPane" with the current selected setting item.
     *
     * @param selection the current selected SettingItem.
     */
    private void refreshPane(SettingItem selection) {
        if (selection.showAble()) {
            rightPane.setContent(selection.getPane());
        }
    }


    /**
     * Fills all components in this window wil the language of the LanguageLoader object "lanLoader".
     */
    private void fillLanguage() {
        confirm.setText(lanLoader.show(301));
        cancel.setText(lanLoader.show(302));
    }


    /**
     * Sets up the TreeView "rootTree" of the settings window.
     */
    private void setTree() {
        try {
            TreeItem<SettingItem> rootNode = new TreeItem<>(new SettingItem(lanLoader.show(15)));

            // Search Pref
            TreeItem<SettingItem> searchPref = new TreeItem<>(new SettingItem(lanLoader.show(313),
                    getSearchPrefPreviewPane()));
            SettingItem exclusion = new SettingItem(lanLoader.show(305), exclusionPane);
            TreeItem<SettingItem> exclude = new TreeItem<>(exclusion);
            SettingItem otherSearchPref = new SettingItem(lanLoader.show(312), otherSearchPrefPane);
            TreeItem<SettingItem> otherSearch = new TreeItem<>(otherSearchPref);

            searchPref.getChildren().add(exclude);
            searchPref.getChildren().add(otherSearch);

            // Display
            TreeItem<SettingItem> display = new TreeItem<>(new SettingItem(lanLoader.show(311), getDisplayPane()));

            // General
            TreeItem<SettingItem> general = new TreeItem<>(new SettingItem(lanLoader.show(304), getGeneralPane()));

            rootNode.getChildren().add(searchPref);
            rootNode.getChildren().add(display);
            rootNode.getChildren().add(general);

            rootNode.setExpanded(true);
            rootTree.setRoot(rootNode);
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }

    }


    /**
     * Action event handler of Button "confirm".
     * <p>
     * This method saves all changes to the pref file or theme file.
     *
     * @throws IOException if the config file is not writable.
     */
    @FXML
    public void confirmButtonAction() throws IOException {

        ConfigLoader.setConfig("case_sen", String.valueOf(caseSensitive.isSelected()));
        ConfigLoader.setConfig("keep_orig", String.valueOf(keepOrig.isSelected()));
        ConfigLoader.setConfig("not_ext", String.valueOf(notExtension.isSelected()));
        ConfigLoader.setConfig("show_hidden", String.valueOf(showHidden.isSelected()));
        ConfigLoader.setConfig("custom_chooser", String.valueOf(useCustomChooser.isSelected()));

        boolean illegal = false;
        ArrayList<String> illegalForms = new ArrayList<>();
        illegalForms.add(" ");

        // Check whether the andField was changed and whether the content is legal.
        if (andField != null && !andField.getText().equals("")) {
            if (illegalForms.contains(andField.getText())) {
                illegal = true;
            } else {
                ConfigLoader.setConfig("and_sep", andField.getText());
            }
        }

        // Check whether the dirSepField was changed and whether the content is legal.
        if (dirSepField != null && !dirSepField.getText().equals("")) {
            if (illegalForms.contains(dirSepField.getText())) {
                illegal = true;
            } else {
                ConfigLoader.setConfig("dir_sep", dirSepField.getText());
            }
        }

        if (illegal) {
            showIllegalChangeDialog();
            return;
        }

        boolean changed = false;

        // Save language if it was changed
        try {
            String currentLanguage = lanLoader.show(0);
            String selectLanguage = languageChooser.getSelectionModel().getSelectedItem();
            if (!currentLanguage.equals(selectLanguage)) {
                // Language changed
                ConfigLoader.setConfig("language", allLanguages.get(
                        languageChooser.getSelectionModel().getSelectedItem()));
                resetLanguage();
            }
        } catch (NullPointerException npe) {
            //
        }

        // Save and apply UI scale if it was changed.
        try {
            String currentScale = ConfigLoader.getConfig().get("scale");
            String selectScale = uiScaleChooser.getSelectionModel().getSelectedItem().substring(
                    0, uiScaleChooser.getSelectionModel().getSelectedItem().length() - 1);
            if (!currentScale.equals(selectScale)) {
                ConfigLoader.setConfig("scale", selectScale);
                changed = true;
            }
        } catch (NullPointerException npe) {
            //
        }

        if (changed) {
            showRestartInfo();
        }
        stage.close();
    }


    /**
     * Re-set the language loader of the main controller.
     */
    private void resetLanguage() {
        lanLoader = new LanguageLoader();
        ctrl.reloadLanLoader();
    }


    /**
     * Action handler of Button "cancel".
     */
    @FXML
    public void cancelButtonAction() {
        stage.close();
    }


    /**
     * Show a dialog to let the user know the input is illegal.
     */
    private void showIllegalChangeDialog() {
        Alert warning = new Alert(Alert.AlertType.WARNING);
        DialogPane dp = warning.getDialogPane();

        warning.setTitle(lanLoader.show(333));
        warning.setHeaderText(lanLoader.show(334));
        warning.setContentText(lanLoader.show(335));

        Stage s = (Stage) dp.getScene().getWindow();
        s.getIcons().add(this.stage.getIcons().get(0));

        warning.showAndWait();
    }


    /**
     * Show a dialog to tell user to restart the program.
     */
    private void showRestartInfo() {
        Alert info = new Alert(Alert.AlertType.CONFIRMATION);
        LanguageLoader newLanLoader = new LanguageLoader();
        DialogPane dp = info.getDialogPane();

        info.setTitle(newLanLoader.show(316));
        info.setHeaderText(newLanLoader.show(315));
        info.setContentText(null);

        Stage s = (Stage) dp.getScene().getWindow();
        s.getIcons().add(this.stage.getIcons().get(0));

        ButtonType restartNowButton = new ButtonType(newLanLoader.show(324), ButtonBar.ButtonData.YES);
        ButtonType restartLaterButton = new ButtonType(newLanLoader.show(325), ButtonBar.ButtonData.NO);

        info.getButtonTypes().setAll(restartNowButton, restartLaterButton);

        Optional result = info.showAndWait();
        if (result.isPresent() && result.get().equals(restartNowButton)) {
            System.exit(0);
        }
    }


    /**
     * Show a confirmation dialog.
     */
    private boolean showConfirmDialog(String text) {
        Alert info = new Alert(Alert.AlertType.CONFIRMATION);
        DialogPane dp = info.getDialogPane();

        info.setTitle(lanLoader.show(204));
        info.setHeaderText(text);
        info.setContentText(lanLoader.show(205));

        Stage s = (Stage) dp.getScene().getWindow();
        s.getIcons().add(this.stage.getIcons().get(0));

        Optional result = info.showAndWait();

        return result.isPresent() && result.get().equals(ButtonType.OK);
    }


    /**
     * Get the Pane object Containing "Display" options.
     *
     * @return a Pane of Display options.
     * @throws IOException if config file cannot be loaded.
     */
    private VBox getDisplayPane() throws IOException {

        VBox vbox = new VBox();
        vbox.setPrefSize(240.0 * uiRatio, 350.0 * uiRatio);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10.0 * uiRatio);

        HBox display = makeSeparator(311, 240.0);
        HBox displayOptions = makeSeparator(319, 180.0);

        Label languageText = new Label(lanLoader.show(303));

        allLanguages = LanguageLoader.getAllLanguages();
        ObservableList<String> options = FXCollections.observableArrayList();
        options.addAll(allLanguages.keySet());
        languageChooser = new ComboBox<>(options);
        languageChooser.getSelectionModel().select(lanLoader.show(0));

        vbox.getChildren().addAll(display, displayOptions);

        vbox.getChildren().add(new Label(lanLoader.show(317)));
        ObservableList<String> scales = FXCollections.observableArrayList();
        scales.add("75%");
        scales.add("100%");
        scales.add("125%");
        scales.add("150%");
        scales.add("200%");
        scales.add("300%");
        uiScaleChooser = new ComboBox<>(scales);
        uiScaleChooser.getSelectionModel().select(ConfigLoader.getConfig().get("scale") + "%");
        vbox.getChildren().add(uiScaleChooser);

//        HBox customFontBox = new HBox();
//        customFontBox.setAlignment(Pos.CENTER);
//        customFontBox.setSpacing(10.0 * uiRatio);
//        customFont.setText(lanLoader.show(321));
//
//        customFontBox.getChildren().add(customFont);

//        customFontBox.getChildren().add(fontSizeChooser);
//        vbox.getChildren().add(customFontBox);

        HBox languageSeparator = makeSeparator(320, 180.0);
        vbox.getChildren().addAll(languageSeparator, languageText, languageChooser);

//        customFont.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
//                fontSizeChooser.setDisable(false);
//            } else {
//                fontSizeChooser.setDisable(true);
//            }
//        });

        return vbox;

    }


    /**
     * Get the Pane object containing search exclusion options.
     *
     * @return a Pane of search exclusion options.
     */
    private VBox getExclusionPane() {
        VBox pane = new VBox();

        pane.setLayoutX(10.0 * uiRatio);
        pane.setAlignment(Pos.TOP_CENTER);

        excludeDirList.setPrefSize(210.0 * uiRatio, 150 * uiRatio);
        addDirListListener();
        refreshExcludeDirList();
        excludeFormatList.setPrefSize(210.0 * uiRatio, 150 * uiRatio);
        addFormatListListener();
        refreshExcludeFormatList();

        HBox hbox1 = new HBox();
        VBox vbox1 = new VBox();

        addDir.setPrefWidth(25.0 * uiRatio);
        addDir.setOnAction(event -> addDirAction());
        removeDir.setPrefWidth(25.0 * uiRatio);
        removeDir.setOnAction(event -> removeDirAction());
        removeDir.setDisable(true);

        vbox1.getChildren().add(addDir);
        vbox1.getChildren().add(removeDir);

        hbox1.getChildren().add(excludeDirList);
        hbox1.getChildren().add(vbox1);

        HBox hbox2 = new HBox();
        VBox vbox2 = new VBox();

        addFormat.setPrefWidth(25.0 * uiRatio);
        addFormat.setOnAction(event -> addFormatAction());
        removeFormat.setPrefWidth(25.0 * uiRatio);
        removeFormat.setOnAction(event -> removeFormatAction());
        removeFormat.setDisable(true);

        vbox2.getChildren().add(addFormat);
        vbox2.getChildren().add(removeFormat);

        hbox2.getChildren().add(excludeFormatList);
        hbox2.getChildren().add(vbox2);

        HBox header = makeSeparator(305, 240.0);
        pane.getChildren().addAll(header,
                new Label(lanLoader.show(306)),
                hbox1,
                new Label(lanLoader.show(307)),
                hbox2);

        return pane;
    }


    /**
     * Get the Pane object containing other search options.
     *
     * @return a Pane of other search options.
     * @throws IOException if config file cannot be loaded.
     */
    private VBox getOtherSearchPrefPane() throws IOException {
        VBox pane = new VBox();

        pane.setPrefSize(240.0 * uiRatio, 350.0 * uiRatio);
        pane.setAlignment(Pos.CENTER);
        pane.setSpacing(10.0 * uiRatio);
        pane.setFillWidth(false);

        HBox other = makeSeparator(312, 240.0);

        VBox subPane = new VBox();
        subPane.setSpacing(10.0 * uiRatio);
        subPane.setAlignment(Pos.TOP_LEFT);

        caseSensitive.setText(lanLoader.show(310));
        caseSensitive.setSelected(Boolean.valueOf(ConfigLoader.getConfig().get("case_sen")));
        subPane.getChildren().add(caseSensitive);

        notExtension.setText(lanLoader.show(329));
        notExtension.setSelected(Boolean.valueOf(ConfigLoader.getConfig().get("not_ext")));
        subPane.getChildren().add(notExtension);

        keepOrig.setText(lanLoader.show(314));
        keepOrig.setSelected(Boolean.valueOf(ConfigLoader.getConfig().get("keep_orig")));
        subPane.getChildren().add(keepOrig);

        showHidden.setText(lanLoader.show(336));
        showHidden.setSelected(Boolean.valueOf(ConfigLoader.getConfig().get("show_hidden")));
        subPane.getChildren().add(showHidden);

        pane.getChildren().add(other);
        pane.getChildren().add(subPane);

        pane.getChildren().add(makeSeparator(326, 180.0));

        VBox subPane2 = new VBox();
        subPane2.setSpacing(10.0 * uiRatio);
        subPane2.setAlignment(Pos.TOP_CENTER);

        GridPane sepPane = new GridPane();
        sepPane.setHgap(10.0 * uiRatio);
        sepPane.setAlignment(Pos.CENTER);
        pane.setPrefWidth(sepPane.getPrefWidth());

        andField = new TextField();
        andField.setPromptText(lanLoader.show(327) + " " + ConfigLoader.getConfig().get("and_sep"));
        andField.setPrefWidth(100.0 * uiRatio);
        sepPane.add(new Label(lanLoader.show(330)), 0, 0);
        sepPane.add(andField, 1, 0);

        Label help = new Label("(?)");
        help.hoverProperty().addListener(event -> {
            Tooltip tp = new Tooltip();
            tp.setText(lanLoader.show(328));
            help.setTooltip(tp);
        });
        sepPane.add(help, 2, 0);

        dirSepField = new TextField();
        dirSepField.setPromptText(lanLoader.show(327) + " " + ConfigLoader.getConfig().get("dir_sep"));
        dirSepField.setPrefWidth(100.0 * uiRatio);
        sepPane.add(new Label(lanLoader.show(331)), 0, 1);
        sepPane.add(dirSepField, 1, 1);

        Label help2 = new Label("(?)");
        help2.hoverProperty().addListener(event -> {
            Tooltip tp = new Tooltip();
            tp.setText(lanLoader.show(332));
            help2.setTooltip(tp);
        });
        sepPane.add(help2, 2, 1);

        pane.getChildren().add(sepPane);

        return pane;
    }


    /**
     * Get the Pane object containing general preferences.
     *
     * @return a Pane of general options.
     */
    private VBox getGeneralPane() throws IOException {
        VBox pane = new VBox();
        pane.setPrefSize(240.0 * uiRatio, 350.0 * uiRatio);
        pane.setAlignment(Pos.CENTER);
        pane.setSpacing(10.0 * uiRatio);
        pane.setFillWidth(true);

        pane.getChildren().add(makeSeparator(304, 240.0));

        pane.getChildren().add(makeSeparator(339, 180.0));
        HBox sepPane = new HBox();
        sepPane.setSpacing(10.0 * uiRatio);
        sepPane.setAlignment(Pos.CENTER);

        useCustomChooser.setSelected(Boolean.valueOf(ConfigLoader.getConfig().get("custom_chooser")));
        useCustomChooser.setText(lanLoader.show(337));
        sepPane.getChildren().add(useCustomChooser);
        Label help = new Label("(?)");
        help.hoverProperty().addListener(event -> {
            Tooltip tp = new Tooltip();
            tp.setText(lanLoader.show(338));
            help.setTooltip(tp);
        });
        if (!System.getProperties().getProperty("os.name").contains("Windows")) {
            useCustomChooser.setDisable(true);
            useCustomChooser.setSelected(false);
        }

        sepPane.getChildren().add(help);
        pane.getChildren().add(sepPane);

        pane.getChildren().add(makeSeparator(340, 180));

        Button restore = new Button(lanLoader.show(322));
        Button clearHis = new Button(lanLoader.show(323));

        restore.setOnAction(event -> {
            try {
                if (showConfirmDialog(lanLoader.show(322))) {
                    restoreSettings();
                }
            } catch (IOException ioe) {
                EventLogger.log(ioe, ioe.getMessage(), Level.WARNING);
            }
        });

        clearHis.setOnAction(event -> {
            try {
                if (showConfirmDialog(lanLoader.show(323))) {
                    ConfigLoader.deleteFile("UserSettings" + File.separator + "SearchHistory.txt");
                    ConfigLoader.checkFiles();
                    ctrl.addSubMenuToHistory();
                }
            } catch (IOException ioe) {
                EventLogger.log(ioe, ioe.getMessage(), Level.WARNING);
            }
        });

        pane.getChildren().add(restore);
        pane.getChildren().add(clearHis);

        return pane;
    }


    /**
     * Get the preview pane of searching preferences TreeItems.
     *
     * @return the preview pane of searching preferences.
     */
    private VBox getSearchPrefPreviewPane() {
        VBox pane = new VBox();
        pane.setSpacing(5.0 * uiRatio);
        pane.setLayoutY(10.0 * uiRatio);
        pane.setAlignment(Pos.TOP_LEFT);

        Hyperlink excludeLink = new Hyperlink();
        excludeLink.setText(lanLoader.show(313) + ": " + lanLoader.show(305));
        excludeLink.setOnAction(event -> refreshPane(new SettingItem(lanLoader.show(305), exclusionPane)));

        pane.getChildren().add(excludeLink);

        Hyperlink otherLink = new Hyperlink();
        otherLink.setText(lanLoader.show(313) + ": " + lanLoader.show(312));
        otherLink.setOnAction(event -> refreshPane(new SettingItem(lanLoader.show(312), otherSearchPrefPane)));

        pane.getChildren().add(otherLink);

        return pane;
    }


    /**
     * Return a Pane object contains only a placeholder Label on the center.
     *
     * @return a nearly-blank Pane object.
     */
    private Pane getBlankPane() {
        Pane p = new Pane();

        Label placeHolder = new Label(lanLoader.show(15));
        placeHolder.setLayoutX(105.0 * uiRatio);
        placeHolder.setLayoutY(165.0 * uiRatio);

        p.getChildren().add(placeHolder);

        return p;
    }


    /**
     * Action event handler of restore all settings.
     *
     * @throws IOException if the config file is not writable.
     */
    private void restoreSettings() throws IOException {
        ConfigLoader.deleteFile("UserSettings" + File.separator + "pref.ini");
        ConfigLoader.checkFiles();
        showRestartInfo();
        stage.close();
    }


    /**
     * Action event handler of adding a new exclusion directory.
     */
    private void addDirAction() {
        try {
            DirectoryChooser dirChoose = new DirectoryChooser();

            File selectedDirectory = dirChoose.showDialog(null);
            if (selectedDirectory != null && !ConfigLoader.getExcludeDirs().contains(selectedDirectory.getAbsolutePath())) {
                ConfigLoader.addExcludeDir(selectedDirectory.getAbsolutePath());
            }

            refreshExcludeDirList();
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }

    }


    /**
     * Action event handler of deleting an exclusion directory.
     */
    private void removeDirAction() {
        try {
            String selected = excludeDirList.getSelectionModel().getSelectedItem();
            ConfigLoader.removeExcludeDir(selected);
            refreshExcludeDirList();
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }
    }


    /**
     * Clear and re-fill the exclusion directory list.
     */
    private void refreshExcludeDirList() {
        excludeDirList.getItems().clear();
        List<String> list = ConfigLoader.getExcludeDirs();
        excludeDirList.getItems().addAll(list);
    }


    /**
     * Action event handler of adding a new format to exclusion format list.
     */
    private void addFormatAction() {
        Pane root = new Pane();
        Stage dialog = new Stage();
        Scene scene = new Scene(root);
        dialog.setTitle(lanLoader.show(308));
        dialog.setScene(scene);
        dialog.setHeight(80.0 * uiRatio);
        dialog.setWidth(240.0 * uiRatio);
        dialog.setResizable(false);

        dialog.getIcons().add(this.stage.getIcons().get(0));

        addFormatText = new TextField();

        Button but = new Button(lanLoader.show(309));

        but.setOnAction(event -> addFormatToList(dialog));
        addFormatText.setOnAction(event -> addFormatToList(dialog));

        HBox hb = new HBox();
        hb.getChildren().add(addFormatText);
        hb.getChildren().add(but);
        hb.setAlignment(Pos.CENTER);
        hb.setPrefSize(240.0 * uiRatio, 40.0 * uiRatio);
        root.getChildren().add(hb);

        dialog.show();
    }


    /**
     * Adds the new-created format to the excluded formats' record file.
     *
     * @param dialog the input dialog of adding a new format.
     */
    private void addFormatToList(Stage dialog) {
        String text = addFormatText.getText();
        if (text.length() > 0) {
            if (!text.startsWith(".")) {
                text = "." + text;
            }
            try {
                ConfigLoader.addExcludeFormat(text);
            } catch (IOException ioe) {
                String message = ioe.getLocalizedMessage();
                EventLogger.log(ioe, message, Level.WARNING);
            }
            refreshExcludeFormatList();
            dialog.close();
        }
    }


    /**
     * Action event handler of deleting a format from the exclusion format list.
     */
    private void removeFormatAction() {
        try {
            String selected = excludeFormatList.getSelectionModel().getSelectedItem();
            ConfigLoader.removeExcludeFormat(selected);
            refreshExcludeFormatList();
        } catch (IOException ioe) {
            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        }
    }


    /**
     * Clear and re-fill the exclusion format list.
     */
    private void refreshExcludeFormatList() {
        excludeFormatList.getItems().clear();
        List<String> list = ConfigLoader.getExclusionFormats();
        excludeFormatList.getItems().addAll(list);
    }


    /**
     * Adds listener for the selection event of exclusion directories list (ListView object "excludeDirList").
     */
    private void addDirListListener() {
        excludeDirList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                removeDir.setDisable(false);
            } else {
                removeDir.setDisable(true);
            }
        });
    }


    /**
     * Adds listener for the selection event of exclusion formats list (ListView object "excludeFormatList").
     */
    private void addFormatListListener() {
        excludeFormatList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                removeFormat.setDisable(false);
            } else {
                removeFormat.setDisable(true);
            }
        });
    }


    /**
     * Sets up the size scale of the GUI.
     */
    private void setUIScale() {
        rootPane.setPrefWidth(rootPane.getPrefWidth() * uiRatio);
        rootPane.setPrefHeight(rootPane.getPrefHeight() * uiRatio);
        rightPane.setPrefWidth(rightPane.getPrefWidth() * uiRatio);
        rightPane.setPrefHeight(rightPane.getPrefHeight() * uiRatio);

    }

    private HBox makeSeparator(int languageCode, double defaultWidth) {
        Separator sep1 = new Separator();
        sep1.setPrefWidth(defaultWidth * uiRatio / 3.2);
        Separator sep2 = new Separator();
        sep2.setPrefWidth(defaultWidth * uiRatio / 3.2);
        Label label = new Label(lanLoader.show(languageCode));
        HBox hBox = new HBox(sep1, label, sep2);
        hBox.setPrefWidth(defaultWidth * uiRatio);
        hBox.setSpacing(10.0);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }
}
