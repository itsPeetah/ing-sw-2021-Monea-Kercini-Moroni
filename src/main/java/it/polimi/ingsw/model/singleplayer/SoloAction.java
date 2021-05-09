package it.polimi.ingsw.model.singleplayer;

import it.polimi.ingsw.model.game.DevCardMarket;
import it.polimi.ingsw.model.general.Color;

import java.util.ArrayList;
import java.util.Collections;

public class SoloAction {

    private ArrayList<SoloActionTokens> soloActionTokens = new ArrayList<SoloActionTokens>();
    private int difficulty;
    private BlackCross cross;

    /**
     * Prepares Lorenzo solo action tokens for single player
     * difficulty - in case we want to make different difficulty levels
     */
    private ArrayList<SoloActionTokens> createSoloActionDeck(){
        switch (difficulty){
            default:
                soloActionTokens.add(SoloActionTokens.DISCARD_2_BLUE);
                soloActionTokens.add(SoloActionTokens.DISCARD_2_GREEN);
                soloActionTokens.add(SoloActionTokens.DISCARD_2_YELLOW);
                soloActionTokens.add(SoloActionTokens.DISCARD_2_PURPLE);
                soloActionTokens.add(SoloActionTokens.MOVE_2);
                soloActionTokens.add(SoloActionTokens.MOVE_1_SHUFFLE);
                // I think one of the tokens was 2 times?
                //todo confirm the number of tokens
        }
        //Shuffle tokens
        Collections.shuffle(soloActionTokens);

        return soloActionTokens;
    }

    public void setSoloGameDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Simulates Lorenzos turn in a single player turn
     * @param dcm the DevCardMarket Lorenzo applies his actions
     * @return true if Lorenzo has won the turn
     */
    public boolean playLorenzoTurn(DevCardMarket dcm){

        boolean win = false;

        switch (soloActionTokens.get(0)){

            case DISCARD_2_BLUE:
                win = dcm.discard2(Color.BLUE);
                break;
            case DISCARD_2_GREEN:
                win = dcm.discard2(Color.GREEN);
                break;
            case DISCARD_2_PURPLE:
                win = dcm.discard2(Color.PURPLE);
                break;
            case DISCARD_2_YELLOW:
                win = dcm.discard2(Color.YELLOW);
                break;
            case MOVE_2:
                win = cross.incrementBlackFaith(2);
                break;
            case MOVE_1_SHUFFLE:
                win = cross.incrementBlackFaith(1);
                //Recreate shuffled deck
                soloActionTokens = createSoloActionDeck();
                break;
        }
        return win;
    }

}
