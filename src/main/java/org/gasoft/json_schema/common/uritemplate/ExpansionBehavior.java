package org.gasoft.json_schema.common.uritemplate;

public class ExpansionBehavior {

    private final Character first;

    private final char separator;

    private final boolean named;

    private final Character empty;

    private final boolean allowReserved;

    public static ExpansionBehavior expand() {
        return new ExpansionBehavior(null, ',', false, null, false);
    }

    public static ExpansionBehavior expandAs(Character first) {
        return new ExpansionBehavior(first, first, false, null, false);
    }

    private ExpansionBehavior(Character first, char separator, boolean named, Character empty, boolean allowReserved) {
        this.first = first;
        this.separator = separator;
        this.named = named;
        this.empty = empty;
        this.allowReserved = allowReserved;
    }

    public ExpansionBehavior separator(char separator) {
        return new ExpansionBehavior(first, separator, named, empty, allowReserved);
    }

    public ExpansionBehavior ifEmptyExplodeWith(char empty) {
        return new ExpansionBehavior(first, separator, named, empty, allowReserved);
    }

    public ExpansionBehavior named() {
        return new ExpansionBehavior(first, separator, true, empty, allowReserved);
    }

    public ExpansionBehavior allowReserved() {
        return new ExpansionBehavior(first, separator, named, empty, true);
    }
}