package it.polimi.ingsw.controller.model;

import it.polimi.ingsw.controller.model.handlers.ModelControllerIOHandler;
import it.polimi.ingsw.controller.model.messages.Message;
import it.polimi.ingsw.controller.model.updates.Update;
import it.polimi.ingsw.controller.model.updates.data.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.controller.model.actions.*;
import it.polimi.ingsw.controller.model.actions.data.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.game.util.GameFactory;
import it.polimi.ingsw.model.general.*;
import it.polimi.ingsw.model.playerboard.*;
import it.polimi.ingsw.model.singleplayer.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class ModelController {

    public GamePhase gamePhase;
    private final Game game;
    private final ModelControllerIOHandler modelControllerIOHandler;
    private boolean singlePlayer = false;

    private SoloAction Lorenzo;
    public void setSinglePlayer(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
    }
    public boolean isSinglePlayer() {
        return singlePlayer;
    }
    public SoloAction getLorenzo() {
        return Lorenzo;
    }

    public ModelController(ModelControllerIOHandler modelControllerIOHandler) {
        // Initialize game (with default settings)
        game = GameFactory.CreateGame();
        gamePhase = GamePhase.PREGAME;
        
        // Set communication handler
        this.modelControllerIOHandler = modelControllerIOHandler;
    }

    public Game getGame() {
        return game;
    }


    public void addPlayer(String nickname){
        try {
            game.addPlayer(nickname);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Method for letting player choose a resource
     * @return the resource
     */
    private Resources askPlayerToChooseResource(Player p){

        modelControllerIOHandler.setExpectedAction(Action.CHOOSE_RESOURCE, p.getNickname());
        ChooseResourceActionData data = modelControllerIOHandler.getResponseData();
        Resources res = data.getResources();

        return res;
    }


    /**
     * Player has to put the resources in the correct Warehouse place
     * @param res resources player has to put
     * @param wh warehouse that will be updated
     */
    private Warehouse askPlayerToPutResources(Player p, Resources res, Warehouse wh){

        modelControllerIOHandler.setExpectedAction(Action.PUT_RESOURCES, p.getNickname());
        PutResourcesActionData data = modelControllerIOHandler.getResponseData();
        Warehouse updatedWarehouse = data.getWarehouse();

        //Checking if player hasn't hacked the game
        if( (wh.getResourcesAvailable().add(res)).isGreaterThan(updatedWarehouse.getResourcesAvailable()) ){

            //Checking if the warehouse organization is correct
            if (!updatedWarehouse.isOrganized()){
                //notify player of his mistake
                modelControllerIOHandler.sendMessage(p.getNickname(), Message.WAREHOUSE_UNORGANIZED);
                return wh;
            }

            //If player has less resources than he should have give other players extra faith point

            int extraFP = (wh.getResourceAmountWarehouse()+ res.getTotalAmount()) - updatedWarehouse.getResourceAmountWarehouse();

            for (int i = 0; i< game.getPlayers().length; i++){
                //add to all players except the one who is playing
                if(game.getPlayers()[i] != p){
                    game.getPlayers()[i].getBoard().incrementFaithPoints(extraFP);
                }
            }

            //update
            updateWarehouse(p);

            //send ok to the view controller
            modelControllerIOHandler.sendMessage(p.getNickname(), Message.OK);
            return updatedWarehouse;

        }else{
            //Player has hacked game !!!!!!!!!!!!!!!!!
            //TODO punish player for trying to cheat
            return wh;
        }
    }


    /**
     * Setting up new game after all the players have joined
     */
    public void setupGame(){

        gamePhase = GamePhase.START;

        //If there is only one player set the game as single player
        if(game.getPlayers().length==1){
            setSinglePlayer(true);
        }

        //Initialize leader cards
        ArrayList<LeadCard> leadCards = CardManager.loadLeadCardsFromJson();

        //shuffle leadCards
        Collections.shuffle(leadCards);

        //shuffle player order
        //game.shufflePlayers();


        //Updating the view with the current Market Tray and DevCard market
        //This might influence player choice on the leader and extra resources
        updateResourceMarket();
        updateDevCardMarket();

        //Getting player Leader choices and Extra resources depending on player order

        for (int i = 0; i< game.getPlayers().length; i++){

            /*
            //Set up player board
            wh.add(new Warehouse());
            sb.add(new Strongbox());
            pp.add(new ProductionPowers(3));
            pb.add(new PlayerBoard(3, wh.get(i), sb.get(i), pp.get(i)));

            game.getPlayers()[i].setBoard(pb.get(i)); //Assign Board to Player
             */

            //The player has been offered 4 leader cards on the model side and is choosing 2
            modelControllerIOHandler.setExpectedAction(Action.CHOOSE_2_LEADERS, game.getPlayers()[i].getNickname());
            Choose2LeadersActionData data = modelControllerIOHandler.getResponseData();
            game.getPlayers()[i].getLeaders().setCards(data.getLeaders());


            if (i>=1){ //second player gets an extra resource

                Resources extra;
                extra = askPlayerToChooseResource(game.getPlayers()[i]);
                game.getPlayers()[i].getBoard().getWarehouse().copy(askPlayerToPutResources(game.getPlayers()[i], extra, game.getPlayers()[i].getBoard().getWarehouse()));
            }
            if (i>=2){ //third player gets an extra faith in addition to the resource
                game.getPlayers()[i].getBoard().incrementFaithPoints(1);
            }
            if (i>=3){//fourth player gets all of the above and again an extra resource

                Resources extra2;
                extra2 = askPlayerToChooseResource(game.getPlayers()[i]);
                game.getPlayers()[i].getBoard().getWarehouse().copy(askPlayerToPutResources(game.getPlayers()[i], extra2, game.getPlayers()[i].getBoard().getWarehouse()));

            }

            //After each players choice update the view
            updateWarehouse(game.getPlayers()[i]);
            updateFaithPoints();
        }

        //If single player game instantiate Lorenzo, the opponent
        if(singlePlayer){
            Lorenzo = new SoloAction(0); //For now the difficulty doesn't matter as there is only one
        }

        startGame();
    }


    /**
     * The game starts
     * The game will now stay on turn phase until last round it's triggered
     * Then the players left will play their last turn (until it's the last players turn)
     * This is where the it will be kept track of the turn and will be triggered the end game
     */

    private void startGame(){

        //System.out.println("Game has started!");

        gamePhase = GamePhase.TURN;

        boolean lastRound = false;

        //Turns will keep being played until it's the last round and it's the last players turn
        while (!lastRound || !(game.getCurrentPlayer() == game.getPlayers()[game.getPlayers().length-1])){

            playTurn(game.getCurrentPlayer());

            //These are the only two ways the game ends for now

            //if player has 7 devCards
            if(game.getCurrentPlayer().getBoard().getOwnedDevCards().size() == 7){
                lastRound = true;
            }

            //if player has reached the end of the faith track
            if(game.getCurrentPlayer().getBoard().getFaithPoints() >= 24){
                lastRound = true;
            }

            //In a single player game, the game might end if Lorenzo wins
            if(singlePlayer) {

                //Lorenzo plays his turn
                if (Lorenzo.playLorenzoTurn(game.getDevCardMarket())) {
                    //Lorenzo has won the game
                    modelControllerIOHandler.sendMessage(game.getCurrentPlayer().getNickname(), Message.LOSER);
                }
                //Sending action token to view
                updateActionToken();
            }

            game.increaseTurnCounter();
        }
        endGame();
    }


    /**
     * This method represents a players turn
     * @param player the player
     */

    private void playTurn(Player player){

        boolean primaryActionUsed = false;
        boolean turnFinished = false;

        modelControllerIOHandler.sendMessage(player.getNickname(), Message.START_TURN);

        //Player may keep doing as many actions as he wants as long as he doesn't end his turn
        do {
            modelControllerIOHandler.setExpectedAction(Action.RESOURCE_MARKET, player.getNickname());
            modelControllerIOHandler.addExpectedAction(Action.DEV_CARD);
            modelControllerIOHandler.addExpectedAction(Action.PRODUCE);
            modelControllerIOHandler.addExpectedAction(Action.PlAY_LEADER);
            modelControllerIOHandler.addExpectedAction(Action.DISCARD_LEADER);
            modelControllerIOHandler.addExpectedAction(Action.REARRANGE_WAREHOUSE);
            modelControllerIOHandler.addExpectedAction(Action.END_TURN);

            switch (modelControllerIOHandler.getResponseAction()) {

                //Player has chosen to acquire resources from the Market tray
                case RESOURCE_MARKET:
                    primaryActionUsed = resourceMarket(player, primaryActionUsed);
                    break;

                //Player has chosen to buy a development card
                case DEV_CARD:
                    primaryActionUsed = devCardMarket(player, primaryActionUsed);
                    break;

                //Player has chosen to produce
                case PRODUCE:
                    primaryActionUsed = produce(player, primaryActionUsed);
                    break;

                //Player has chosen to play/discard leader
                //these are not primary actions and can be used more than once during his turn, whenever player wants
                case PlAY_LEADER:
                    playLeader(player);
                    break;

                case DISCARD_LEADER:
                    discardLeader(player);
                    break;

                //Player has chosen rearrange the resources he has in his warehouse
                //(this choice is practically useless since player can arrange his warehouse anytime he acquires
                // resources from Market Tray)
                case REARRANGE_WAREHOUSE:
                    //Basically we ask the player to put all resources that he has in warehouse in his warehouse
                    player.getBoard().getWarehouse().copy(askPlayerToPutResources(player, player.getBoard().getWarehouse().getResourcesAvailable(), player.getBoard().getWarehouse()));
                    break;

                case END_TURN:
                    //Nothing - player just ends his turn
                    turnFinished = true;
                    break;
            }

            //After each action check if the player has triggered a vatican report event
            game.checkVaticanReport();

        }while(!turnFinished);

    }


    /**
     * Calculate victory points for each player and show victory screens
     */

    public void endGame(){

        gamePhase = GamePhase.END;

        //Calculate VP for each player

        //An integer array for storing player VP so it can be more easily accessed
        int[] VP = new int[game.getPlayers().length];

        for (int i = 0; i< game.getPlayers().length; i++) {

            VP[i] = game.getPlayers()[i].getVictoryPoints();
        }

        //winner symbolizes the position of the player that has won, not the player or the points!
        int winner = getWinner(VP);

        //Sending to players their corresponding victory points
        updateVP(VP);

        modelControllerIOHandler.sendMessage(game.getPlayers()[winner].getNickname(), Message.WINNER);
        //Maybe add post-game functionality
    }


    /**
     * get the position of the winning player (the one with the highest number of points)
     * in case two players have the same number of points, the one with more resources win,
     * if these are also equal the game rules give no indication in who the winner is
     * @param array of victory points
     * @return position of the winning player
     */

    private int getWinner( int[] array ) {
        int winner = 0;
        for ( int i = 1; i < array.length; i++ )
        {
            if ( array[i] > array[winner] ){
                winner = i;
            }
            //check if players have equal VP and and one has more resources
            if (array[i] == array[winner] && ( game.getPlayers()[i].getBoard().getResourcesAvailable().getTotalAmount() > game.getPlayers()[winner].getBoard().getResourcesAvailable().getTotalAmount()) ){
                winner = i;
            }
        }
        return winner; // position of the one wih most VP found
    }


    /**
     * Updates Market after players choice as well as his Warehouse
     * @param player
     * @param row =true if player has chosen to acquire from row =false if he acquires a column
     * @param index the row/column he has chosen
     * @return true if it executed the action with no problems
     */
    protected boolean resourceMarketUpdate(Player player, boolean row, int index) {

        Resources res = new Resources();

        if (row) {
            try {
                res.add(game.getResourceMarket().pickRow(index));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                res.add(game.getResourceMarket().pickColumn(index));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Check if there is a red marble (gives faith) and remove it
        res = faithCheck(player, res);

        //Check if the player has any Lead Ability that transforms his white marbles
        //Count how many blank replacements we have (in the majority of the cases it will be 0 and almost never 2)
        res = checkWhite(player, res);

        //Ask player to put the gotten resources in his warehouse.
        player.getBoard().getWarehouse().copy(askPlayerToPutResources (player, res, player.getBoard().getWarehouse() ));

        //Send update of all stuff that has been updated
        updateResourceMarket();
        //updateWarehouse(player); Warehouse is already updated when player was asked to put resources
        updateFaithPoints();

        return true;
    }


    /**
     * Updates The development card market and player board
     * @param player
     * @param chosenCard The dev card the player has chosen
     * @param position The position (pile) the player wants to put the card he bought
     * @return true if it executed the action with no problems
     */
    protected boolean devCardMarketUpdate(Player player, DevCard chosenCard, int position){

        //check if affordable
        if(!chosenCard.affordable(player)){
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.NOT_ENOUGH_RESOURCES);
            return false;

            //check if it's possible to place that card there
        }else if (!player.getBoard().getProductionPowers().canDevCardBePlaced(chosenCard, position)){

            modelControllerIOHandler.sendMessage(player.getNickname(), Message.ILLEGAL_CARD_PLACE);
            return false;

        }else{
            //Removes the cost from players warehouse
            player.getBoard().getWarehouse().withdraw(chosenCard.getCost());
            //Adds card in players board
            player.getBoard().getProductionPowers().addDevCard(chosenCard, position);
            //Remove Card from DevCardMarket
            try {
                game.getDevCardMarket().buyCard(chosenCard);
            }catch (Exception e){
                e.printStackTrace();
            }

            //Update
            updateDevCardMarket();
            updateProductionPowers(player);
            updateWarehouse(player);

            modelControllerIOHandler.sendMessage(player.getNickname(), Message.OK);
            return true;
        }
    }


    /**
     * Updates the warehouse and strongbox with players choice
     * @param player
     * @param chosenProduction by the player
     * @return true if it executed the action with no problems
     */
    protected boolean produceUpdate(Player player, ArrayList<Production> chosenProduction){

        //Check if all productions can be activated at the beginning, before any actual production has taken place

        //First ask the player to choose all input choices

        Resources input = new Resources();

        //Calculate total costs
        Resources tot_cost = new Resources();
        for(Production production : chosenProduction) {

            input = makePlayerChoose(player, production.getInput()); //If player has choice in input he has to choose here

            tot_cost.add(input);

        }

        //If it is not enough
        if (!player.getBoard().getResourcesAvailable().isGreaterThan(tot_cost)) {
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.NOT_ENOUGH_RESOURCES);
            return false;

        }else{

            //Paying cost
            Resources fromStrongbox = new Resources(); // The resources that should be withdrawn from strongbox after the first withdrawal from warehouse has been done

            fromStrongbox.add(tot_cost);

            try {
                //Withdraw as many resources as you need from warehouse
                fromStrongbox.remove(player.getBoard().getWarehouse().withdraw(tot_cost));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Withdraw the rest from strongbox
            player.getBoard().getStrongbox().withdraw(fromStrongbox);

            //Adding output
            for(Production production : chosenProduction) {

                //Removes faith from resource, otherwise it will end up in Strongbox!

                Resources output = makePlayerChoose(player, production.getOutput()); //If player has choice in output he has to choose here

                //Removes faith from resource, otherwise it will end up in Strongbox!
                output = faithCheck(player, output);

                //Add the output of production to the strongbox
                player.getBoard().getStrongbox().deposit(output);

            }
        }

        //update
        updateWarehouse(player);
        updateFaithPoints();

        modelControllerIOHandler.sendMessage(player.getNickname(), Message.OK);
        return true;
    }


    /**
     * Plays the Leader Card the player has chosen
     * @param player
     * @param chosenLeader
     */
    private void playLeaderUpdate(Player player, LeadCard chosenLeader){

        if(chosenLeader.affordable(player)){
            chosenLeader.play(player);

            //update
            updateLeaders(player);
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.OK);
        }else{
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.REQUIREMENTS_NOT_MET);
        }
    }


    /**
     * Discard the Leader Card the player has chosen
     * @param player
     * @param chosenLeader
     */
    private void discardLeaderUpdate(Player player, LeadCard chosenLeader){

        chosenLeader.discard(player);

        //update
        updateLeaders(player);

        //I suppose no message is needed here since the leader is for sure discarded, so the game just goes on
    }


    /**
     * Makes the player choose all the resources he wants from the input resources, if it has any choices
     * @param p player
     * @param r resource to check
     * @return The resource, but instead of choice it has been updated with the player choices
     */
    private Resources makePlayerChoose(Player p, Resources r){

        Resources no_choice = new Resources();
        no_choice.add(r);

        //ask player to choose resource until he has finished all choices
        while (no_choice.getAmountOf(ResourceType.CHOICE)>0) {

            no_choice.add(askPlayerToChooseResource(p));

            try {
                no_choice.remove(ResourceType.CHOICE, 1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return no_choice;

    }


    /**
     * Given a resource removes faith from it and gives the player the equivalent faith points
     * @param player
     * @param res the resource to check
     * @return the resource without the faith
     */

    private Resources faithCheck(Player player, Resources res){

        if (res.getAmountOf(ResourceType.FAITH) > 0) {
            //increase the faith points
            player.getBoard().incrementFaithPoints(res.getAmountOf(ResourceType.FAITH));
            //remove the faith from resources
            try {
                res.remove(ResourceType.FAITH, res.getAmountOf(ResourceType.FAITH));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }


    /**
     * Methods check if player has any white marble replacements from his leadCards
     * If so he whether replaces automatically in case the player has only one
     * or asks player which one he chooses
     * If not, just removes the blanks from the resource (otherwise they would have to be placed in the warehouse)
     * @param player
     * @param res resources to check for white
     * @return resources after the white have been removed/replaced
     */

    protected Resources checkWhite(Player player, Resources res){

        int whiteReplacements = 0;
        ArrayList<ResourceType> replaceTypes = new ArrayList<>();

        for (int i = 0; i < player.getLeaders().getPlayedCards().size(); i++) {

            //if player has a leader with the white marble replacement not blank

            if (player.getLeaders().getPlayedCards().get(i).getAbility().getWhiteMarbleReplacement() != ResourceType.BLANK) {
                whiteReplacements++;
                replaceTypes.add(player.getLeaders().getPlayedCards().get(i).getAbility().getWhiteMarbleReplacement());
            }
        }

        switch (whiteReplacements) {
            case 1:
                //automatically replace the white marble with the one granted from the leader ability
                res.replaceWhite(replaceTypes.get(0));
                break;
            case 2:

                //asking the player to choose one of the two resources he can to substitute white

                //Maybe this line will need to be switched off in the real game, but it is necessary for testing
                modelControllerIOHandler.setExpectedAction(Action.CHOOSE_RESOURCE, player.getNickname());

                ChooseResourceActionData data = modelControllerIOHandler.getResponseData();
                Resources choice = data.getResources();

                //Converting the choice from resources in resource type and adding it
                for (ResourceType type : ResourceType.values()) {
                    //also checking if player choice is effectively one of the types he can replace
                    if(choice.getAmountOf(type) > 0 && (type.equals(replaceTypes.get(0)) || type.equals(replaceTypes.get(1)))){
                        res.replaceWhite(type);
                        break;
                    }
                }
                break;

                //Than just replace white with the player choice
            default:
                //replace nothing, but do remove the white
                try {
                    res.remove(ResourceType.BLANK, res.getAmountOf(ResourceType.BLANK));
                } catch (ResourcesException e) {
                    e.printStackTrace();
                }
        }
        return res;

    }


    /**THESE ARE THE METHODS INSIDE PLAYTURN
     *
     * They call their respective Update methods
     * The only difference is they take care of the I/O - getting the player choice
     * and whether the primary action has been used
     */

    private boolean resourceMarket(Player player, boolean primaryActionUsed){

        //communicationHandler.setExpectedAction(Action.RESOURCE_MARKET, player.getNickname());
        ResourceMarketActionData playerChoice = modelControllerIOHandler.getResponseData();

        //Supposing the player will have to make choice
        modelControllerIOHandler.setExpectedAction(Action.CHOOSE_RESOURCE, player.getNickname());
        modelControllerIOHandler.addExpectedAction(Action.REARRANGE_WAREHOUSE);

        //Do this action only if the player has not used his primary action
        if(!primaryActionUsed){
            primaryActionUsed = resourceMarketUpdate(player, playerChoice.isRow(), playerChoice.getIndex());
        }else{
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.ALREADY_USED_PRIMARY_ACTION);
        }

        return primaryActionUsed;
    }

    private boolean devCardMarket(Player player, boolean primaryActionUsed){

        //communicationHandler.setExpectedAction(Action.DEV_CARD, player.getNickname());
        DevCardActionData devCardChoice = modelControllerIOHandler.getResponseData();

        //Do this action only if the player has not used his primary action
        if(!primaryActionUsed){
            primaryActionUsed = devCardMarketUpdate(player, devCardChoice.getChosenCard(), devCardChoice.getPosition());
        }else{
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.ALREADY_USED_PRIMARY_ACTION);
        }

        return primaryActionUsed;
    }

    private boolean produce(Player player, boolean primaryActionUsed){

        //communicationHandler.setExpectedAction(Action.PRODUCE, player.getNickname());
        ProduceActionData produceChoice = modelControllerIOHandler.getResponseData();

        //Warning: May need to set the action as expecting action choice
        //but if this resets the action taken, than the whole production choice order should be changed.

        //Do this action only if the player has not used his primary action
        if(!primaryActionUsed){
            primaryActionUsed = produceUpdate(player, produceChoice.getChosenProd());
        }else{
            modelControllerIOHandler.sendMessage(player.getNickname(), Message.ALREADY_USED_PRIMARY_ACTION);
        }

        return primaryActionUsed;
    }

    private void playLeader(Player player){

        //communicationHandler.setExpectedAction(Action.PlAY_LEADER, player.getNickname());
        ChooseLeaderActionData playLeaderEventData = modelControllerIOHandler.getResponseData();

        playLeaderUpdate(player, playLeaderEventData.getChosenLeader());
    }

    private void discardLeader(Player player){

        //communicationHandler.setExpectedAction(Action.DISCARD_LEADER, player.getNickname());
        ChooseLeaderActionData discardLeaderEventData = modelControllerIOHandler.getResponseData();

        discardLeaderUpdate(player, discardLeaderEventData.getChosenLeader());
    }


    /**
     * Method for making the faithUpdate easier (sending message to view with updated faith points for each player)
     */

    private void updateFaithPoints(){

        int fp[] = new int[4];

        for (int i = 0; i < game.getPlayers().length; i++) {
            fp[i] = game.getPlayers()[i].getBoard().getFaithPoints();
        }

        FaithUpdateData fUp = new FaithUpdateData(fp);

        modelControllerIOHandler.pushUpdate(Update.FAITH, fUp);
    }

    /**
     * All the other update methods
     * @param player
     */

    private void updateWarehouse(Player player){

        WarehouseUpdateData wUp = new WarehouseUpdateData(player.getBoard().getWarehouse(), player.getNickname());
        modelControllerIOHandler.pushUpdate(Update.WAREHOUSE, wUp);
    }

    private void updateResourceMarket(){

        ResourceMarketUpdateData resUp = new ResourceMarketUpdateData(game.getResourceMarket());
        modelControllerIOHandler.pushUpdate(Update.RESOURCE_MARKET, resUp);
    }

    private void updateDevCardMarket(){

        DevCardMarketUpdateData devUp = new DevCardMarketUpdateData(game.getDevCardMarket());
        modelControllerIOHandler.pushUpdate(Update.DEVCARD_MARKET, devUp);
    }

    private void updateProductionPowers(Player player){

        ProductionPowersUpdateData ppUp = new ProductionPowersUpdateData(player.getBoard().getProductionPowers(), player.getNickname());
        modelControllerIOHandler.pushUpdate(Update.PRODUCTION_POWERS, ppUp);
    }

    private void updateLeaders(Player player){

        PlayerLeadersUpdateData plUp = new PlayerLeadersUpdateData(player.getLeaders(), player.getNickname());
        modelControllerIOHandler.pushUpdate(Update.LEADERS, plUp);
    }

    private void updateVP(int[] VP){

        VPUpdateData VPUp = new VPUpdateData(VP, game.getPlayerNames());
        modelControllerIOHandler.pushUpdate(Update.VP, VPUp);
    }

    private void updateActionToken(){

        ActionTokenUpdateData ATUp = new ActionTokenUpdateData(Lorenzo.getLastPlayedToken(), Lorenzo.getCross());
        modelControllerIOHandler.pushUpdate(Update.SOLO_ACTION, ATUp);
    }

}
