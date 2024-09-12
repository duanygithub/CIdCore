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
    public static final Types Struct = new Types();
    public static final ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
            "int", "float", "bool", "char", "struct", "enum", "union",
            "while", "do", "for", "return", "break", "continue", "goto"
    ));
    public int lvl;
    public Types type;

    public Types() {
        lvl = 0;
        type = this;
    }

    //For pointers
    public Types(int lvl, Types type) {
        this.lvl = lvl;
        this.type = type;
    }

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
            case "bool" -> {
                return Boolean;
            }
            case "struct" -> {
                return Struct;
            }
            default -> {
                if (type.matches("(int|char|float|void|struct)\\*+")) {
                    return Pointer;
                } else return null;
            }
        }
    }

    public static Types getPointerTypes(String pointerTypeString) {
        int pointerLevel = 0, pointerBegin = 0;
        for (int j = 0; j < pointerTypeString.length(); j++) {
            if (pointerTypeString.charAt(j) == '*') {
                if (pointerBegin == 0) {
                    pointerBegin = j;
                }
                pointerLevel++;
            }
        }
        String typeStr = pointerTypeString.substring(0, pointerBegin);
        Types type;
        switch (typeStr) {
            case "int" -> type = Types.Int;
            case "float" -> type = Types.Float;
            case "char" -> type = Types.Char;
            case "void" -> type = Types.Void;
            case "struct" -> type = Types.Struct;
            default -> type = null;
        }
        return new Types(pointerLevel, type);
    }

    public static int getSize(Types type) {
        if (type == Int) return 4;
        if (type == Float) return 4;
        if (type == Pointer) return 4;
        if (type == Char) return 1;
        if (type == Boolean) return 1;
        if (type == Void) return 0;
        return -1;
    }

    public static int getSize(String type) {
        return getSize(string2Keywords(type));
    }
}
