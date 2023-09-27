package org.aquarell.forks;

import org.aquarell.bybit.BybitOrderBook;
import org.aquarell.exchanges.Exchange;
import org.aquarell.gate.GateOrderBook;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.orderbook.OrderBookHelper;
import org.aquarell.pair.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// we don't need to test forks with all possible orderbook implementations
// we just gotta test all the orderbook implementations
// and that'd make sure that everything is safe
public class ForkTest {
    private Pair<OrderBook> orderBooks;

    @BeforeEach
    public void setUp() {
        GateOrderBook gateOrderBook = new GateOrderBook("btc", 1d);
        BybitOrderBook bybitOrderBook = new BybitOrderBook("btc");

        gateOrderBook.updateAsks(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.GATE,
                new double[][] {
                    new double[] { 100, 1.7 },
                    new double[] { 400, 1.6 },
                    new double[] { 300, 1.5 },
                    new double[] { 200, 1.4 },
                }
            ),
            false
        );
        gateOrderBook.updateBids(OrderBookHelper.getOrderBookQuotes(
                Exchange.GATE,
                new double[][] {
                    new double[] { 220, 1.3 },
                    new double[] { 300, 1.2 },
                    new double[] { 550, 1.1 },
                    new double[] { 600, 1 },
                }
            ),
            false
        );

        bybitOrderBook.updateAsks(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.BYBIT,
                new double[][] {
                    new double[] { 500, 1.4 },
                    new double[] { 200, 1.3 },
                    new double[] { 150, 1.2 },
                    new double[] { 210, 1.1 },
                }
            ),
            true
        );
        bybitOrderBook.updateBids(
            OrderBookHelper.getOrderBookQuotes(
                Exchange.BYBIT,
                new double[][] {
                    new double[] { 220, 1 },
                    new double[] { 250, 0.9 },
                    new double[] { 400, 0.8 },
                    new double[] { 600, 0.7 },
                }
            ),
            true
        );

        orderBooks = new Pair<>(gateOrderBook, bybitOrderBook);
    }

    private Fork createFork(Direction direction) {
        return new Fork("btc", orderBooks, direction);
    }

    @Test
    public void testCalculateLongShortSpread() {
        Map<Integer, Double> spreadMap = new HashMap<>() {{
            put(100, -28.77);
            put(200, -28.77);
            put(300, -31.14);
        }};

        Map<Integer, Double> forkSpread = createFork(Direction.LONG_SHORT).getSpread();

        spreadMap.forEach((volume, spread) -> assertEquals(spread, forkSpread.get(volume)));
    }

    @Test
    public void testCalculateShortLongSpread() {
        Map<Integer, Double> spreadMap = new HashMap<>() {{
            put(100, 17.98);
            put(200, 17.98);
            put(300, 15.27);
        }};

        Map<Integer, Double> forkSpread = createFork(Direction.SHORT_LONG).getSpread();

        spreadMap.forEach((volume, spread) -> assertEquals(spread, forkSpread.get(volume)));
    }

    @Test
    public void testForkIsActive() {
        assertFalse(createFork(Direction.LONG_SHORT).isActive());
        assertTrue(createFork(Direction.SHORT_LONG).isActive());
    }
}
