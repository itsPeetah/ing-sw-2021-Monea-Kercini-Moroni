package it.polimi.ingsw.application.gui.scenes;

import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.common.GameApplicationState;
import it.polimi.ingsw.application.common.listeners.PacketListener;
import it.polimi.ingsw.application.gui.GUIScene;
import it.polimi.ingsw.application.gui.GUIUtility;
import it.polimi.ingsw.controller.model.messages.Message;
import it.polimi.ingsw.network.common.NetworkPacket;
import it.polimi.ingsw.network.common.NetworkPacketType;
import it.polimi.ingsw.network.common.SystemMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class GUIMPSelection implements PacketListener, Initializable {
    public Button joinButt;
    public Button createButt;
    public Button backButt;
    public Button rejoinButt;
    public TextField userTextField;
    public TextField roomTextField;
    public ChoiceBox numberChoiceBox;
    private TimerTask timeoutTask;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numberChoiceBox.getItems().addAll(Arrays.asList(1, 2, 3, 4));
        numberChoiceBox.setValue(4);
    }

    public void onCreateClick(ActionEvent actionEvent) {
        System.out.println("create pressed ");
        performSelection(SystemMessage.CREATE_ROOM);
    }

    public void onJoinClick(ActionEvent actionEvent) {
        System.out.println("join pressed ");
        performSelection(SystemMessage.JOIN_ROOM);
    }

    public void onReJoinClick(ActionEvent actionEvent) {
        System.out.println("rejoin pressed ");
        performRejoin();
    }

    @FXML
    public void onBackButton(ActionEvent actionEvent) {
        GUIScene.GAME_MODE_SELECTION.load();
    }

    private void performSelection(SystemMessage gameLobbyMessage) {
        String username = userTextField.getText();
        String room = roomTextField.getText();
        Integer playersNumber = (Integer)numberChoiceBox.getValue();
        System.out.println("GUIMPSelection.performSelection: " + playersNumber);

        GameApplication.getInstance().setRoomName(username);
        GameApplication.getInstance().setUserNickname(room);

        setButtonsDisabled(true);
        GUIScene.showLoadingScene();

        Timer timer = new Timer();

        timeoutTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    System.out.println("GUIMPSelection: no room found");
                    setButtonsDisabled(false);
                    Platform.runLater(GUIScene.MP_SELECTION::load);
                    // TODO improve by removing this task and add listener for network messages
                });
            }
        };
        timer.schedule(timeoutTask, GUIConnSettings.TIMEOUT_TIME);

        GUIUtility.executorService.submit(() -> {
            GameApplication.getInstance().out("Processing request, please wait.");
            String messageContent = gameLobbyMessage.addBody(room + " " + username + (gameLobbyMessage == SystemMessage.CREATE_ROOM ? " " + playersNumber : ""));
            NetworkPacket np = new NetworkPacket(NetworkPacketType.SYSTEM, messageContent);
            GameApplication.getInstance().setApplicationState(GameApplicationState.CONNECTING_TO_ROOM);
            GameApplication.getInstance().sendNetworkPacket(np);

        });
    }

    private void performRejoin() {
        NetworkPacket np = new NetworkPacket(NetworkPacketType.SYSTEM, SystemMessage.REJOIN_ROOM.addBody(GameApplication.getInstance().getUserId()));
        GameApplication.getInstance().setApplicationState(GameApplicationState.CONNECTING_TO_ROOM);
        GameApplication.getInstance().sendNetworkPacket(np);
    }

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public void onSystemMessage(SystemMessage type, String additionalContent) {
        Platform.runLater(() -> {
            setButtonsDisabled(false);
            switch(type) {
                case IN_ROOM:
                    GUIUtility.runSceneWithDelay(GUIScene.MP_ROOM, 500);
                    break;
                case IN_GAME:
                    GUIUtility.runSceneWithDelay(GUIScene.MAIN_GAME, 500);
                    break;
            }
        });
    }

    private void setButtonsDisabled(boolean disabled) {
        joinButt.setDisable(disabled);
        createButt.setDisable(disabled);
        backButt.setDisable(disabled);
    }
}
