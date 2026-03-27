package io.github.lennyslounge.aureum.writer;

import java.util.HashMap;
import java.util.Map;

public class OccurrenceState {

    private final Map<String, Integer> nextNumberForOccurrence = new HashMap<>();
    private final Map<String, Integer> numberForValue = new HashMap<>();

    public String getOccurrence(String occurrenceKey, String value) {
        String valueKey = occurrenceKey + value;
        Integer number = numberForValue.get(valueKey);
        if (number == null) {
            number = nextNumberForOccurrence.merge(occurrenceKey, 1, Integer::sum);
            numberForValue.put(valueKey, number);
        }
        return occurrenceKey + "_" + number;
    }
}
