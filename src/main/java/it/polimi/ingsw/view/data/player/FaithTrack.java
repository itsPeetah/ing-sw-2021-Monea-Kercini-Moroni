package it.polimi.ingsw.view.data.player;

import it.polimi.ingsw.view.observer.player.FaithTrackObserver;

public class FaithTrack {
    /* OBSERVER */
    FaithTrackObserver faithTrackObserver;

    private int faith;
    private boolean[] reportsAttended;

    public synchronized void setFaith(int faith) {
        this.faith = faith;
        if(faithTrackObserver != null) faithTrackObserver.onFaithChange();
    }

    public synchronized void setReportsAttended(boolean[] reportsAttended) {
        this.reportsAttended = reportsAttended;
        if(faithTrackObserver != null) faithTrackObserver.onReportsAttendedChange();
    }

    public FaithTrack() {
        this.faith = 0;
        this.reportsAttended = new boolean[3];
    }

    public void setObserver(FaithTrackObserver faithTrackObserver) {
        this.faithTrackObserver = faithTrackObserver;
        faithTrackObserver.onFaithChange();
        faithTrackObserver.onReportsAttendedChange();
    }
}
