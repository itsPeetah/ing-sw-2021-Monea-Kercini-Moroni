package it.polimi.ingsw.application.cli.components.scenes;

import it.polimi.ingsw.application.cli.components.CLIScene;
import it.polimi.ingsw.application.cli.components.scenes.game.*;
import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.common.GameApplicationIOHandler;
import it.polimi.ingsw.controller.model.actions.ActionPacket;
import it.polimi.ingsw.controller.view.game.GameController;
import it.polimi.ingsw.controller.view.game.GameState;
import it.polimi.ingsw.controller.view.game.handlers.GameControllerIOHandler;

public class CLIGame extends CLIScene {

    private CLIScene board = new CLIBoard();
    private CLIScene leadChoice = new CLILeadChoice();
    private CLIScene resourceChoice = new CLIResourceChoice();
    private CLIScene warehouseOrganizing = new CLIWarehouseOrganizing();

    private GameController gameController;
    private GameControllerIOHandler gameControllerIO;

    private CLIScene currentView;

    private GameState currentGameState, previousGameState;

    public CLIGame() {
        super();
    }

    public void init() {
        this.gameController = GameApplication.getInstance().getGameController();
        this.gameControllerIO = GameApplication.getInstance().getGameControllerIO();
        currentGameState = gameController.getCurrentState();
        previousGameState = GameState.UNKNOWN;
    }

    @Override
    public void update() {

        currentGameState = gameController.getCurrentState();

        if (currentGameState != previousGameState) {
            currentView = selectCurrentView(currentGameState);
            show();
        }

        previousGameState = currentGameState;
    }

    @Override
    public void show() {
        clearConsole();
        println("|         Masters of Renaissance - In Game         |");
        println("+--------------------------------------------------+");
        println("Current state: " + currentGameState.toString());

        if (currentView == null) {
            println("");
            println("Please wait...");
            println("");
        } else {
            currentView.update();
            currentView.show();
        }
        println("====================================================");
    }

    @Override
    public void help() {
        if (currentView != null) currentView.help();
        else super.help();
    }

    @Override
    public void execute(String command, String[] arguments) {
        if (currentView != null) currentView.execute(command, arguments);
        else {
            switch (command) {
                case "help":
                    help();
                    break;
                default:
                    error("Unsupported command.");
                    break;
            }
        }
    }

    private CLIScene selectCurrentView(GameState currentState) {
        switch (currentState) {
            case CHOOSE_LEADERS:
                return leadChoice;
            case PICK_RESOURCES:
                return resourceChoice;
            case ORGANIZE_WAREHOUSE:
                return warehouseOrganizing;
            default:
                return board;
        }
    }

    public static void pushAction(ActionPacket ap) {

        GameApplication.getInstance().getGameController().moveToState(GameState.IDLE);

        if (GameApplication.getInstance().getGameController().isSinglePlayer() && !GameApplication.getInstance().isOnNetwork()) {
            GameApplication.getInstance().getGameControllerIO().notifyAction(ap);
        } else {
            GameApplicationIOHandler.getInstance().pushAction(ap);
        }
    }
}
