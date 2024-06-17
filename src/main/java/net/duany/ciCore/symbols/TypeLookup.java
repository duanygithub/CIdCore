package net.duany.ciCore.symbols;

public class TypeLookup {
    public static final int FUNCTION = 0;
    public static final int VARIABLE = 1;
    public static final int BASICTYPE = 2;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int SPLITPOINT = 5;
    public static final int POINTER = 6;
    public static final int STRING = 7;

    public static int lookup(String str) {
        if (str.matches("[0-9]+")) {
            return INTEGER;
        }
        if (str.matches("^([0-9]+[.][0-9]*)$")) {
            return FLOAT;
        }
        if (str.matches("[,;)}]")) {
            return SPLITPOINT;
        }
        if (str.matches("(int|char|float)\\*+") || str.matches("int|char|float")) {
            if (str.matches("(int|char|float)\\*+")) {
                return POINTER;
            } else return BASICTYPE;
        }
        if (Functions.funcList.getOrDefault(str, null) != null) {
            return FUNCTION;
        }
        if (str.matches("\\w+")) {
            return VARIABLE;
        }
        if (str.matches("\"([^\"]*)\"")) {
            return STRING;
        }
        return -1;
    }
}
