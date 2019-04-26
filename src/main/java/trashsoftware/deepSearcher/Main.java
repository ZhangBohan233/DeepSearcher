package trashsoftware.deepSearcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import trashsoftware.deepSearcher.configLoader.ConfigLoader;
import trashsoftware.deepSearcher.configLoader.EventLogger;
import trashsoftware.deepSearcher.configLoader.LanguageLoader;
import trashsoftware.deepSearcher.controllers.MainPage;

import java.io.IOException;
import java.util.logging.Level;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            ConfigLoader.checkFiles();
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/trashsoftware/deepSearcher/fxml/mainPage.fxml"));

            Parent root = loader.load();
            LanguageLoader lanLoader = new LanguageLoader();
            primaryStage.setTitle(lanLoader.show(402));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            primaryStage.getIcons().add(new Image(getClass()
                    .getResource("/trashsoftware/deepSearcher/images/icon.png").toExternalForm()));

            MainPage ctrl = loader.getController();
            ctrl.setStage(primaryStage);

            primaryStage.show();
        } catch (IOException ioe) {

            String message = ioe.getLocalizedMessage();
            EventLogger.log(ioe, message, Level.WARNING);
        } catch (Exception e) {

            String message = e.getLocalizedMessage();
            EventLogger.log(e, message, Level.SEVERE);
            System.exit(0);

        }
    }


}
