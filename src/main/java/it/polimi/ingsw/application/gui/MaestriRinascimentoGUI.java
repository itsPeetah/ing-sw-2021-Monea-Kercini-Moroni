package it.polimi.ingsw.application.gui;

import it.polimi.ingsw.application.common.GameApplication;
import it.polimi.ingsw.application.common.GameApplicationMode;

public class MaestriRinascimentoGUI {

    public static void main(String[] args){

        // Create the backend application
        GameApplication gameApplication = GameApplication.getInstance();

        // Set GUI output mode
        GameApplication.setOutputMode(GameApplicationMode.GUI);

        // Create the stage and start the GUI
        /*new Thread(() -> GUIApplication.main(args)).start();*/
        GUIApplication.main(args);

    }
}
