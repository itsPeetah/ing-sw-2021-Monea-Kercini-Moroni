package it.polimi.ingsw.application.cli.components.ASCIIElements;

import it.polimi.ingsw.application.cli.util.ANSIColor;
import it.polimi.ingsw.view.data.CommonData;
import it.polimi.ingsw.view.data.GameData;

public class ASCIIFaithPath {
    public static void draw(GameData gameData){

        for(int i = 0; i <= 24; i++){

            String color = ANSIColor.RESET;
            if((i >= 5 && i <= 7) || (i >= 12 && i <= 15) || i >= 19) color = ANSIColor.YELLOW;
            if(i % 8 == 0 && i > 0) color = ANSIColor.RED;
            if(i % 3 == 0 && i > 0) color = ANSIColor.GREEN;


        }
    }
}