package org.aquarell.tickers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class TickersManager {
    public final boolean test;
    private final String TICKERS_PATH = "/tickers.json";
    private final JSONObject coins;

    public TickersManager(boolean test) {
        this.test = test;
        this.coins = new JSONObject(readJson());
    }

    public List<String> getCoinsByKey(String key) {
        return (test ? coins.getJSONArray("test") : coins.getJSONArray(key)).toList()
            .stream()
            .map(Object::toString)
            .toList();
    }

    private String readJson() {
        try (InputStream inputStream = TickersManager.class.getResourceAsStream(TICKERS_PATH)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + TICKERS_PATH);
            }

            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                return scanner.useDelimiter("\\A").next();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
