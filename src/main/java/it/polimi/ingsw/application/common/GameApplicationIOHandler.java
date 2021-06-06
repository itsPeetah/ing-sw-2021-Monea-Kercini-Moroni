package it.polimi.ingsw.application.common;

import it.polimi.ingsw.application.cli.util.ANSIColor;
import it.polimi.ingsw.application.gui.GUIScene;
import it.polimi.ingsw.controller.model.actions.ActionPacket;
import it.polimi.ingsw.controller.model.messages.MessagePacket;
import it.polimi.ingsw.controller.model.updates.UpdatePacket;
import it.polimi.ingsw.network.client.persistence.ReconnectionInfo;
import it.polimi.ingsw.network.common.NetworkPacket;
import it.polimi.ingsw.network.common.social.SocialPacket;
import it.polimi.ingsw.network.common.SystemMessage;
import it.polimi.ingsw.util.JSONUtility;

public class GameApplicationIOHandler {

    // TODO Add Thread Pool for elaborating commands

    private static GameApplicationIOHandler instance;
    public static GameApplicationIOHandler getInstance(){
        if(instance == null) instance = new GameApplicationIOHandler();
        return instance;
    }

    public GameApplicationIOHandler() {
    }

    public void notifyMessage(NetworkPacket messageNetworkPacket) {
        MessagePacket messagePacket = JSONUtility.fromJson(messageNetworkPacket.getPayload(), MessagePacket.class);
        GameApplication.getInstance().getGameControllerIO().notifyMessage(messagePacket);
    }

    public void notifyUpdate(NetworkPacket updateNetworkPacket) {
        UpdatePacket updatePacket = JSONUtility.fromJson(updateNetworkPacket.getPayload(), UpdatePacket.class);
        GameApplication.getInstance().getGameControllerIO().notifyUpdate(updatePacket);
    }

    public void pushAction(ActionPacket actionPacket) {
        NetworkPacket networkPacket = NetworkPacket.buildActionPacket(actionPacket);
        GameApplication.getInstance().sendNetworkPacket(networkPacket);
    }

    public void pushChatMessage(String content) {
        GameApplication gameApplication = GameApplication.getInstance();
        NetworkPacket networkPacket = NetworkPacket.buildChatPacket(content, gameApplication.getUserNickname());
        gameApplication.sendNetworkPacket(networkPacket);
    }

    public int handleSystemMessage(NetworkPacket systemMessageNetworkPacket){
        String serverMessage = systemMessageNetworkPacket.getPayload();
        String[] messageFields = serverMessage.split(" ", 2);
        String[] messageArgs = messageFields.length > 1 ? messageFields[1].split(" ") : null;

        int exitCode = 0;
        // TODO Perhaps change with a switch(SystemMessage.valueOf(messageFields[0]) ?
        if (SystemMessage.QUIT.check(messageFields[0])) {
            handleQuitMessage();
            exitCode = -1;
        }
        else if(SystemMessage.IN_LOBBY.check(messageFields[0])) {
            GameApplication.getInstance().setApplicationState(GameApplicationState.LOBBY);
        }
        else if(SystemMessage.PING.check(messageFields[0])) {
            GameApplication.getInstance().sendNetworkPacket(NetworkPacket.buildSystemMessagePacket(SystemMessage.PING.getCode()));
        }
        else if(SystemMessage.START_ROOM.check(messageFields[0])) {
            ReconnectionInfo.saveID(GameApplication.getInstance().getUserId()); // TODO Add GAME_OVER => resetID
            GameApplication.getInstance().startMPGame();
            // Save id in case the connection is interrupted...
        }
        else if(SystemMessage.PLAYERS_IN_ROOM.check(messageFields[0])) {
            System.out.println(messageFields[1]);
            GameApplication.getInstance().setRoomPlayers(messageFields[1]);
        }
        else if(SystemMessage.IN_ROOM.check(messageFields[0])){
            GameApplication.getInstance().setRoomName(messageArgs[0]);
            GameApplication.getInstance().setUserNickname(messageArgs[1]);
            GameApplication.getInstance().setApplicationState(GameApplicationState.PREGAME);
        } else if (SystemMessage.CANT_JOIN.check(messageFields[0])){
            if(messageArgs != null) GameApplication.getInstance().out(messageFields[1]);
            GameApplication.getInstance().setApplicationState(GameApplicationState.LOBBY);
        } else if (SystemMessage.IN_GAME.check(messageFields[0])){
            if(!GameApplication.getInstance().gameExists()) GameApplication.getInstance().startMPGame();
            GameApplication.getInstance().setApplicationState((GameApplicationState.INGAME));
        } else if (SystemMessage.ERR.check(messageFields[0])) {
            System.out.println(ANSIColor.RED+ "[ERR] " + messageFields[1] + ANSIColor.RESET);
        }
        if(GameApplication.getOutputMode() == GameApplicationMode.GUI && GUIScene.getActiveScene() != null) GUIScene.getActiveScene().onSystemMessage(null);
        return exitCode;
    }

    public void handleSocialMessage(NetworkPacket networkPacket) {
        SocialPacket socialPacket = JSONUtility.fromJson(networkPacket.getPayload(), SocialPacket.class);
        GameApplication gameApplication = GameApplication.getInstance();
        switch(socialPacket.getType()) {
            case CHAT:
                gameApplication.outChat(socialPacket.getFrom(), socialPacket.getBody());
                break;
            case WHISPER:
                gameApplication.outWhisper(socialPacket.getFrom(), socialPacket.getBody());
                break;
        }
    }

    public void handleDebugMessage(NetworkPacket debugMessageNetworkPacket){
        GameApplication.getInstance().out(debugMessageNetworkPacket.getPayload());
    }

    /*private void handleSystemOK(String args){

        String[] arguments = args.split(" ");

        GameApplicationState state = GameApplication.getInstance().getApplicationState();
        switch (state){
            case CONNECTING_TO_ROOM:
                GameApplication.getInstance().setRoomName(arguments[0]);
                GameApplication.getInstance().setUserNickname(arguments[1]);
                GameApplication.getInstance().setApplicationState(GameApplicationState.PREGAME);
                break;
        }

    }

    private void handleSystemERR(String args){
        GameApplicationState state = GameApplication.getInstance().getApplicationState();
        switch (state){
            case CONNECTING_TO_ROOM:
                if(args != null) GameApplication.getInstance().out(args);
                GameApplication.getInstance().setApplicationState(GameApplicationState.LOBBY);
                break;
        }
    }*/

    private void handleQuitMessage() {
        System.out.println("Received quit instruction");
        GameApplication.getInstance().closeConnectionWithServer();
        GameApplication.getInstance().setApplicationState(GameApplicationState.STARTED);
    }

}
