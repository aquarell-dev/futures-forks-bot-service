package org.aquarell.forks;

import org.aquarell.orderbook.NotEnoughVolumeInOrderBook;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.pair.Pair;
import org.aquarell.serializer.Serializer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Fork {
    public final String coin;
    public final Direction direction;
    public final Pair<OrderBook> orderBooks;
    private final double SPREAD_THRESHOLD = 0;
    private final int[] volumes = new int[] { 100, 200, 300, 500, 700, 1000 };
    private final Map<Integer, Double> spread = new TreeMap<>();
    private boolean active;

    public Fork(String coin, Pair<OrderBook> orderBooks, Direction direction) {
        this.coin = coin;
        this.orderBooks = orderBooks;
        this.direction = direction;
        updateSpreadMap();
    }

    public void updateOrderBook(OrderBook orderBook) {
        if (orderBooks.getFirst().exchange == orderBook.exchange) {
            orderBooks.setFirst(orderBook);
        }

        if (orderBooks.getSecond().exchange == orderBook.exchange) {
            orderBooks.setSecond(orderBook);
        }

        updateSpreadMap();
    }

    public JSONObject toJson() {
        JSONObject fork = new JSONObject();

        fork.put("coin", coin);
        fork.put("direction", direction);
        fork.put("active", active);
        fork.put("spreads", Serializer.serializeMap(spread, "volume", "spread"));

        JSONObject orderBooks = new JSONObject();

        orderBooks.put("first", this.orderBooks.getFirst().toJson());
        orderBooks.put("second", this.orderBooks.getSecond().toJson());

        fork.put("orderBooks", orderBooks);

        return fork;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s)", orderBooks.getFirst().exchange.exchangeTitle, orderBooks.getSecond().exchange.exchangeTitle, direction.caption);
    }

    private void updateSpreadMap() {
        OrderBook firstOrderBook = orderBooks.getFirst();
        OrderBook secondOrderBook = orderBooks.getSecond();

        double fee = firstOrderBook.exchange.fee + secondOrderBook.exchange.fee;

        Arrays.stream(volumes)
            .forEach(volume -> {
                try {
                    spread.put(volume, direction == Direction.SHORT_LONG
                        ? calculateSpread(firstOrderBook.calculateSellAveragePrice(volume), secondOrderBook.calculateBuyAveragePrice(volume)) - fee
                        : calculateSpread(secondOrderBook.calculateSellAveragePrice(volume), firstOrderBook.calculateBuyAveragePrice(volume)) - fee);
                } catch (NotEnoughVolumeInOrderBook e) {
                    spread.put(volume, null);
                }
            });

        active = spread.values().stream().anyMatch(spread -> spread != null && spread >= SPREAD_THRESHOLD);
    }

    public boolean isActive() {
        return active;
    }

    public Map<Integer, Double> getSpread() {
        return spread.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> (entry.getValue() != null) ? roundToDecimalPlaces(entry.getValue(), 2) : Double.NaN
            )
        );
    }

    private Double calculateSpread(double higherPrice, double lowerPrice) {
        return (higherPrice / lowerPrice - 1) * 100;
    }

    public static Double roundToDecimalPlaces(Double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
}
