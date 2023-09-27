package org.aquarell.orderbook;

public class NotEnoughVolumeInOrderBook extends Exception {
    public NotEnoughVolumeInOrderBook(String message) {
        super(message);
    }
}
