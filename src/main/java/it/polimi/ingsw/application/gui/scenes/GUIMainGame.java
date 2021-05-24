package it.polimi.ingsw.application.gui.scenes;

import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.common.listeners.PacketListener;
import it.polimi.ingsw.application.gui.Materials;
import it.polimi.ingsw.controller.model.messages.Message;
import it.polimi.ingsw.view.data.GameData;
import it.polimi.ingsw.view.observer.CommonDataObserver;
import it.polimi.ingsw.view.observer.PlayerDataObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Sphere;

import java.net.URL;
import java.util.ResourceBundle;

import static it.polimi.ingsw.application.gui.Materials.getMaterial;
import static it.polimi.ingsw.model.cards.CardManager.getImage;

public class GUIMainGame implements Initializable, CommonDataObserver, PacketListener, PlayerDataObserver {

    public ImageView dev01;
    public ImageView dev02;
    public ImageView dev12;
    public ImageView dev11;
    public ImageView dev32;
    public ImageView dev22;
    public ImageView dev21;
    public ImageView dev31;
    public ImageView dev00;
    public ImageView dev10;
    public ImageView dev20;
    public ImageView dev30;

    public ImageView lead1;
    public ImageView lead2;

    private ImageView[][] devCards = new ImageView[4][3];

    private Sphere[][] marbles = new Sphere[3][4];

    @FXML
    //The marble waiting
    private Sphere marble = new Sphere(29);
    @FXML
    //the other marbles
    private Sphere marble00 = new Sphere(29);
    @FXML
    private Sphere marble01 = new Sphere(29);
    @FXML
    private Sphere marble02 = new Sphere(29);
    @FXML
    private Sphere marble03 = new Sphere(29);
    @FXML
    private Sphere marble10 = new Sphere(29);
    @FXML
    private Sphere marble11 = new Sphere(29);
    @FXML
    private Sphere marble12 = new Sphere(29);
    @FXML
    private Sphere marble13 = new Sphere(29);
    @FXML
    private Sphere marble20 = new Sphere(29);
    @FXML
    private Sphere marble21 = new Sphere(29);
    @FXML
    private Sphere marble22 = new Sphere(29);
    @FXML
    private Sphere marble23 = new Sphere(29);

    //generating materials needed for the marble spheres
    Materials materials = new Materials();

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public void onSystemMessage(String message) {

    }

    @Override
    public void onDevCardMarketChange() {
        Platform.runLater(() -> {

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    devCards[i][j].setImage(getImage(GameApplication.getInstance().getGameController().getGameData().getCommon().getDevCardMarket().getAvailableCards()[i][j].getCardId()));
                }
            }
        });

    }

    @Override
    public void onMarketTrayChange() {

        Platform.runLater(() -> {

            marble.setMaterial(getMaterial(GameApplication.getInstance().getGameController().getGameData().getCommon().getMarketTray().getWaiting()[0].getMarbleColor()));

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    marbles[j][i].setMaterial(getMaterial(GameApplication.getInstance().getGameController().getGameData().getCommon().getMarketTray().getAvailable()[j][i].getMarbleColor()));
                }
            }
        });

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setListeners();

        //Connecting all marbles to matrix for simplicity
        marbles[0][0] = marble00;
        marbles[0][1] = marble01;
        marbles[0][2] = marble02;
        marbles[0][3] = marble03;
        marbles[1][0] = marble10;
        marbles[1][1] = marble11;
        marbles[1][2] = marble12;
        marbles[1][3] = marble13;
        marbles[2][0] = marble20;
        marbles[2][1] = marble21;
        marbles[2][2] = marble22;
        marbles[2][3] = marble23;

        //Connecting all dev cards to matrix

        devCards[0][0] = dev00;
        devCards[0][1] = dev01;
        devCards[0][2] = dev02;
        devCards[1][0] = dev10;
        devCards[1][1] = dev11;
        devCards[1][2] = dev12;
        devCards[2][0] = dev20;
        devCards[2][1] = dev21;
        devCards[2][2] = dev22;
        devCards[3][0] = dev30;
        devCards[3][1] = dev31;
        devCards[3][2] = dev32;

    }

    private void setListeners() {
        GameData gameData = GameApplication.getInstance().getGameController().getGameData();
        gameData.getCommon().getMarketTray().setObserver(this);
        gameData.getCommon().getDevCardMarket().setObserver(this);
        gameData.getPlayerData(GameApplication.getInstance().getUserNickname()).getPlayerLeaders().setObserver(this);
    }

    @Override
    public void onFaithChange() {

    }

    @Override
    public void onReportsAttendedChange() {

    }

    @Override
    public void onLeadersChange() {
        lead1.setImage(getImage(GameApplication.getInstance().getGameController().getGameData().getPlayerData(GameApplication.getInstance().getUserNickname()).getPlayerLeaders().getLeaders()[0].getCardId()));
        lead2.setImage(getImage(GameApplication.getInstance().getGameController().getGameData().getPlayerData(GameApplication.getInstance().getUserNickname()).getPlayerLeaders().getLeaders()[1].getCardId()));
    }

    @Override
    public void onLeadersStatesChange() {

    }

    @Override
    public void onStrongboxChange() {

    }

    @Override
    public void onWarehouseContentChange() {

    }

    @Override
    public void onWarehouseExtraChange() {

    }
}