package com.hibiscusmc.hmccosmetics.nms;

public interface NMSHandler {

    int getNextEntityId();

    default boolean getSupported () {
        return false;
    }
}
