package it.polimi.ingsw.network.server.metapackets.updates;

import it.polimi.ingsw.network.server.metapackets.messages.Message;

public class UpdatePacket {
    Update type;
    String data;

    /**
     * Constructor for GSON
     */
    public UpdatePacket() {}

    public UpdatePacket(Update type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Get the update type.
     */
    public Update getUpdate() {
        return type;
    }

    /**
     * Get json formatted string of data.
     */
    public String getData() {
        return data;
    }
}
