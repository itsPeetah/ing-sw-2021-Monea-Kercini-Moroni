package it.polimi.ingsw.application.cli.components.scenes;

import it.polimi.ingsw.application.cli.components.CLIScene;

/**
 * Loading scene for the game
 */
public class CLIWait extends CLIScene {

    @Override
    public void show() {
        println("Please wait...");
    }
}
