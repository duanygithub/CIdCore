package net.duany.ciCore.symbols;

import net.duany.ciCore.variable.Variable;

import java.util.HashMap;
import java.util.Map;

public class TypeLookup {
    public static final int FUNCTION = 0;
    public static final int VARIABLE = 1;
    public static final int BASICTYPE = 2;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int SPLITPOINT = 5;
    public static final int POINTER = 6;
    public static final int STRING = 7;
    public static final int FUNCTION_CALL = 8;
    public static final int DECLEAR_POINTER = 9;
    public static final int VARIABLE_FORMAT = 10;
    public static final int RETURN = 11;

    public static int lookup(String str, Variables vars) {
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
                return DECLEAR_POINTER;
            } else return BASICTYPE;
        }
        if (Functions.funcList.getOrDefault(str, null) != null) {
            return FUNCTION;
        }
        if (vars.vars.get(str) != null) {
            return VARIABLE;
        }
        if (str.equals("return")) {
            return RETURN;
        }
        if (str.matches("\"([^\"]*)\"")) {
            return STRING;
        }
        if (str.matches("__cidfunc_\\w+_l[0-9]+r[0-9]+__")) {
            return FUNCTION_CALL;
        }
        if (str.matches("\\w+")) {
            return VARIABLE_FORMAT;
        }
        return -1;
    }

    public static Keywords lookupKeywords(String str, Variables tmpVars) {
        Map<String, Variable> vars;
        if (tmpVars == null) {
            vars = new HashMap<>();
        } else vars = new HashMap<>(tmpVars.vars);
        if (str.matches("[0-9]+")) {
            return Keywords.Int;
        }
        if (str.matches("^([0-9]+[.][0-9]*)$")) {
            return Keywords.Float;
        }
        if (Functions.funcList.getOrDefault(str, null) != null) {
            Functions.funcList.get(str);
        }
        if (vars.get(str) != null) {
            return vars.get(str).getType();
        }
        if (str.matches("\"([^\"]*)\"")) {
            return Keywords.Pointer;
        }
        if (Functions.funcList.get(str) != null) {
            return Functions.funcList.get(str);
        }
        return null;
    }
}
