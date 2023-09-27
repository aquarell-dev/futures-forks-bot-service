package org.aquarell.gate;

import org.aquarell.exchanges.Exchange;
import org.aquarell.exchanges.ExchangeClient;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.tickers.TickersManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GateExchangeClient extends ExchangeClient {
    private final String ORDERBOOK_CHANNEL = "futures.order_book";
    private final String ORDERBOOK_EVENT = "all";
    private Map<String, Double> quantoMultipliersMap = new HashMap<>();

    public GateExchangeClient(TickersManager tickersManager) {
        super(Exchange.GATE, tickersManager);
        quantoMultipliersMap = getCoinsQuantoMultipliers();
    }

    private HashMap<String, Double> getCoinsQuantoMultipliers() {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("https://api.gateio.ws/api/v4/futures/usdt/contracts"))
            .setHeader("Content-Type", "application/json")
            .setHeader("Accept", "application/json")
            .build();

        HashMap<String, Double> quantoMultipliersMap = new HashMap<>();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray arr = new JSONArray(response.body());

            arr.forEach(coin -> {
                JSONObject coinObject = new JSONObject(coin.toString());
                quantoMultipliersMap.put(
                    coinObject.getString("name").replace("_USDT", "").toLowerCase(),
                    coinObject.getDouble("quanto_multiplier")
                );
            });

            return quantoMultipliersMap;
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Couldn't get quanto multipliers"); // TODO come up with a better error
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);

            String channel = jsonMessage.getString("channel");
            String event = jsonMessage.getString("event");

            try {
                JSONObject result = jsonMessage.getJSONObject("result");
                if (ORDERBOOK_CHANNEL.equals(channel) && ORDERBOOK_EVENT.equals(event)) {
                    String ticker = result.getString("contract");

                    OrderBook orderBook = orderBooks.get(ticker);

                    if (orderBook == null) return;

                    orderBook.updateBids(result.getJSONArray("bids"), false);
                    orderBook.updateAsks(result.getJSONArray("asks"), false);
                }
            } catch (JSONException e) {
                System.out.println(jsonMessage);
            }
        } catch (JSONException e) {
            logger.error(exchange.exchangeTitle + ": Error parsing JSON - " + e);
        }
    }

    @Override
    protected void subscribeToOrderBook(String coin) {
        String ticker = coinToTicker(coin);

        if (!orderBooks.containsKey(ticker)) {
            OrderBook orderBook = new GateOrderBook(coin, quantoMultipliersMap.get(coin));
            orderBooks.put(ticker, orderBook);
            send(createOrderbookSubscriptionMessage(ticker));
        }
        logger.info(Exchange.GATE.exchangeTitle + ": Subscribed to " + coin.toUpperCase());
    }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.toLowerCase().substring(0, ticker.length() - 5);
    }

    @Override
    protected String coinToTicker(String coin) {
        return coin.toUpperCase() + "_USDT";
    }

    private String createOrderbookSubscriptionMessage(String ticker) {
        JSONObject subscribeMessage = new JSONObject();
        subscribeMessage.put("time", System.currentTimeMillis() / 1000);
        subscribeMessage.put("channel", "futures.order_book");
        subscribeMessage.put("event", "subscribe");
        subscribeMessage.put("payload", new JSONArray(String.format("[\"%s\", \"20\", \"0\"]", ticker.toUpperCase())));

        return subscribeMessage.toString();
    }
}
