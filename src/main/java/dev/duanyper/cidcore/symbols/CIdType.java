package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.grammar.StructureDescriptor;

import java.util.ArrayList;
import java.util.Arrays;

public class CIdType {
    public static final CIdType Int = new CIdType();
    public static final CIdType Void = new CIdType();
    public static final CIdType Char = new CIdType();
    public static final CIdType Float = new CIdType();
    public static final CIdType Boolean = new CIdType();
    public static final CIdType Struct = new CIdType();
    public static final ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
            "int", "float", "bool", "char", "struct", "enum", "union",
            "while", "do", "for", "return", "break", "continue", "goto"
    ));

    public static CIdPointerType createPointerType(int level, CIdType type) {
        return new CIdPointerType(level, type);
    }

    public static CIdStructType createStructType(StructureDescriptor sd) {
        return new CIdStructType(sd);
    }

    public static CIdType string2Type(String type) {
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
                if (type.matches("(int|char|float|void|struct)\\*+")) {//int*
                    return createPointerType(type.length() - type.indexOf('*'), string2Type(type.substring(0, type.indexOf('*'))));
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
        if (type instanceof CIdPointerType) return 4;
        if (type == Char) return 1;
        if (type == Boolean) return 1;
        if (type == Void) return 0;
        return -1;
    }

    public static int getSize(String type) {
        return getSize(string2Type(type));
    }
}
