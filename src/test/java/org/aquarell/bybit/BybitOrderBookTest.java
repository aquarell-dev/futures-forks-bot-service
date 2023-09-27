package org.aquarell.bybit;

import org.aquarell.exchanges.Exchange;
import org.aquarell.orderbook.IOrderBookTest;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.orderbook.OrderBookHelper;

public class BybitOrderBookTest implements IOrderBookTest {
    public OrderBook getOrderBook() {
        OrderBook bybitOrderBook = new BybitOrderBook("btc");
        initializeOrderBooks(bybitOrderBook);
        return bybitOrderBook;
    }

    public void initializeOrderBooks(OrderBook orderBook) {
        orderBook.updateAsks(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.BYBIT,
                10,
                10,
                8
            ),
            false
        );
        orderBook.updateBids(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.BYBIT,
                10,
                5,
                4
            ),
            false
        );
    }
}
