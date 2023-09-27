package org.aquarell;

import org.aquarell.bybit.BybitExchangeClient;
import org.aquarell.server.ForkWebSocketServer;
import org.aquarell.forks.ForksRegistry;
import org.aquarell.gate.GateExchangeClient;
import org.aquarell.tickers.TickersManager;

public class Main {
    public static void main(String[] args) {
        TickersManager tickersManager = new TickersManager(false);

        ForksRegistry forksRegistry = new ForksRegistry(
            tickersManager,
            new GateExchangeClient(tickersManager),
            new BybitExchangeClient(tickersManager)
        );

        ForkWebSocketServer server = new ForkWebSocketServer(forksRegistry, 8080, true);
        server.start();
        server.startUpdates();
    }
}
