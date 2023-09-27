package org.aquarell.bybit;

import org.aquarell.exchanges.Exchange;
import org.aquarell.exchanges.ExchangeClient;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.tickers.TickersManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BybitExchangeClient extends ExchangeClient {
    public BybitExchangeClient(TickersManager tickersManager) {
        super(Exchange.BYBIT, tickersManager);
    }

    @Override
    public void onMessage(String message){
        JSONObject jsonMessage = new JSONObject(message);

        if (jsonMessage.has("topic") && jsonMessage.getString("topic").contains("orderbook")) {
            String type = jsonMessage.getString("type");

            JSONObject data = jsonMessage.getJSONObject("data");

            String ticker = data.getString("s");

            OrderBook orderBook = orderBooks.get(ticker);
            if (orderBook == null) return;

            Boolean isSnapShot = "snapshot".equals(type);

            JSONArray bids = data.getJSONArray("b");
            JSONArray asks = data.getJSONArray("a");

            orderBook.updateAsks(asks, isSnapShot);
            orderBook.updateBids(bids, isSnapShot);
        }
    }

    @Override
    protected void subscribeToOrderBook(String coin) {
        String ticker = coinToTicker(coin);

        if (orderBooks.containsKey(coinToTicker(coin))) return;

        send(createOrderbookSubscriptionMessage(coin));

        orderBooks.put(ticker, new BybitOrderBook(coin));

        logger.info(exchange.exchangeTitle + ": Subscribed to " + coin.toUpperCase());
    }

    private String createOrderbookSubscriptionMessage(String coin) {
        JSONObject subscriptionMessage = new JSONObject();
        subscriptionMessage.put("op", "subscribe");
        subscriptionMessage.put("args", new JSONArray(String.format("[\"orderbook.50.%s\"]", coinToTicker(coin))));
        return subscriptionMessage.toString();
    }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.toLowerCase().substring(0, ticker.length() - 4);
    }

    @Override
    protected String coinToTicker(String coin) {
        return coin.toUpperCase() + "USDT";
    }
}
