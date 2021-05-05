package it.polimi.ingsw.network.server.components;

import it.polimi.ingsw.network.common.ExSocket;

import java.io.IOException;

public class RemoteUser {

    private final String id;
    private ExSocket socket;

    private String roomId, nickname;

    /**
     * Class constructor.
     */
    public RemoteUser(String id, ExSocket socket){
        this.id = id;
        this.socket = socket;
        this.roomId = null;
    }

    public String getId() {return id;}
    public ExSocket getSocket() {return socket;}
    public boolean isInGame(){
        return roomId == null;
    }

    public void sendMessage(String message){
        socket.send(message);
    }

    public String receiveMessage(){
        return  socket.receive();
    }

    public String[] receiveMessageFields(){
        return receiveMessage().split("\\s+");
    }

    public void terminateConnection() {
        socket.close();

    }

    public void joinRoom(String roomId, String nickname){
        this.roomId = roomId;
        this.nickname = nickname;
    }
}
