package it.polimi.ingsw.application.cli.components.scenes.game;

import it.polimi.ingsw.application.cli.components.ASCIIElements.*;
import it.polimi.ingsw.application.cli.components.CLIScene;
import it.polimi.ingsw.application.cli.components.scenes.CLIGame;
import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.controller.model.actions.Action;
import it.polimi.ingsw.controller.model.actions.ActionPacket;
import it.polimi.ingsw.controller.model.actions.data.*;
import it.polimi.ingsw.model.cards.DevCard;
import it.polimi.ingsw.model.general.Production;
import it.polimi.ingsw.model.playerboard.ProductionPowers;
import it.polimi.ingsw.model.playerleaders.CardState;
import it.polimi.ingsw.util.JSONUtility;
import it.polimi.ingsw.view.data.common.DevCardMarket;
import it.polimi.ingsw.view.data.common.MarketTray;
import it.polimi.ingsw.view.data.player.PlayerLeaders;
import it.polimi.ingsw.view.data.player.Strongbox;
import it.polimi.ingsw.view.data.player.Warehouse;

import java.util.ArrayList;
import java.util.List;

/**
 * CLIScene representing the main game board
 */
public class CLIBoard extends CLIScene {

    @Override
    public void help() {
        println("Visualization:");
        println("User \"players\" to visualize the player list.");
        println("Use \"view <'mt'|'dcm'|'ft'>\" to visualize the Market Tray, the Development Card Market or the Fait Track.");
        println("Use \"view dev [<player>]\" to visualize someone's development cards (omitting the player will show yours).");
        println("Use \"view res [<player>]\" to visualize someone's warehouse and strongbox resources (omitting the player will show yours).");
        println("Use \"view leaders [<player>]\" to visualize someone's leaders (omitting the player will show yours) (you won't be able to see non-activated leaders from other players).");
        println("Actions:");
        println("Use \"get <'row'|'col'> <index>\" to acquire resources from the market tray.");
        println("Use \"buy <color> <tier> <stack_index>\" to buy a development card.");
        println("\tColors: b (blue), g (green), p (purple), y (yellow), tiers: 1, 2, 3, stack_indices: 1, 2, 3");
        println("Use \"produce <indices>\" to activate your productions (indices: 0 for default, 1-3 for dev cards, 4-5 for leaders).");
        println("\tExample usage: \"produce 0 1 2 3\" to activate all production.");
        println("\tExample usage: \"produce 0\" to only activate the default production.");
        println("\tExample usage: \"produce 1 4\" to only activate the productions in the first stack and the first active leader.");
        println("Use \"activate <1|2>\" to activate a leader.");
        println("Use \"discard <1|2>\" to discard a leader.");
        println("User \"end\" to end your turn.");
        println("User \"clear\" to clear the screen.");
    }

    @Override
    public void execute(String command, String[] arguments) {
        if ((arguments == null || arguments.length < 1) && ( !"end endturn help players clear".contains(command))) {
            error("Missing arguments.");
            return;
        }

        switch (command) {
            case "clear":
                clearConsole();
                break;
            case "players":
                printPlayerList();
                break;
            case "v":
            case "view":
                onView(arguments);
                break;
            case "get":
                getResources(arguments);
                break;
            case "buy":
                onBuy(arguments);
                break;
            case "produce":
                onProduce(arguments);
                break;
            case "activate":
                onPlayLeader(arguments, true);
                break;
            case "discard":
                onPlayLeader(arguments, false);
                break;
            case "end":
                onEndTurn();
                break;
            case "help":
                help();
                break;
            default:
                error("Invalid command");
                break;
        }
    }

    /**
     * Print the dev card market to the screen
     */
    public void printDevCardMarket() {
        DevCardMarket dcm = GameApplication.getInstance().getGameController().getGameData().getCommon().getDevCardMarket();
        ASCIIDevCardMarket.draw(dcm);
    }

    /**
     * Print the market tray to the screen
     */
    public void printMarketTray() {
        MarketTray mt = GameApplication.getInstance().getGameController().getGameData().getCommon().getMarketTray();
        ASCIIMarketTray.draw(mt);
    }

