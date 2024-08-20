package dev.duanyper.cidcore.symbols;

import java.util.ArrayList;
import java.util.Arrays;

public class Types {
    public static final Types Int = new Types();
    public static final Types Void = new Types();
    public static final Types Char = new Types();
    public static final Types Float = new Types();
    public static final Types Boolean = new Types();
    public static final Types Pointer = new Types();
    public static final Types CharArray = new Types();
    public static final Types Struct = new Types();

    public static final ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
            "int", "float", "bool", "char", "struct", "enum", "union",
            "while", "do", "for", "return", "break", "continue", "goto"
    ));

    public static Types string2Keywords(String type) {
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
