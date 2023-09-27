package org.aquarell.orderbook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public interface IOrderBookTest {
    public OrderBook getOrderBook();

    public void initializeOrderBooks(OrderBook orderBook);

    @Test
    default void testCalculateAverageBuyPrice_FillByBestPrice() throws NotEnoughVolumeInOrderBook {
        assertEquals(8, getOrderBook().calculateBuyAveragePrice(80));
        assertEquals(8, getOrderBook().calculateBuyAveragePrice(10));
    }

    @Test
    default void testCalculateAverageSellPrice_FillByBestPrice() throws NotEnoughVolumeInOrderBook {
        assertEquals(5, getOrderBook().calculateSellAveragePrice(50));
        assertEquals(5, getOrderBook().calculateSellAveragePrice(10));
    }

    @Test
    default void testCalculateAverageSellPrice_FillByWorstPrice() throws NotEnoughVolumeInOrderBook {
        assertEquals(4.5, getOrderBook().calculateSellAveragePrice(90));
    }

    @Test
    default void testCalculateAverageBuyPrice_FillByWorstPrice() throws NotEnoughVolumeInOrderBook {
        assertEquals(9, getOrderBook().calculateBuyAveragePrice(180));
    }

    @Test
    default void testCalculateAverageBuyPrice_PartialFill() throws NotEnoughVolumeInOrderBook {
        assertEquals(8.75, getOrderBook().calculateBuyAveragePrice(140));
    }

    @Test
    default void testCalculateAverageSellPrice_PartialFill() throws NotEnoughVolumeInOrderBook {
        assertEquals(4.625, getOrderBook().calculateSellAveragePrice(74));
    }

    @Test
    default void testCalculateAveragePrice_USDTVolumeGreaterThanVolumeInOrderbook() {
        assertThrows(NotEnoughVolumeInOrderBook.class, () -> getOrderBook().calculateSellAveragePrice(100));
        assertThrows(NotEnoughVolumeInOrderBook.class, () -> getOrderBook().calculateBuyAveragePrice(200));
    }

    @Test
    default void testGetBestBid() {
        assertEquals(5, getOrderBook().getBestBid());
    }

    @Test
    default void testGetBestAsk() {
        assertEquals(8, getOrderBook().getBestAsk());
    }
}
