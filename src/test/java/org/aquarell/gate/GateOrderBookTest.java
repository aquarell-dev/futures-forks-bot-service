package org.aquarell.gate;

import org.aquarell.exchanges.Exchange;
import org.aquarell.orderbook.IOrderBookTest;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.orderbook.OrderBookHelper;

public class GateOrderBookTest implements IOrderBookTest {
    public OrderBook getOrderBook() {
        OrderBook gateOrderBook = new GateOrderBook("btc", 1d);
        initializeOrderBooks(gateOrderBook);
        return gateOrderBook;
    }

    public void initializeOrderBooks(OrderBook orderBook) {
        orderBook.updateAsks(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.GATE,
                10,
                10,
                8
            ),
            false
        );
        orderBook.updateBids(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.GATE,
                10,
                5,
                4
            ),
            false
        );
    }
}
