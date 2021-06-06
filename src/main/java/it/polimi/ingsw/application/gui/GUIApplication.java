package it.polimi.ingsw.application.gui;

import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.gui.scenes.GUIMainGame;
import it.polimi.ingsw.model.cards.CardManager;
import it.polimi.ingsw.model.singleplayer.SoloActionTokens;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class GUIApplication extends Application {
    private static Alert oldDialog;
    public static final String ICON_PATH = "src/main/resources/images/other/calamaio.png";

    @Override
    public void start(Stage stage) throws Exception {
        CardManager.loadImages();
        Materials.init();
        SoloActionTokens.init();
        GUIScene.init();
        GUIMainGame.init();
        stage.setTitle("Masters of Renaissance");
        stage.setScene(GUIScene.getScene());
        GUIScene.MAIN_MENU.load();
        stage.setResizable(false);
        File file = new File(ICON_PATH);
        Image iconImage = new Image(file.toURI().toString());
        stage.getIcons().add(iconImage);
        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void showDialog(String message) {
        Platform.runLater(() -> {
            if(oldDialog != null) {
                oldDialog.close();
            }
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
            oldDialog = alert;
        });
    }
}
