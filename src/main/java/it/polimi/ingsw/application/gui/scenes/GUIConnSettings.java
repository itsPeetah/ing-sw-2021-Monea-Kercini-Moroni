package it.polimi.ingsw.application.gui.scenes;

import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.common.listeners.PacketListener;
import it.polimi.ingsw.application.gui.GUIScene;
import it.polimi.ingsw.application.gui.GUIUtility;
import it.polimi.ingsw.network.common.SystemMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class GUIConnSettings implements Initializable, PacketListener {
    public static final int TIMEOUT_TIME = 5000;
    public Button connButton;
    public Button backButton;
    public ImageView loadingFlask;
    private String address;
    private int port;
    public TextField addressTextField;
    public TextField portTextField;

    /**
     * On back button click
     * @param actionEvent
     */
    public void onBackButton(ActionEvent actionEvent) {
        GUIScene.MAIN_MENU.load();
    }

    /**
     * On connect button click
     * @param actionEvent
     */
    public void onConnectClick(ActionEvent actionEvent) {
        disableButtons(true);
        address = addressTextField.getText();
        String portString = portTextField.getText().equals("") ? "0" : portTextField.getText();
        port = Integer.parseInt(portString);
        GUIScene.showLoadingScene();

        GUIUtility.executorService.submit(() -> {
            // If client was previously connected, remove current connection
            if(GameApplication.getInstance().isOnNetwork()) GameApplication.getInstance().closeConnectionWithServer();

            GameApplication.getInstance().connect(address, port);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make the port text field change only with numbers
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // If a non number has been added, remove all the non numbers with empty strings
            if (!newValue.matches("\\d*")) {
                portTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

    }

    @Override
    public void onSystemMessage(SystemMessage type, String additionalContent) {
        Platform.runLater(() -> {
            switch(type) {
                case IN_LOBBY:
                    disableButtons(false);
                    GUIScene.removeActiveScene();
                    GUIUtility.runSceneWithDelay(GUIScene.MAIN_MENU);
                    break;
                case ERR:
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                disableButtons(false);
                                GUIScene.CONN_SETTINGS.load();
                                GUIUtility.showDialog(additionalContent);
                            });
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(timerTask, 500);
                    break;
            }
        });
    }

    /**
     * Set the disable effect of the buttons.
     * @param disabled if true the buttons will be disabled, otherwise they would be clickable.
     */
    private void disableButtons(boolean disabled) {
        connButton.setDisable(disabled);
        backButton.setDisable(disabled);
    }
}
