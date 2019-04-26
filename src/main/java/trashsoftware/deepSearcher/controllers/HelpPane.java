package trashsoftware.deepSearcher.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.EventLogger;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;
import trashsoftware.deepSearcher.util.HelperFunctions;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class HelpPane implements Initializable {

    @FXML
    private Label header;

    @FXML
    private VBox rootBox;

    @FXML
    private javafx.scene.control.ScrollPane rootPane;

    private LanguageLoader lanLoader = new LanguageLoader();

    private double uiRatio;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            uiRatio = Double.valueOf(ConfigLoader.getConfig().get("scale")) / 100;
            setUIScale();

            header.setText(lanLoader.show(801));

            rootPane.setContent(getShowingPane());
        } catch (IOException ioe) {
            EventLogger.log(ioe, ioe.getMessage(), Level.INFO);
        }
    }


    /**
     * Returns the pane that is the content the ScrollPane object rootPane.
     *
     * @return the content of the rootPane.
     * @throws IOException if the path is neither Windows form nor Unix form.
     */
    private VBox getShowingPane() throws IOException {
        VBox pane = new VBox();
        pane.setPrefWidth(rootPane.getPrefWidth() - 20 * uiRatio);
        pane.setAlignment(Pos.CENTER);
        pane.setSpacing(10.0 * uiRatio);

        ImageView iv = new ImageView();
        Image image = new Image(getClass()
                .getResource("/trashsoftware/deepSearcher/helps/" + lanLoader.show(901)).toExternalForm());
        iv.setImage(image);
        iv.setFitWidth(500 * uiRatio);
        iv.setPreserveRatio(true);

        pane.getChildren().add(new Label(lanLoader.show(802)));
        pane.getChildren().add(iv);
        pane.getChildren().add(new Label(lanLoader.show(803)));

        pane.getChildren().add(new Label("----------------------------------------"));

        ImageView iv2 = new ImageView();
        Image image2 = new Image(getClass()
                .getResource("/trashsoftware/deepSearcher/helps/" + lanLoader.show(902)).toExternalForm());
        iv2.setImage(image2);
        iv2.setFitWidth(500 * uiRatio);
        iv2.setPreserveRatio(true);
        pane.getChildren().add(new Label(lanLoader.show(804)));
        pane.getChildren().add(new Label(lanLoader.show(805)));
        pane.getChildren().add(iv2);

        pane.getChildren().add(new Label("----------------------------------------"));

        ImageView iv3 = new ImageView();
        Image image3 = new Image(getClass()
                .getResource("/trashsoftware/deepSearcher/helps/" + lanLoader.show(903)).toExternalForm());
        iv3.setImage(image3);
        iv3.setFitWidth(500 * uiRatio);
        iv3.setPreserveRatio(true);

        pane.getChildren().add(new Label(lanLoader.show(806)));
        pane.getChildren().add(new Label(lanLoader.show(807)));
        pane.getChildren().add(iv3);
        Label lab1 = new Label(lanLoader.show(808));
        lab1.setTextAlignment(TextAlignment.CENTER);
        pane.getChildren().add(lab1);
        Label lab2 = new Label(lanLoader.show(810));
        lab2.setTextAlignment(TextAlignment.CENTER);
        pane.getChildren().add(lab2);
        pane.getChildren().add(new Label(lanLoader.show(809)));
        Label lab3 = new Label(lanLoader.show(811));
        lab3.setTextAlignment(TextAlignment.CENTER);
        pane.getChildren().add(lab3);

        return pane;
    }


    /**
     * Sets up the size scale of the GUI.
     */
    private void setUIScale() {
        rootPane.setPrefWidth(rootPane.getPrefWidth() * uiRatio);
        rootPane.setPrefHeight(rootPane.getPrefHeight() * uiRatio);

        rootBox.setPrefWidth(rootPane.getPrefWidth());
        rootBox.setPrefHeight(rootPane.getPrefHeight());

    }

}
