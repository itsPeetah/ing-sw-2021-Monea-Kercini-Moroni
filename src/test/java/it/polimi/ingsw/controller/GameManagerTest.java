package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.cards.CardManager;
import it.polimi.ingsw.model.cards.LeadCard;
import it.polimi.ingsw.model.game.Player;
import it.polimi.ingsw.model.general.ResourceType;
import it.polimi.ingsw.model.general.Resources;
import it.polimi.ingsw.model.playerboard.Warehouse;
import it.polimi.ingsw.network.server.metapackets.actions.Action;
import it.polimi.ingsw.network.server.metapackets.actions.data.Choose2LeadersActionData;
import it.polimi.ingsw.network.server.metapackets.actions.data.ChooseResourceActionData;
import it.polimi.ingsw.network.server.metapackets.actions.data.PutResourcesActionData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



class GameManagerTest {

    final int wait_time = 1;


    /**
     * Testing if setup works correctly in a 2 player game
     *
     * NOTE: To make testing easier the shuffle players method has been deactivated
     * This is important also for testing the order of the players that have entered the game is correct
     */
    @Test
    void setup(){

        GameManager gm = new GameManager();

        gm.addPlayer("Player 1");
        gm.addPlayer("Player 2");

        //Players have decided to start the game
        new Thread(() -> {
            gm.setupGame();
        }).start();


        //Preparing Leaders
        LeadCard myLeaders1[] = new LeadCard[2];
        LeadCard myLeaders2[] = new LeadCard[2];
        myLeaders1[0]  = CardManager.loadLeadCardsFromJson().get(0);
        myLeaders1[1]  = CardManager.loadLeadCardsFromJson().get(1);
        myLeaders2[0]  = CardManager.loadLeadCardsFromJson().get(2);
        myLeaders2[1]  = CardManager.loadLeadCardsFromJson().get(3);


        Choose2LeadersActionData TwoLeaders = new Choose2LeadersActionData();

        //Player 1 Leaders
        TwoLeaders.setLeaders(myLeaders1);
        TwoLeaders.setPlayer("Player 1");
        MockResponse MR1 = new MockResponse(Action.CHOOSE_2_LEADERS, TwoLeaders);
        MR1.startSendingResponse();

        //Player 2 Leaders
        TwoLeaders.setLeaders(myLeaders2);
        TwoLeaders.setPlayer("Player 2");
        MockResponse MR2 = new MockResponse(Action.CHOOSE_2_LEADERS, TwoLeaders);
        MR2.startSendingResponse();

        //Player 2 Chooses a resource
        Resources res = new Resources();
        res.add(ResourceType.STONES, 1);
        ChooseResourceActionData pickedRes = new ChooseResourceActionData();
        pickedRes.setRes(res);
        pickedRes.setPlayer("Player 2");
        MockResponse MR3 = new MockResponse(Action.CHOOSE_RESOURCE, pickedRes);
        MR3.startSendingResponse();

        //Player 2 Puts the resource in his warehouse
        Warehouse wh = new Warehouse();
        wh.deposit(res, 1);
        PutResourcesActionData putres = new PutResourcesActionData();
        putres.setWh(wh);
        putres.setPlayer("Player 2");
        MockResponse MR4 = new MockResponse(Action.PUT_RESOURCES, putres);
        MR4.startSendingResponse();

        //We wait a millisecond before turning off the player responses (the time is enough)

        try {
            TimeUnit.MILLISECONDS.sleep(wait_time);
        }catch (Exception e){
            e.printStackTrace();
        }
        MR1.stopSendingResponse();
        MR2.stopSendingResponse();
        MR3.stopSendingResponse();
        MR4.stopSendingResponse();

        //Testing if we have the player in game
        assertEquals("Player 1", gm.getGame().getPlayers()[0].getNickname() );

        //Testing if we added the correct card to that player
        assertEquals(myLeaders1[0].getCardId(), gm.getGame().getPlayers()[0].getLeaders().getPlayableCards().get(0).getCardId() );
        //Testing if we added the correct card to player 2
        assertEquals(myLeaders2[1].getCardId(), gm.getGame().getPlayers()[1].getLeaders().getPlayableCards().get(1).getCardId() );

        //Testing if we can find the resource in the players 2 warehouse
        assertTrue(res.equals(gm.getGame().getPlayers()[1].getBoard().getWarehouse().getResourcesAvailable()));

        //At this point the game starts
        assertEquals(GamePhase.TURN, gm.gamePhase);

        System.out.println("YAY");

    }

    @Test
    void checkWhite(){

        //Adding one player to the game
        GameManager gm = new GameManager();
        gm.addPlayer("Player 1");
        Player p = gm.getGame().getPlayers()[0];

        //Testing by adding a resource with no particularities
        Resources res = new Resources();
        res.add(ResourceType.STONES, 2);
        res.add(ResourceType.SHIELDS, 1);

        Resources newRes = gm.checkWhite(p, res);

        //Nothing should have changed
        assertTrue(res.equals(newRes));

        //Now let's try adding some white to our resource
        res.add(ResourceType.BLANK, 2);

        newRes = gm.checkWhite(p, res);
        //The white should be removed

        Resources res2 = new Resources();
        res2.add(ResourceType.STONES, 2);
        res2.add(ResourceType.SHIELDS, 1);

        //Check if it is equal to res2, the one without the blank
        assertTrue(res2.equals(newRes));

        //Now trying to add a leader with the white replacement ability in the players hand and then play it
        //lead_9 gives us servants instead of blank

        LeadCard lead_9 = CardManager.loadLeadCardsFromJson().get(8);
        LeadCard leaders[] = new LeadCard[2];
        leaders[0] = lead_9;
        //Set leader in players hand
        p.getLeaders().setCards(leaders);
        //Play leader
        try {
            p.getLeaders().playCard(0);
        }catch (Exception e){
            e.printStackTrace();
        }

        res2.add(ResourceType.BLANK, 2);

        Resources res3 = new Resources();
        res3.add(ResourceType.SHIELDS, 1);
        res3.add(ResourceType.STONES, 2);
        res3.add(ResourceType.SERVANTS, 2);

        //After the check res 2 should be equal to res 3

        newRes = gm.checkWhite(p, res2);

        assertTrue(res3.equals(newRes));


        //Now adding the other leader
        //This time player will need to make a choice
        //lead_12 gives us coins instead of blank

        LeadCard lead_12 = CardManager.loadLeadCardsFromJson().get(11);
        leaders[1] = lead_12;
        //Set leader in players hand
        p.getLeaders().setCards(leaders);
        //Play both leaders
        try {
            p.getLeaders().playCard(0);
            p.getLeaders().playCard(1);
        }catch (Exception e){
            e.printStackTrace();
        }

        Resources res4 = new Resources();
        res4.add(ResourceType.STONES, 2);
        res4.add(ResourceType.BLANK, 3);

        Resources res5 = new Resources();
        res5.add(ResourceType.COINS, 3);
        res5.add(ResourceType.STONES, 2);

        //After the check res 4 should be equal to res 5

        //We choose the second leader as our resource type (coin)
        Resources choice = new Resources();
        choice.add(ResourceType.COINS, 1);
        ChooseResourceActionData pickedRes = new ChooseResourceActionData();
        pickedRes.setRes(choice);
        pickedRes.setPlayer("Player 1");
        MockResponse MR1 = new MockResponse(Action.CHOOSE_RESOURCE, pickedRes);
        MR1.startSendingResponse();

        newRes = gm.checkWhite(p, res4);

        assertTrue(res5.equals(newRes));

        MR1.stopSendingResponse();


    }

}