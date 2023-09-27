package org.aquarell.forks;

import org.aquarell.combination.Combination;
import org.aquarell.exchanges.ExchangeClient;
import org.aquarell.orderbook.OrderBook;
import org.aquarell.pair.Pair;
import org.aquarell.tickers.TickersManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ForksRegistry {
    protected final Logger logger = LoggerFactory.getLogger(ForksRegistry.class);
    private final List<String> coins;
    private final Map<String, List<Fork>> forks = new ConcurrentHashMap<>();
    private final List<ExchangeClient> clients;

    public ForksRegistry(TickersManager manager, ExchangeClient... clients) {
        this.coins = manager.getCoinsByKey("common");
        this.clients = List.of(clients);
    }

    public Map<String, List<Fork>> getForks() {
        return forks;
    }

    public void start() {
        runClients();
        ensureOrderBooksFilled();
        initializeForks();
        updateForks();
    }

    private void runClients() {
        clients.stream().map(Thread::new).forEach(Thread::start);
    }

    private void ensureOrderBooksFilled() { // TODO dodge this step, make it so if ob is empty to skip iterations (busd, ray uncommented)
        boolean filled = false;

        while (!filled) {
            filled = clients.stream().allMatch(ExchangeClient::areOrderBooksFilled);

            clients.forEach(client -> {
                List<String> unfilledOrderbooks = client.getUnfilledOrderBooks();
                if (!unfilledOrderbooks.isEmpty()) System.out.println(client.exchange + ": " + unfilledOrderbooks);
            });

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ignored) {}
        }

        logger.info("All orderbooks are filled");
    }

    private void initializeForks() {
        coins.forEach(coin -> {
            if (!forks.containsKey(coin)) {
                List<OrderBook> orderBooks = clients.stream()
                    .map(client -> client.getOrderBookByCoin(coin))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!orderBooks.isEmpty()) {
                    List<Fork> currentForks = createForks(coin, orderBooks);
                    forks.put(coin, currentForks);
                }
            }
        });

        logger.info("Forks hashmap has been initialized");
    }

    private List<Fork> createForks(String coin, List<OrderBook> orderBooks) {
        List<List<OrderBook>> orderBooksCombinations = Combination.generateCombinations(orderBooks);

        return orderBooksCombinations.stream()
            .flatMap(orderBookList -> generateBothSidesFork(coin, orderBookList).stream())
            .collect(Collectors.toList());
    }

    private List<Fork> generateBothSidesFork(String coin, List<OrderBook> orderBooks) {
        return List.of(new Fork(coin, new Pair<>(orderBooks.get(0), orderBooks.get(1)), Direction.LONG_SHORT), new Fork(coin, new Pair<>(orderBooks.get(0), orderBooks.get(1)), Direction.SHORT_LONG));
    }

    private void updateForks() {
        Thread thread = new Thread(() -> {
            while (true) {
                forks.forEach((coin, forkList) -> forkList.forEach(fork -> clients.forEach(client -> {
                    OrderBook orderBook = client.getOrderBookByCoin(coin);
                    if (orderBook != null) fork.updateOrderBook(orderBook);
                })));
            }
        });

        thread.start();
    }
}
