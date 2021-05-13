package it.polimi.ingsw.application.gui;

import it.polimi.ingsw.application.gui.scenes.GUIMainMenu;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIStage extends Application {
    private static Stage primaryStage;
    private static Alert oldDialog;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Masters of Renaissance");
        setScene(GUIMainMenu.getScene());
        stage.setResizable(false);
        stage.setOnCloseRequest(windowEvent -> stage.close());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void setScene(Scene scene) {
        primaryStage.setScene(scene);
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