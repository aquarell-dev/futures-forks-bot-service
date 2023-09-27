package org.aquarell.serializer;

import org.aquarell.forks.Fork;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class Serializer {
    public static <K, V> JSONArray serializeMap(Map<K, V> map, String keyCaption, String valueCaption) {
        JSONArray serializedMap = new JSONArray();

        map.forEach((key, value) -> {
            JSONObject iter = new JSONObject();

            iter.put(keyCaption, key);
            iter.put(valueCaption, Fork.roundToDecimalPlaces((Double) value, 2));

            serializedMap.put(iter);
        });

        return serializedMap;
    }
}
