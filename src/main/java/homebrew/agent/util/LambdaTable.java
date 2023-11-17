package homebrew.agent.util;

import java.util.HashMap;
import java.util.Map;

public class LambdaTable<T> {

    private final Map<Integer, T> map;
    private final Map<T, Integer> reverseMap;

    public LambdaTable() {
        map = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    public T get(Integer integer) {
        return map.getOrDefault(integer, null);
    }

    public int put(T lambda) {
        if (reverseMap.containsKey(lambda)) {
            return reverseMap.get(lambda);
        }
        int index = map.size();
        map.put(index, lambda);
        reverseMap.put(lambda, index);
        return index;
    }

}