    /**
     * Print a player's resources (WH+SB) to the screen
     */
    private void printResources(String player) {
        if (player == null) player = GameApplication.getInstance().getUserNickname();
        if (!GameApplication.getInstance().getGameController().getGameData().getPlayersList().contains(player)) {
            error("The desired player does not exist.");
            return;
        }
        Warehouse wh = GameApplication.getInstance().getGameController().getGameData().getPlayerData(player).getWarehouse();
        Strongbox sb = GameApplication.getInstance().getGameController().getGameData().getPlayerData(player).getStrongbox();
        ASCIIWarehouse.draw(wh);
        ASCIIStrongbox.draw(sb);
    }

    /**
     * Print the production powers of a player to the screen
     */
    private void printProductions(String player) {
        if (player == null) player = GameApplication.getInstance().getUserNickname();
        if (!GameApplication.getInstance().getGameController().getGameData().getPlayersList().contains(player)) {
            error("The desired player does not exist.");
            return;
        }
        ASCIIDevelopment.draw(player);
    }

    /**
     * Print a player's leader cards to the screen
     * @param player
     */
    private void printLeaders(String player) {
        if (player == null) player = GameApplication.getInstance().getUserNickname();
        if (!GameApplication.getInstance().getGameController().getGameData().getPlayersList().contains(player)) {
            error("The desired player does not exist.");
            return;
        }
        ASCIIPlayerLeaders.draw(player);
    }

    /**
     * Process the "view" command
     */
    private void onView(String[] args) {
        switch (args[0]) {
            case "mt":
                printMarketTray();
                break;
            case "dcm":
                printDevCardMarket();
                break;
            case "ft":
                ASCIIFaithPath.draw(GameApplication.getInstance().getGameController().getGameData(),
                        GameApplication.getInstance().getGameController().isSinglePlayer());
                break;
            case "leaders":
            case "lead":
                printLeaders(args.length < 2 ? null : args[1]);
                break;
            case "dev":
                printProductions(args.length < 2 ? null : args[1]);
                break;
            case "res":
                printResources(args.length < 2 ? null : args[1]);
                break;
            case "players":
                printPlayerList();
                break;
            default:
                error("Invalid argument.");
                break;
        }
    }

    /**
     * Procedure for getting resources from the MT
     * @param args command arguments
     */
    private void getResources(String[] args) {
        // Missing args
        if (args.length < 2) {
            error("Missing arguments");
            return;
        }
        // Get index
        int index = 0;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            error("Could not parse the index.");
            return;
        }
        // Validate index
        if (index < 1 || index > 4) {
            error("Index should be within 1-4");
            return;
        }
        boolean row;
        switch (args[0]) {
            case "row":
                // Row index
                if (index > 3) {
                    error("Row index should be within 1-3");
                    return;
                }
                row = true;
                break;
            case "col":
                row = false;
                break;
            default:
                error("Invalid argument, use either 'row' or 'col'.");
                return;
        }

