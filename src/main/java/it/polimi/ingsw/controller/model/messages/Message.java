package it.polimi.ingsw.controller.model.messages;

public enum Message {
    ERROR("ERROR"),
    OK("Action executed correctly."),
    SETTING_UP_GAME("The players are making their choices, please wait for your turn."),
    GAME_HAS_STARTED("The game has begun!"),
    TURN_PASSED("A player has ended his turn."),
    CHOOSE_LEADERS("Choose your 2 leaders to keep in your hand."),
    CHOOSE_RESOURCE("Please choose a resource of your choice."),
    CHOOSE_REPLACEMENT("Please choose a resource of your choice."),
    INCORRECT_REPLACEMENT("You don't have a leader that lets you get that resource."),
    START_TURN("It's your turn!"),
    NOT_ENOUGH_RESOURCES("You don't have enough resources."),
    ILLEGAL_CARD_PLACE("You can't put the card there."),
    REQUIREMENTS_NOT_MET("You don't meet the requirements to play this card."),
    ALREADY_USED_PRIMARY_ACTION("You have already used your primary action."),
    WAREHOUSE_UNORGANIZED("Please organize your warehouse."),
    WINNER("VICTORY!"),
    LOSER("WASTED! You were no match for Lorenzo's magnificence!"),
    LOSER_MULTIPLAYER("WASTED!"),
    SELECT_INPUT("Getting the resources needed for your production."),
    SELECT_OUTPUT("Producing the new resources.");

    private final String message;

    Message(String message) {
        this.message = message;
    }
    @Override
    public String toString() {
        return message;
    }
}
