package org.aquarell.permutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Permutation {
    public static <T> List<List<T>> generatePermutations(List<T> elements) {
        List<List<T>> result = new ArrayList<>();
        permute(elements, 0, result);
        return result;
    }

    private static <T> void permute(List<T> elements, int start, List<List<T>> result) {
        if (start == elements.size() - 1) {
            result.add(new ArrayList<>(elements));
            return;
        }

        for (int i = start; i < elements.size(); i++) {
            Collections.swap(elements, start, i);
            permute(elements, start + 1, result);
            Collections.swap(elements, start, i);
        }
    }
}
