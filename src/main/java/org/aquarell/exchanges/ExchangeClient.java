package org.aquarell.exchanges;

import org.aquarell.orderbook.OrderBook;
import org.aquarell.tickers.TickersManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ExchangeClient extends WebSocketClient {
    public final Exchange exchange;
    public final List<String> coins;
    public final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    protected final Logger logger = LoggerFactory.getLogger(ExchangeClient.class);

    public ExchangeClient(Exchange exchange, TickersManager tickersManager) {
        super(exchange.connectionUri);
        this.exchange = exchange;
        this.coins = tickersManager.getCoinsByKey(exchange.exchangeTitle.toLowerCase());
    }

    protected abstract void subscribeToOrderBook(String coin);

    public abstract String tickerToCoin(String ticker);

    protected abstract String coinToTicker(String coin);

    public OrderBook getOrderBookByCoin(String coin) {
        String ticker = coinToTicker(coin);

        if (!orderBooks.containsKey(ticker)) return null;

        return orderBooks.get(ticker);
    }

    private boolean isOrderBookFilled(OrderBook orderBook) {
        return !orderBook.getAsks().isEmpty() && !orderBook.getBids().isEmpty();
    }

    /**
     * @return true if every orderbook is not empty(bids and asks are not empty)
     */
    public boolean areOrderBooksFilled() {
        if (orderBooks.size() != coins.size()) return false;

        return orderBooks.values()
            .stream()
            .allMatch(this::isOrderBookFilled);
    }

    public List<String> getUnfilledOrderBooks() {
        return Collections.singletonList(String.join(", ", orderBooks.values()
            .stream().filter(ob -> !isOrderBookFilled(ob)).map(orderBook -> orderBook.coin).toList()));
    }

    @Override
    public void run() {
        super.run();
        logger.info(exchange.exchangeTitle + " thread has started");
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info(this.exchange.exchangeTitle + ": Connection established");
        for (String coin : coins) subscribeToOrderBook(coin);
    }

    @Override
    public abstract void onMessage(String s);

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info(this.exchange.exchangeTitle + ": Connection closed. " + code + ". " + reason);
    }

    @Override
    public void onError(Exception e) {
        logger.error(this.exchange.exchangeTitle + ": WebSocketError - " + e);
    }
}
