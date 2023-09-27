package org.aquarell.orderbook;

import org.aquarell.exchanges.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public class OrderBookHelper {
    public static JSONArray getOrderBookQuotes(Exchange exchange, int size, double ...prices) {
        JSONArray orderBook = new JSONArray();

        DoubleStream.of(prices).forEach(price -> {
            orderBook.put(switch (exchange) {
                case BYBIT -> getBybitOrderBookQuotes(size, price);
                case GATE -> getGateOrderBookQuotes(size, price);
            });
        });

        return orderBook;
    }

    /**
     * @param quotes - 2d array, where first item is the size, and the second is the price
     */
    public static JSONArray getOrderBookQuotes(Exchange exchange, double[][] quotes) {
        JSONArray orderBook = new JSONArray();

        Arrays.stream(quotes).forEach(quote -> {
            orderBook.put(switch (exchange) {
                case BYBIT -> getBybitOrderBookQuotes((int) quote[0], quote[1]);
                case GATE -> getGateOrderBookQuotes((int) quote[0], quote[1]);
            });
        });

        return orderBook;
    }

    private static JSONObject getGateOrderBookQuotes(int size, Double price) {
        return new JSONObject(String.format("{\"p\": \"%s\", \"s\": \"%s\"}", price, size));
    }

    private static JSONArray getBybitOrderBookQuotes(int size, Double price) {
        return new JSONArray(new double[]{price, size});
    }
}
