package it.polimi.ingsw.application.cli.components.ASCIIElements;

import it.polimi.ingsw.application.cli.util.ANSIColor;
import it.polimi.ingsw.view.data.GameData;
import it.polimi.ingsw.view.data.player.FaithTrack;

/**
 * ASCII Faith Path drawing class
 */
public class ASCIIFaithPath {

    /**
     * Draw the faith path for all players to the screen
     */
    public static void draw(GameData gameData, boolean isSinglePlayer){

        System.out.println("Faith track:");
        drawCell(1);
        for(int i = 2; i < 25; i++) {
            System.out.print("..");
            drawCell(i);
        }

        FaithTrack ft;
        for(String p : gameData.getPlayersList()){
            ft = gameData.getPlayerData(p).getFaithTrack();
            System.out.println("\n" + p + "'s faith progress:");
            System.out.println("FPs: " + ft.getFaith());
            String reports = "Reports attended:";
            int count = 0;
            for(int i = 0; i < 3; i++){
                if(ft.getReportsAttended()[i] != null && ft.getReportsAttended()[i].booleanValue()){
                    count++;
                    reports += " " + (i+1);
                }
            }
            if(count < 1) reports += " none";
            System.out.println(reports);
            System.out.println("____________");
        }
        if(isSinglePlayer){
            System.out.println("Lorenzo's faith progress: " + gameData.getCommon().getLorenzo().getBlackCross());
            System.out.println("____________");
        }

    }

    /**
     * Draw a single cell of the faith track
     * @param i index of the cell to draw
     */
    private static void drawCell(int i){
        String color = ANSIColor.RESET;
        // Vatican report range
        if((i >= 5 && i <= 7) || (i >= 12 && i <= 15) || i >= 19) color = ANSIColor.YELLOW;
        // Vatican report
        if(i % 8 == 0 && i > 0) color = ANSIColor.RED;
        // VP milestone
        if(i % 3 == 0 && i > 0) color = ANSIColor.GREEN;
        System.out.print(color + "["+ i + "]" + ANSIColor.RESET);
    }
}
