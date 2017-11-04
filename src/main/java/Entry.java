
import java.util.Map;

public class Entry<Key, Value> implements Map.Entry<Key, Value> {

    private final Key key;
    private final Value value;

    public Entry(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

    public String toString() {
        return key + " : " + value;
    }


    public Key getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }

    public Value setValue(Value value) {
        return null;
    }


}