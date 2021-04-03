package it.polimi.ingsw.model.playerleaders;

import it.polimi.ingsw.model.cards.LeadCard;

import java.util.Arrays;
import java.util.ArrayList;

public class PlayerLeaders {

    private LeadCard[] cards;
    private CardState[] cardStates;

    /**
     *
     * @return All the cards the player has in hand.
     */

    public ArrayList<LeadCard> getPlayableCards(){

        ArrayList<LeadCard> playableCards = new ArrayList<LeadCard>();

        for(int i = 0; i < cards.length; i++){
            if (cardStates[i] == CardState.INHAND){
                playableCards.add(cards[i]);
            }
        }
        return playableCards;
    }

    /**
     * Sets the state of the card at index position as PLAYED
     * @param index is the position of the card that the player plays
     */

    public void playCard(int index){
        cardStates[index] = CardState.PLAYED;
    }


    /**
     * Gives the player the leader cards he has in his hand and sets the cardstates INHAND
     * @param cards the leader cards
     */

    public void setCards(LeadCard[] cards) {
        this.cards = cards;
        Arrays.fill(this.cardStates, CardState.INHAND);
    }

    /**
     *
     * @return the total victory points from all played leaders
     */

    public int getPlayedCardVictoryPoints(){

        int vp = 0;

        for(int i = 0; i < cards.length; i++){
            if (cardStates[i] == CardState.PLAYED){
                vp += cards[i].getVictoryPoints();
            }
        }
        return vp;
    }

}
