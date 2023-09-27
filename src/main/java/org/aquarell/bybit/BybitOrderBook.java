package org.aquarell.bybit;

import org.aquarell.exchanges.Exchange;
import org.aquarell.orderbook.OrderBook;
import org.json.JSONArray;

import java.util.Map;

public class BybitOrderBook extends OrderBook {
    public BybitOrderBook(String coin) {
        super(coin, Exchange.BYBIT);
    }

    @Override
    public void updateBids(JSONArray bidsArray, Boolean snapshot) {
        if (snapshot) {
            initializeOrderBook(bids, bidsArray);
            return;
        }

        updateOrderBook(bids, bidsArray);
    }

    @Override
    public void updateAsks(JSONArray asksArray, Boolean snapshot) {
        if (snapshot) {
            initializeOrderBook(asks, asksArray);
            return;
        }

        updateOrderBook(asks, asksArray);
    }

    private synchronized void initializeOrderBook(Map<Double, Double> map, JSONArray data) {
        map.clear();

        for (int i = 0; i < data.length(); i++) {
            JSONArray entry = data.getJSONArray(i);
            map.put(entry.getDouble(0), entry.getDouble(1));
        }
    }

    private synchronized void updateOrderBook(Map<Double, Double> map, JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONArray entry = data.getJSONArray(i);

            double price = entry.getDouble(0);
            double size = entry.getDouble(1);

            if (size == 0) {
                map.remove(price);
            } else {
                map.put(price, size);
            }
        }
    }
}
