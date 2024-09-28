package dev.duanyper.cidcore.symbols;

import java.util.ArrayList;
import java.util.Arrays;

public class CIdType {
    public static final CIdType Int = new CIdType();
    public static final CIdType Void = new CIdType();
    public static final CIdType Char = new CIdType();
    public static final CIdType Float = new CIdType();
    public static final CIdType Boolean = new CIdType();
    public static final CIdType Pointer = new CIdType();
    public static final CIdType Struct = new CIdType();
    public static final ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
            "int", "float", "bool", "char", "struct", "enum", "union",
            "while", "do", "for", "return", "break", "continue", "goto"
    ));

    public static CIdPointerType createPointerType(int level, CIdType type) {
        return new CIdPointerType(level, type);
    }

    public static CIdType string2Keywords(String type) {
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

    public static CIdPointerType getPointerType(String pointerTypeString) {
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
        CIdType type;
        switch (typeStr) {
            case "int" -> type = CIdType.Int;
            case "float" -> type = CIdType.Float;
            case "char" -> type = CIdType.Char;
            case "void" -> type = CIdType.Void;
            case "struct" -> type = CIdType.Struct;
            default -> type = null;
        }
        return CIdType.createPointerType(pointerLevel, type);
    }

    public static int getSize(CIdType type) {
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
