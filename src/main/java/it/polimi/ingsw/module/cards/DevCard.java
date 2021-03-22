package it.polimi.ingsw.module.cards;

import it.polimi.ingsw.module.general.*;

public class DevCard extends Card {
    Integer level;
    Integer color;
    Resources cost;
    Production production;

    public DevCard(Integer victoryPoints, String cardId, Integer level, Integer color, Resources cost, Production production) {
        super(victoryPoints, cardId);
        this.level = level;
        this.color = color;
        this.cost = cost;
        this.production = production;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getColor() {
        return color;
    }

    public Resources getCost() {
        return cost;
    }

    @Override
    public Boolean affordable(Resources playerResources) {
        return playerResources.isGreaterThan(cost);
    }

    @Override
    public void play() {

    }
}
