package it.polimi.ingsw.model.playerboard;

import it.polimi.ingsw.model.cards.DevCard;
import it.polimi.ingsw.model.cards.LeadCard;
import it.polimi.ingsw.model.general.Production;

import java.util.ArrayList;

public class ProductionPowers {

    private Production basicProduction;
    private DevCard[][] cardPile;  //The first symbolizes the pile and each can have 3 cards maximum
    private Production[] LeadProduction;

    /**
     * Searches all card piles and returns the card on top of each + Production from LeadCards
     * @return array of production of all the top development cards on all piles
     */
    public ArrayList<Production> getAvailableProductions(){

        ArrayList<Production> AvailableProductions = new ArrayList<Production>();

        for (int i = 0; i < cardPile.length; i++) {
            //If the card exists I add the production of the top card
            //Here might need to revise if we want to add cards of higher level
            if (cardPile[i][2] != null){
                AvailableProductions.add(cardPile[i][2].getProduction());
            }else if (cardPile[i][1] != null){
                AvailableProductions.add(cardPile[i][1].getProduction());
            }else if (cardPile[i][0] != null) {
                AvailableProductions.add(cardPile[i][0].getProduction());
            }
        }

        AvailableProductions.add(basicProduction);

        for (int i = 0; i < LeadProduction.length; i++) {
            AvailableProductions.add(LeadProduction[i]);
        }

        return AvailableProductions;
    }

    /**
     *
     * @return all the Development Cards The player has purchased
     */

    public ArrayList<DevCard> getOwnedDevCards() {

        ArrayList<DevCard> AvailableCards = new ArrayList<DevCard>();

        for (int i = 0; i < cardPile.length; i++) {
            for (int j = 0; j < 3; j++) {
                if (cardPile[i][j] != null) {
                    AvailableCards.add(cardPile[i][j]);
                }
            }
        }
        return AvailableCards;
    }

    /**
     * adds development card on top of the selected pile
     * @param devCard to add
     * @param position of the pile
     */

    public void addDevCard(DevCard devCard, int position){

        cardPile[position][devCard.getLevel()-1] = devCard;
    }

    /**
     * Adds the extra production from LeadCard
     * @param leadCard the LeaderCard that gives the extra production power
     */

    public void addLeadCardProduction(LeadCard leadCard){

        LeadProduction[LeadProduction.length] = leadCard.getProduction();

    }


    /**
     *
     * @param piles in the default rules should be 3
     */

    public ProductionPowers(int piles) {
        this.basicProduction = new Production(); //QUA DA AGGIORNARE !!!
        this.cardPile = new DevCard[piles][3];
        this.LeadProduction = new Production[2];
    }

    /**
     *
     * @return sum of victory points all development cards you have give you
     */

    public int getOwnedCardsVictoryPoints(){
        int vp = 0;
        for (int i = 0; i < cardPile.length; i++){
            for (int j = 0; j < cardPile[i].length; j++){
                if (cardPile[i][j] != null){
                    vp += cardPile[i][j].getVictoryPoints();
                }
            }
        }
        return vp;
    }
}
