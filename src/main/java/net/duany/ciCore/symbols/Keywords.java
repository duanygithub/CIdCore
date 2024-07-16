package net.duany.ciCore.symbols;

import net.duany.ciCore.variable.Variable;

import java.util.*;

public class Keywords {
    public static final Keywords Int = new Keywords();
    public static final Keywords Void = new Keywords();
    public static final Keywords Char = new Keywords();
    public static final Keywords Float = new Keywords();
    public static final Keywords Boolean = new Keywords();
    public static final Keywords Pointer = new Keywords();
    public static final Keywords CharArray = new Keywords();

    public static final ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
            "int", "float", "bool", "char",
            "while", "do", "for", "return", "break", "continue", "goto"
    ));

    public static Keywords string2Keywords(String type) {
        switch (type) {
            case "int" -> {
                return Int;
            }
            case "char" -> {
                return Char;
            }
            case "Float" -> {
                return Float;
            }
            default -> {
                if (type.matches("(int|char|float)\\*+")) {
                    return Pointer;
                } else return null;
            }
        }
    }

}
