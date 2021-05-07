package it.polimi.ingsw.network.server.protocols;

import it.polimi.ingsw.network.common.ConnectionMessage;
import it.polimi.ingsw.network.server.components.RemoteUser;

public class ServerSideClientListener {

    private RemoteUser user;
    private String clientMessage;
    public ServerSideClientListener(RemoteUser user){
        this.user = user;
    }

    public void run() {

        while (true){

            clientMessage = user.receiveMessage();
            user.sendMessage("[SERVER] Did you say \"" + clientMessage + "\"?");

            if(ConnectionMessage.QUIT.check(clientMessage)){
                return;
            }

        }
    }
}
