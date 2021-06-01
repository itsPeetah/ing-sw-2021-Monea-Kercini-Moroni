package it.polimi.ingsw.network.server.components;

import it.polimi.ingsw.controller.model.ModelController;
import it.polimi.ingsw.controller.model.handlers.ModelControllerIOHandler;
import it.polimi.ingsw.controller.model.handlers.MPModelControllerIOHandler;
import it.polimi.ingsw.network.common.NetworkPacket;
import it.polimi.ingsw.network.common.NetworkPacketType;
import it.polimi.ingsw.network.common.social.SocialPacket;
import it.polimi.ingsw.network.common.sysmsg.GameLobbyMessage;
import it.polimi.ingsw.network.server.GameServer;
import it.polimi.ingsw.util.JSONUtility;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringJoiner;

/**
 * Framework class for a game room.
 */
public class GameRoom {

    private String roomId;              // room id
    private Object lock;                // concurrency-safe table lock
    private Hashtable<String, RemoteUser> users;
    private final ModelControllerIOHandler modelControllerIOHandler;
    private ModelController modelController;
    private Hashtable<String, String> miaPlayers;

    // TODO Add PINGING functionalities

    /**
     * Class constructor.
     */
    public GameRoom(String roomId) {
        this.roomId = roomId;

        this.lock = new Object();
        this.users = new Hashtable<String, RemoteUser>();
        this.modelControllerIOHandler = new MPModelControllerIOHandler(this);
        this.modelController = null;
        this.miaPlayers = new Hashtable<String, String>();
    }

    /**
     * Room id getter.
     */
    public String getId(){
        return roomId;
    }

    public boolean gameInProgress(){
        return modelController != null;
    }

    /**
     * Add a user to the room.
     * @param nickname User's local (room) nickname.
     * @param user User reference.
     * @throws GameRoomException if the nickname is already taken.
     */
    public void addUser(String nickname, RemoteUser user) throws GameRoomException{
        // TODO controllare che non ci sia una partita in corso (FAI ECCEZIONE RESILIENZA O RICONNESSIONE MAMMA TI CHIAMO TRA UN ATTIMO CIAO)
        synchronized (lock) {
            if(users.containsKey(nickname)) throw new GameRoomException("The nickname \"" + nickname +"\" is already taken in this room.");
            users.put(nickname, user);
            user.assignRoom(roomId, nickname);
        }
        StringJoiner stringJoiner = new StringJoiner(" ");
        for(String k: users.keySet())stringJoiner.add(k);
        String messageContent = GameLobbyMessage.PLAYERS_IN_ROOM.addBody(stringJoiner.toString());
        broadcast(new NetworkPacket(NetworkPacketType.SYSTEM, messageContent));
    }

    public void rejoinUser(String nickname, RemoteUser user){
        synchronized (lock){
            miaPlayers.remove(user.getId());
            users.put(nickname, user);
            // TODO Send catch up update
            System.out.println("User " + user.getId() + " rejoined room " + roomId + " as " + nickname + "!");
            // catchUp(user);
        }
    }

    // TODO if in game prevent from succeeding
    // TODO if room is empty after a user has left delete it.
    public boolean removeUser(String nickname){
        boolean result = false;
        synchronized (lock){
            if(users.containsKey(nickname)){
                RemoteUser removed = users.remove(nickname);
                result = true;

                // If the game is in progress
                if(gameInProgress()){
                    miaPlayers.put(removed.getId(), nickname);
                }
            }

            if(users.size() < 1) {
                System.out.println("Removing room '" + roomId +"' since it's been left empty.");
                GameServer.getInstance().getRoomTable().removeRoom(roomId);
            }
        }
        return result;
    }

    // TODO error handling?
    // TODO: if sending an update/message to the user check if they are connected first -> if not, push next turn action
    // TODO: define fallback answer for actions if disconnected -> pick leads, res and next turn.
    public void sendTo(String player, NetworkPacket packet){
        users.get(player).send(packet);
    }

    public void broadcast(NetworkPacket packet){
        for (String player: users.keySet()){
            users.get(player).send(packet);
        }
    }

    /**
     * Notify an action packet to the room's IOHandler
     * @param packet (ActionPacket) Network Packet to handle.
     */
    public void notify(NetworkPacket packet) {
        modelControllerIOHandler.notify(packet);
    }

    /**
     * Start the game
     * @return
     */
    public boolean startGame() {
        // Don't let the singleplayer game start online?
        /*if(users.size() < 2)
            broadcast(new NetworkPacket(NetworkPacketType.SYSTEM, ConnectionMessage.ERR.addBody("Can't start a multiplayer game by yourself!")));*/

         if(gameInProgress())
             return false;

        // Instantiate controller
        modelController = new ModelController(modelControllerIOHandler);
        // add players
        for(String player: users.keySet()) {
            modelController.addPlayer(player);
        }
        // randomize order
        modelController.getGame().shufflePlayers();
        // Send start
        String startMessage = GameLobbyMessage.START_ROOM.addBody("Game started");
        broadcast(new NetworkPacket(NetworkPacketType.SYSTEM, startMessage));
        // Start the game
        modelController.setupGame();

        return true;
    }

    public void handleSocialPacket(NetworkPacket networkPacket) {
        SocialPacket socialPacket = JSONUtility.fromJson(networkPacket.getPayload(), SocialPacket.class);
        switch(socialPacket.getType()) {
            case CHAT:
                System.out.println("Sending CHAT message");
                broadcast(networkPacket);
                break;
            case WHISPER:
                sendTo(socialPacket.getTo(), networkPacket);
                break;
        }
    }

    public String checkMIA(String userId){
        synchronized (lock){
            return miaPlayers.get(userId);
        }
    }
}