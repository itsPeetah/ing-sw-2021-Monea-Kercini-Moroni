package it.polimi.ingsw.controller.model.actions;

/**
 * Abstract class defining a EventData object. For each possible Action, a new an extension of EventData must be defined.
 */
public abstract class ActionData {
    protected String player;

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
