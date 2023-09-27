package org.aquarell.exchanges;

import java.net.URI;

public enum Exchange {
//    KUCOIN ("KuCoin", "wss://api.gateio.ws/ws/v4/", 0.12),
    BYBIT ("Bybit", "wss://stream.bybit.com/v5/public/linear", 0.1),
    GATE ("Gate.io", "wss://fx-ws.gateio.ws/v4/ws/usdt", 0.1);

    public final String exchangeTitle;
    public final URI connectionUri;
    public final double fee;

    /**
     * @param exchangeTitle
     * @param connectionUri Websocket connection uri
     * @param fee Total fee(enter and exit) for market order in percents
     */
    private Exchange(String exchangeTitle, String connectionUri, double fee) {
        this.exchangeTitle = exchangeTitle;
        this.connectionUri = URI.create(connectionUri);
        this.fee = fee;
    }
}
