package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.Patterns;
import dev.duanyper.cidcore.variable.Variable;

import java.util.HashMap;
import java.util.Map;

import static dev.duanyper.cidcore.Patterns.*;

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
    public static final int DECLARE_POINTER = 9;
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
        if (isMatch(str, SIGNED_NUMBER) || isMatch(str, HEX_NUMBER)) {
            return INTEGER;
        }
        if (isMatch(str, FLOAT_NUMBER)) {
            return FLOAT;
        }
        if (isMatch(str, COMMA_OR_SEMICOLON)) {
            return SPLITPOINT;
        }
        if (isMatch(str, Patterns.DECLARE_POINTER)) {
            return TypeLookup.DECLARE_POINTER;
        }
        if (isMatch(str, BASIC_TYPE)) {
            return BASICTYPE;
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
        if (isMatch(str, Patterns.STRING)) {
            return STRING;
        }
        if (isMatch(str, Patterns.PROC_CONTROL)) {
            return PROC_CONTROL;
        }
        if (isMatch(str, Patterns.BOOLEAN)) {
            return BOOLEAN;
        }
        if (functions.funcList.get(str) != null) {
            return FUNCTION;
        }
        if (isMatch(str, IDENTIFIER)) {
            return VARIABLE_FORMAT;
        }
        return -1;
    }

    public static CIdType lookupKeywords(String str, Variables tmpVars, Functions functions) {
        Map<String, Variable> vars;
        if (tmpVars == null) {
            vars = new HashMap<>();
        } else vars = new HashMap<>(tmpVars);
        if (isMatch(str, SIGNED_NUMBER)) {
            return CIdType.Int;
        }
        if (isMatch(str, FLOAT_NUMBER)) {
            return CIdType.Float;
        }
        if (functions.funcList.getOrDefault(str, null) != null) {
            functions.funcList.get(str);
        }
        if (vars.get(str) != null) {
            return vars.get(str).getType();
        }
        if (functions.funcList.get(str) != null) {
            return functions.funcList.get(str);
        }
        return null;
    }
}
