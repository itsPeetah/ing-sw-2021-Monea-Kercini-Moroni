package it.polimi.ingsw.application.common.listeners;

import it.polimi.ingsw.controller.model.messages.Message;
import it.polimi.ingsw.network.common.SystemMessage;
import org.jetbrains.annotations.Nullable;

public interface PacketListener extends GameMessageListener, SystemMessageListener {


    @Override
    default void onMessage(Message message) {

    }

    @Override
    default void onSystemMessage(SystemMessage type, @Nullable String additionalContent) {

    }
}
