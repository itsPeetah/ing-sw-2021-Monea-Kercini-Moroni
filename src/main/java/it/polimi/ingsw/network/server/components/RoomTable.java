package it.polimi.ingsw.network.server.components;

import it.polimi.ingsw.application.cli.util.ANSIColor;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.network.common.NetworkPacket;
import it.polimi.ingsw.network.common.SystemMessage;
import it.polimi.ingsw.network.server.GameServer;

import java.util.Hashtable;

/**
 * Stores all the rooms in a server.
 */
public class RoomTable {

    private static final int quickRoomRandomIdSequenceLength = 5;
    private static RoomTable instance;

    private Object lock;
    private Hashtable<String, GameRoom> rooms;

    /**
     * Class constructor
     */
    public RoomTable(){
        this.lock = new Object();
        this.rooms = new Hashtable<String, GameRoom>();
    }

    /**
     * Get an array with all of the server's room IDs.
     * @return
     */
    public String[] getRoomIDs(){
        String[] ids = new String[rooms.size()];
        int i = 0;
        for (String id : rooms.keySet()){
            ids[i] = id;
            i++;
        }
        return ids;
    }

    /**
     * Make the instance the singleton instance.
     * @return the instance
     */
    public RoomTable setAsInstance() {
        instance = this;
        return this;
    }

    /**
     * Get the singleton instance;
     */
    public static RoomTable getInstance(){
        if(instance == null){
            instance = new RoomTable();
        }
        return instance;
    }

    /**
     * Register a user room its id.
     * @return whether the operation was successful.
     */
    private void add(GameRoom room){
        synchronized (lock){
            rooms.put(room.getId(), room);
        }
    }

    /**
     * Check whether a room with such id already exists.
     */
    public boolean exists(String roomId){
        boolean result;
        synchronized (lock){
            result = rooms.containsKey(roomId);
        }
        return result;
    }

    /**
     * Get a user using their id.
     */
    public GameRoom getRoom(String id){
        GameRoom room = null;
        synchronized (lock){
            if(rooms.containsKey(id)) room = rooms.get(id);
        }
        return room;
    }

    /**
     * Create a new room.
     * @param roomId Name of the new room.
     * @param ownerNickname Nickname of the owner (creator) of the room.
     * @param owner Owner user reference.
     * @throws GameRoomException if there is already a room with the same name.
     */
    public void createRoom(String roomId, String ownerNickname, RemoteUser owner, int maxPlayers) throws GameRoomException{

        if(maxPlayers < 1 || maxPlayers > 4) throw new GameRoomException("A room can only host 1-4 players.");

        synchronized (lock){
            // Throw exception if the room already exists.
            if(roomId == null) roomId = "Room" + GameServer.getInstance().getUserTable().generateId(quickRoomRandomIdSequenceLength);
            if(rooms.containsKey(roomId)) throw new GameRoomException("A room with name \"" + roomId + "\" already exists. Please, try again.");
            // Create room and add owner
            GameRoom room = new GameRoom(roomId, maxPlayers);
            room.addUser(ownerNickname, owner);
            add(room);
        }
    }

    /**
     * Join an existing room.
     * @param roomId Name of the room to join.
     * @param nickname Nickname of the user.
     * @param user User reference.
     * @throws GameRoomException either if the room does not exist or if the name is already taken inside of that room.
     */
    public void joinRoom(String roomId, String nickname, RemoteUser user) throws GameRoomException{
        synchronized (lock){
            // Throw exception if the room doesn't exist.
            if(!rooms.containsKey(roomId)) throw new GameRoomException("There's no room with name \""+roomId+"\" on the server.");
            // Add user to the room
            GameRoom room = rooms.get(roomId);
            room.addUser(nickname, user);
        }
    }

    /**
     * Join a player to a random room
     */
    public void joinRandomRoom(String nickname, RemoteUser user) throws GameRoomException{
        synchronized (lock){

                boolean hasJoined = false;
            for(String roomID : rooms.keySet()){
                if(!rooms.get(roomID).isFull()) {
                    try {
                        joinRoom(roomID, nickname, user);
                        hasJoined = true;
                        break;
                    } catch (GameRoomException ex){
                        System.out.println(ANSIColor.YELLOW + "[QUICK START] User " + user.getId() + " tried to access room " + roomID + " as " + nickname + " but failed.");
                    }
                }
            }
            if(!hasJoined) createRoom(null, nickname, user, 4);
        }
    }

    /**
     * Remove a room from the server.
     */
    public void removeRoom(String roomId){
        synchronized (lock){
            GameRoom room = rooms.get(roomId);
            if(room != null){
                room.broadcast(NetworkPacket.buildSystemMessagePacket(SystemMessage.IN_LOBBY.getCode()));
                rooms.remove(roomId);
            }
        }
    }

    /**
     * Try finding a room for the user to rejoin
     * @param userId userID for the user trying to rejoin.
     * @return A String[] shaped like [roomID, in_gameUserNickname].
     * @throws GameRoomException if no rooms are waiting for this particular user.
     */
    public String[] findRoomToRejoin(String userId) throws GameRoomException{
        synchronized (lock){
            String nickname;
            for(String roomID : rooms.keySet()){
                nickname = rooms.get(roomID).checkMIA(userId);
                if(nickname != null){
                    return new String[]{roomID, nickname};
                }
            }
            throw new GameRoomException("There is no room user expecting the given id.");
        }
    }

}
