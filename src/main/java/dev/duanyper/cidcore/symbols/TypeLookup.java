package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.variable.Variable;

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
    public static final int PROC_CONTROL = 12;
    public static final int BOOLEAN = 13;
    public static final int BLOCK_START = 14;
    public static final int STRUCT = 15;

    public static int lookup(String str, Variables vars, Functions functions) {
        if (str.equals("{")) {
            return BLOCK_START;
        }
        if (str.matches("(\\+|-)?[0-9]+")) {
            return INTEGER;
        }
        if (str.matches("^([0-9]+[.][0-9]*)$")) {
            return FLOAT;
        }
        if (str.matches("[,;]")) {
            return SPLITPOINT;
        }
        if (str.matches("(int|char|float)\\*+") || str.matches("int|char|float")) {
            if (str.matches("(int|char|float)\\*+")) {
                return DECLEAR_POINTER;
            } else return BASICTYPE;
        }
        if (str.equals("struct")) {
            return STRUCT;
        }
        if (functions.funcList.getOrDefault(str, null) != null) {
            return FUNCTION;
        }
        try {
            if (vars.get(str) != null) {
                return VARIABLE;
            }
        } catch (NullPointerException ignore) {
        }
        if (str.equals("return")) {
            return RETURN;
        }
        if (str.matches("\"([^\"]*)\"")) {
            return STRING;
        }
        if (str.matches("(for)|(while)|(if)|(do)|(goto)")) {
            return PROC_CONTROL;
        }
        if (str.matches("(true)|(false)")) {
            return BOOLEAN;
        }
        if (functions.funcList.get(str) != null) {
            return FUNCTION;
        }
        if (str.matches("\\w+")) {
            return VARIABLE_FORMAT;
        }
        return -1;
    }

    public static CIdType lookupKeywords(String str, Variables tmpVars, Functions functions) {
        Map<String, Variable> vars;
        if (tmpVars == null) {
            vars = new HashMap<>();
        } else vars = new HashMap<>(tmpVars);
        if (str.matches("[0-9]+")) {
            return CIdType.Int;
        }
        if (str.matches("^([0-9]+[.][0-9]*)$")) {
            return CIdType.Float;
        }
        if (functions.funcList.getOrDefault(str, null) != null) {
            functions.funcList.get(str);
        }
        if (vars.get(str) != null) {
            return vars.get(str).getType();
        }
        if (str.matches("\"([^\"]*)\"")) {
            return CIdType.Pointer;
        }
        if (functions.funcList.get(str) != null) {
            return functions.funcList.get(str);
        }
        return null;
    }
}
