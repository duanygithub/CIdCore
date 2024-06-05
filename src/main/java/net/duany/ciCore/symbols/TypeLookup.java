package net.duany.ciCore.symbols;

public class TypeLookup {
    public static final int FUNCTION = 0;
    public static final int VARIABLE = 1;
    public static final int BASICTYPE = 2;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int SPLITPOINT = 5;

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
        if (str.equals("int") || str.equals("char") || str.equals("long") || str.equals("float")) {
            return BASICTYPE;
        }
        if (Functions.funcList.getOrDefault(str, null) != null) {
            return FUNCTION;
        }
        if (Variables.vars.getOrDefault(str, null) != null) {
            return VARIABLE;
        }
        return -1;
    }
}
