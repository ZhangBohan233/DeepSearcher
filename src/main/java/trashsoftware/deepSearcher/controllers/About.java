package trashsoftware.deepSearcher.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.EventLogger;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class About implements Initializable {

    @FXML
    private Label nameLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private Label companyLabel;

    @FXML
    private VBox vbox;

    private String version = " v1.0 Beta5";

    private LanguageLoader lanLoader = new LanguageLoader();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameLabel.setText(lanLoader.show(402));
        versionLabel.setText(version);
        companyLabel.setText(lanLoader.show(401));
        try {
            double ratio = Double.valueOf(ConfigLoader.getConfig().get("scale")) / 100.0;
            vbox.setPrefSize(vbox.getPrefWidth() * ratio, vbox.getPrefHeight() * ratio);

        } catch (IOException ioe) {
            EventLogger.log(ioe, ioe.getMessage(), Level.WARNING);
        }
    }


    /**
     * Returns the current version.
     *
     * @return the current version.
     */
    public String getVersion() {
        return version;
    }
}
