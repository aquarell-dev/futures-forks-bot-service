package org.aquarell.combination;

import java.util.ArrayList;
import java.util.List;

public class Combination {
    public static <T> List<List<T>> generateCombinations(List<T> elements) {
        List<List<T>> result = new ArrayList<>();

        for (int i = 0; i < elements.size() - 1; i++) {
            for (int j = i + 1; j < elements.size(); j++) {
                List<T> combination = new ArrayList<>();
                combination.add(elements.get(i));
                combination.add(elements.get(j));
                result.add(combination);
            }
        }

        return result;
    }
}