        ResourceMarketActionData rmad = new ResourceMarketActionData(row, index - 1);
        rmad.setPlayer(GameApplication.getInstance().getUserNickname());
        ActionPacket ap = new ActionPacket(Action.RESOURCE_MARKET, JSONUtility.toJson(rmad, ResourceMarketActionData.class));
        CLIGame.pushAction(ap);
    }

    /**
     * Procedure for buying a DevCard from the DCM
     * @param args command arguments
     */
    private void onBuy(String[] args){
        if(args.length < 3){
            error("Missing arguments.");
            return;
        }
        if(!"bgpy".contains(args[0]) && !"blue green purple yellow".contains(args[0])){
            error("Please specify a correct dev card color.");
            return;
        }
        if(!"123".contains(args[1])){
            error("Please specify a correct dev card tier.");
            return;
        }
        if(!"123".contains(args[2])){
            error("Please specify a correct production stack number.");
            return;
        }

        int tier = Integer.parseInt(args[1]) - 1;
        int stack = Integer.parseInt(args[2]);
        int color = -1;
        switch (args[0]){
            case "b":
            case "blue":
                color = 0;
                break;
            case "p":
            case "purple":
                color = 1;
                break;
            case "g":
            case "green":
                color = 2;
                break;
            case "y":
            case "yellow":
                color = 3;
                break;
        }

        DevCardMarket dcm = GameApplication.getInstance().getGameController().getGameData().getCommon().getDevCardMarket();
        try{
            DevCard chosen = dcm.getAvailableCards()[color][tier];

            DevCardActionData dcad = new DevCardActionData(chosen, stack - 1);
            dcad.setPlayer(GameApplication.getInstance().getUserNickname());
            ActionPacket ap = new ActionPacket(Action.DEV_CARD, JSONUtility.toJson(dcad, DevCardActionData.class));

            CLIGame.pushAction(ap);
        } catch (NullPointerException ex){
            error("The card is not available.");
            return;
        }

    }

    /**
     * Procedure to activate productions
     * @param args command arguments
     */
    private void onProduce(String[] args){
        int[] prods = new int[args.length];

        for(int i = 0; i < args.length; i++){
            try{
                prods[i] = Integer.parseInt(args[i]);
            } catch (NumberFormatException ex){
                error("Invalid arguments. Please insert only numbers between 0 and 3 (4-5 for leaders).");
                return;
            }
        }

        DevCard[] dcs = GameApplication.getInstance().getGameController().getGameData()
                .getPlayerData(GameApplication.getInstance().getUserNickname()).getDevCards().getDevCards();
        PlayerLeaders pls = GameApplication.getInstance().getGameController().getGameData()
                .getPlayerData(GameApplication.getInstance().getUserNickname()).getPlayerLeaders();
        ArrayList<Production> chosenProductions = new ArrayList<Production>();

        for(int p : prods){
            if(p > 5){
                error("Please insert only numbers between 0 and 3 (4-5 for leaders).");
                return;
            } else if (p > 3) {
                int leader = p - 4;
                if(pls.getStates()[leader] != CardState.PLAYED || pls.getLeaders()[leader].getAbility().getProduction() == null || pls.getLeaders()[leader].getAbility().getProduction().getInput().getTotalAmount() < 1){
                    error("You can't play this leader now.");
                    return;
                }
                chosenProductions.add(pls.getLeaders()[leader].getAbility().getProduction());
            } else if(p > 0){
                if(dcs[p-1] == null){
                    error("That production is not available at the moment.");
                    return;
                } else {
                    chosenProductions.add(dcs[p-1].getProduction());
                    println("Selected production in stack #" + p);
                }
            }
            else{
                chosenProductions.add(ProductionPowers.getBasicProduction());
                println("Selected default production.");
            }
        }

        ProduceActionData pad = new ProduceActionData(chosenProductions);
        pad.setPlayer(GameApplication.getInstance().getUserNickname());
        ActionPacket ap = new ActionPacket(Action.PRODUCE, JSONUtility.toJson(pad, ProduceActionData.class));

        CLIGame.pushAction(ap);
    }

    /**
     * Procedure to end the player's turn
     */
    private void onEndTurn(){
        NoneActionData nad = new NoneActionData();
        nad.setPlayer(GameApplication.getInstance().getUserNickname());
        ActionPacket ap = new ActionPacket(Action.END_TURN, JSONUtility.toJson(nad, NoneActionData.class));

        CLIGame.pushAction(ap);
    }

    /**
     * Pricedure to activate/discard a leader
     * @param args command arguments
     * @param activate if true -> activate the leader, if false -> discard
     */
    private void onPlayLeader(String[] args, boolean activate){
        int leader;
        try {
            leader=Integer.parseInt(args[0]) - 1;
        } catch (NumberFormatException ex){
            error("Invalid argument. Accepted values: 1, 2");
            return;
        }
        if(leader < 0 || leader > 1){
            error("Please select a valid leader (1, 2)");
            return;
        }
        PlayerLeaders pls = GameApplication.getInstance().getGameController().getGameData()
                .getPlayerData(GameApplication.getInstance().getUserNickname()).getPlayerLeaders();
        if(pls.getStates()[leader] != CardState.INHAND){
            error("You can't activate this leader!");
            return;
        }


        ChooseLeaderActionData clad = new ChooseLeaderActionData(pls.getLeaders()[leader]);
        clad.setPlayer(GameApplication.getInstance().getUserNickname());

        Action action = activate ? Action.PLAY_LEADER : Action.DISCARD_LEADER;

        ActionPacket ap = new ActionPacket(action, JSONUtility.toJson(clad, ChooseLeaderActionData.class));
        CLIGame.pushAction(ap);
    }

    /**
     * Print the player list to the screen
     */
    private void printPlayerList(){
        List<String> players = GameApplication.getInstance().getGameController().getGameData().getPlayersList();
        println("Players in the room:");
        for(String s:players){
            if(s.equals(GameApplication.getInstance().getUserNickname()))
                println("> " + s + "(you)");
            else println("> " + s);
        }
        println("--------------------");
    }

}
