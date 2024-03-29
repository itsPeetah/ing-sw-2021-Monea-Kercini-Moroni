package it.polimi.ingsw.application.gui.scenes;

import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.common.GameApplicationState;
import it.polimi.ingsw.application.common.listeners.PacketListener;
import it.polimi.ingsw.application.gui.GUIChat;
import it.polimi.ingsw.application.gui.GUIObserverScene;
import it.polimi.ingsw.application.gui.GUIScene;
import it.polimi.ingsw.application.gui.GUIUtility;
import it.polimi.ingsw.controller.model.messages.Message;
import it.polimi.ingsw.network.common.NetworkPacket;
import it.polimi.ingsw.network.common.NetworkPacketType;
import it.polimi.ingsw.network.common.SystemMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class GUIMPRoom implements PacketListener, GUIObserverScene {

    public static ObservableList<String> observablePlayersList = FXCollections.observableArrayList();

    public Label room_name;
    public Button startButt;
    public Button leaveButton;

    @FXML
    private ListView<String> playersListView;

    @FXML
    private TextField textField;

    @FXML
    private ListView<String> chatListView;

    /**
     * On start button click.
     */
    public void onStartClick() {
        setButtonsDisabled(true);
        String messageContent = SystemMessage.START_ROOM.addBody(GameApplication.getInstance().getRoomName() + " " + GameApplication.getInstance().getUserNickname());
        NetworkPacket np = new NetworkPacket(NetworkPacketType.SYSTEM, messageContent);
        GameApplication.getInstance().sendNetworkPacket(np);
    }

    /**
     * On leave button click.
     * @param actionEvent
     */
    public void onLeaveClick(ActionEvent actionEvent) {
        GUIUtility.handleLeaveGame();
    }

    /**
     * Send the message after enter is pressed.
     * @param keyEvent
     */
    public void sendMessage(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            String message = textField.getText();
            textField.clear();
            GUIChat.sendMessage(message);
        }
    }

    @Override
    public void onSystemMessage(SystemMessage type, String additionalContent) {
        Platform.runLater(() -> {
            switch(type) {
                case QUIT:
                    GUIUtility.handleServerQuit();
                    break;
                case START_ROOM:
                    GUIScene.showLoadingScene();
                    GUIUtility.runSceneWithDelay(GUIScene.PRE_GAME);
                    break;
                case IN_GAME:
                    GUIScene.showLoadingScene();
                    GUIUtility.runSceneWithDelay(GUIScene.MAIN_GAME);
                    break;
            }
        });
    }

    /**
     * Set the disable effect of the buttons.
     * @param disabled if true the buttons will be disabled, otherwise they would be clickable.
     */
    private void setButtonsDisabled(boolean disabled) {
        startButt.setDisable(disabled);
        leaveButton.setDisable(disabled);
    }

    @Override
    public void startObserver() {
        setButtonsDisabled(false);
        System.out.println("GUIMPRoom.startObserver");
        playersListView.setItems(observablePlayersList);
        GUIChat.resetChat();
        GUIChat.bindChat(chatListView);
        room_name.setText(GameApplication.getInstance().getRoomName());
    }
}
