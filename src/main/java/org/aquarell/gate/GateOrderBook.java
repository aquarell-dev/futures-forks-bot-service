package org.aquarell.gate;

import org.aquarell.exchanges.Exchange;
import org.aquarell.orderbook.OrderBook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class GateOrderBook extends OrderBook {
    private final Double quantoMultiplier;

    public GateOrderBook(String coin, Double quantoMultiplier) {
        super(coin, Exchange.GATE);
        this.quantoMultiplier = quantoMultiplier;
    }

    @Override
    public void updateBids(JSONArray bidsArray, Boolean snapshot) {
        updateOrderMap(bids, bidsArray);
    }

    @Override
    public void updateAsks(JSONArray asksArray, Boolean snapshot) {
        updateOrderMap(asks, asksArray);
    }

    private synchronized void updateOrderMap(Map<Double, Double> map, JSONArray dataArray) {
        map.clear();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject entry = dataArray.getJSONObject(i);
            map.put(Double.parseDouble(entry.getString("p")), entry.getDouble("s") * quantoMultiplier);
        }
    }
}
