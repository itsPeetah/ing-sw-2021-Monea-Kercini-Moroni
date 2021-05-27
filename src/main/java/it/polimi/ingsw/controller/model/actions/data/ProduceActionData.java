package it.polimi.ingsw.controller.model.actions.data;

import it.polimi.ingsw.controller.model.actions.ActionData;
import it.polimi.ingsw.model.general.Production;

import java.util.ArrayList;

public class ProduceActionData extends ActionData {
    ArrayList<Production> chosenProductions;

    /**
     * Constructor for GSON
     */

    public ProduceActionData(ArrayList<Production> chosenProductions) {
        this.chosenProductions = chosenProductions;
    }

    public ArrayList<Production> getChosenProd() {
        return chosenProductions;
    }
}
