package trashsoftware.deepSearcher.controllers;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.EventLogger;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;
import trashsoftware.deepSearcher.itemUnits.FileDetailCell;
import trashsoftware.deepSearcher.itemUnits.FileRoot;
import trashsoftware.deepSearcher.itemUnits.FileTreeItem;
import trashsoftware.deepSearcher.itemUnits.SpecialFile;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class ExplorerClient implements Initializable {

    @FXML
    private TreeView<File> rootTree;

    @FXML
    private VBox rootBox;

    @FXML
    private HBox upperBox;

    @FXML
    private Button selectButton;

    @FXML
    private HBox leftBox;

    @FXML
    private HBox rightBox;

    @FXML
    private Label currentLabel;

    @FXML
    private TableView<FileDetailCell> table;

    @FXML
    private TableColumn<FileDetailCell, String> nameCol;

    @FXML
    private TableColumn<FileDetailCell, String> typeCol;

    private double uiRatio;

    private LanguageLoader lanLoader = new LanguageLoader();

    private MainPage ctrl;

    private Stage stage;

    private String currentSelection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            uiRatio = Double.valueOf(ConfigLoader.getConfig().get("scale")) / 100;
            setUIScale();
            setTree();
            setTreeListener();
            selectButton.setText(lanLoader.show(1002));
            currentLabel.setText(lanLoader.show(1004) + " " + lanLoader.show(32));
            nameCol.setText(lanLoader.show(4));
            typeCol.setText(lanLoader.show(6));
            setNameColHoverFactory();
            nameCol.setCellValueFactory(new PropertyValueFactory<>("Name1"));
            typeCol.setCellValueFactory(new PropertyValueFactory<>("Name2"));
            setTableListener();
            table.setPlaceholder(new Label());


        } catch (Exception e) {
            EventLogger.log(e, e.getMessage(), Level.SEVERE);
        }

    }


    /**
     * Sets up the main controller of this ExplorerClient object belongs to.
     *
     * @param ctrl the main Controller of the program.
     */
    public void setController(MainPage ctrl) {
        this.ctrl = ctrl;
    }


    /**
     * Sets up the showing stage of this ExplorerClient object.
     *
     * @param stage the showing Stage object.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    /**
     * Sets up the content of the TreeView object "rootTree".
     */
    private void setTree() {
        TreeItem<File> rootNode = new TreeItem<>(new File(System.getenv("COMPUTERNAME")));
        for (File file : File.listRoots()) {
            FileTreeItem rootItems = new FileTreeItem(new SpecialFile(file.getAbsolutePath()));
            rootNode.getChildren().add(rootItems);
        }
        rootTree.setRoot(rootNode);
    }


    /**
     * The action event listener of the Button object "selectButton".
     */
    @FXML
    public void SelectButtonHandler() {
        if (currentSelection == null) {
            ctrl.setStartDirectory(new FileRoot());
        } else {
            ctrl.setStartDirectory(new File(currentSelection));
        }
        stage.close();
    }


    /**
     * Sets up the selection listener of the TableView object "table".
     */
    private void setTableListener() {
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                File file = new File(newValue.getFullPath());
                if (file.isDirectory()) {
                    selectButton.setDisable(false);
                    currentSelection = newValue.getFullPath();
                    currentLabel.setText(lanLoader.show(1004) + " " + currentSelection);
                } else {
                    currentSelection = file.getParent();
                    currentLabel.setText(lanLoader.show(1004) + " " + currentSelection);
                }
            }
        });
    }


    /**
     * Sets up the hover property listener of the TableColumn object "nameCol".
     */
    private void setNameColHoverFactory() {
        nameCol.setCellFactory(new Callback<TableColumn<FileDetailCell, String>, TableCell<FileDetailCell, String>>() {
            @Override
            public TableCell<FileDetailCell, String> call(TableColumn<FileDetailCell, String> param) {
                return new TableCell<FileDetailCell, String>() {
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

                                    table.setTooltip(tp);
                                } else {
                                    table.setTooltip(null);
                                }
                            });
                        }
                    }
                };
            }
        });
    }


    /**
     * Sets up the change listener of the directory tree.
     */
    private void setTreeListener() {
        rootTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectButton.setDisable(false);
                fillTable(newValue);
                if (newValue.getValue().exists()) {
                    currentSelection = newValue.getValue().getAbsolutePath();
                    currentLabel.setText(lanLoader.show(1004) + " " + currentSelection);
                } else {
                    currentSelection = null;
                    currentLabel.setText(lanLoader.show(1004) + " " + lanLoader.show(1001));
                }
            } else {
                selectButton.setDisable(true);
                currentSelection = null;
                currentLabel.setText(lanLoader.show(1004) + " " + lanLoader.show(32));
            }
        });
    }


    /**
     * Fills the file detail table when a directory is selected.
     *
     * @param node the TreeItem object representing this directory.
     */
    private void fillTable(TreeItem<File> node) {
        table.getItems().clear();
        if (node.getValue().exists()) {
            for (File f : Objects.requireNonNull(node.getValue().listFiles())) {
                FileDetailCell fdc;
                if (f.isDirectory()) {
                    fdc = new FileDetailCell(f.getAbsolutePath(), lanLoader.show(3));
                } else {
                    fdc = new FileDetailCell(f.getAbsolutePath(), lanLoader.show(35));
                }
                table.getItems().add(fdc);
            }
        } else {
            for (File d : File.listRoots()) {
                table.getItems().add(new FileDetailCell(d.getAbsolutePath(), lanLoader.show(1005)));
            }
        }
    }


    /**
     * Sets up the UI scale of the ExplorerClient.
     */
    private void setUIScale() {
        upperBox.setSpacing(5.0 * uiRatio);
        upperBox.setPrefHeight(upperBox.getPrefHeight() * uiRatio);
        table.setPrefWidth(280.0 * uiRatio);
        table.setPrefHeight(290.0 * uiRatio);
        rootBox.setPrefSize(rootBox.getPrefWidth() * uiRatio, rootBox.getPrefHeight() * uiRatio);
        leftBox.setPrefWidth(leftBox.getPrefWidth() * uiRatio);
        rightBox.setPrefWidth(rightBox.getPrefWidth() * uiRatio);
        nameCol.setPrefWidth(nameCol.getPrefWidth() * uiRatio);
        typeCol.setPrefWidth(typeCol.getPrefWidth() * uiRatio);
        rootTree.setPrefWidth(rootTree.getPrefWidth() * uiRatio);
    }

}
