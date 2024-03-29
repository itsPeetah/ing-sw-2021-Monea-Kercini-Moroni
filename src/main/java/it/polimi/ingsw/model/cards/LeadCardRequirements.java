package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.game.Player;
import it.polimi.ingsw.model.general.Color;
import it.polimi.ingsw.model.general.Level;
import it.polimi.ingsw.model.general.Resources;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class LeadCardRequirements {
    private HashMap<Color, Integer> devCardColors;
    private HashMap<Color, Level> devCardLevels;
    private Resources resourceAmounts;

    public LeadCardRequirements() {
    }

    public LeadCardRequirements(HashMap<Color, Integer> devCardColors, HashMap<Color, Level> devCardLevels, Resources resourceAmounts) {
        this.devCardColors = devCardColors;
        this.devCardLevels = devCardLevels;
        this.resourceAmounts = resourceAmounts;
    }

    /**
     * Get the dev cards colors requirement.
     */
    public HashMap<Color, Integer> getDevCardColors() {
        return devCardColors;
    }

    /**
     * Get the dev cards levels requirement.
     */
    public HashMap<Color, Level> getDevCardLevels() {
        return devCardLevels;
    }

    /**
     * Get the resources requirement.
     */
    public Resources getResourceAmounts() {
        return resourceAmounts;
    }

    @Override
    public String toString() {

        String dCCs = "";
        for (Color c : devCardColors.keySet()) dCCs += c.toString() + " x" + devCardColors.get(c).toString() + ", ";
        String dCLs = "";
        for (Color c : devCardLevels.keySet()) dCLs += c.toString() + " x" + devCardLevels.get(c).toString() + ", ";

        return "Requirements {" +
                "Development cards required: " + dCCs +
                ", Development card levels required =" + dCLs +
                ", Required resources:" + resourceAmounts.toString() +
                '}';
    }

    /**
     * Check if a player meets the requirements.
     */
    public Boolean check(Player player) {
        ArrayList<DevCard> playerCards = player.getBoard().getOwnedDevCards();

        /* Check resource amounts */
        if (!player.getBoard().getResourcesAvailable().isGreaterThan(resourceAmounts)) return false;

        /* Check dev card colors requirements */
        for(Color colReq: devCardColors.keySet()) {
            if(devCardColors.get(colReq) > playerCards.parallelStream().filter(dc -> dc.getColor() == colReq).count()) return false;
        }

        /* Check dev card levels requirements */
        for(Color colReq: devCardLevels.keySet()) {
            if(playerCards.parallelStream().noneMatch(lc -> lc.getColor().equals(colReq) && lc.getLevel().equals(devCardLevels.get(colReq)))) return false;
        }

        /* All checks have been passed */
        return true;
    }
}
