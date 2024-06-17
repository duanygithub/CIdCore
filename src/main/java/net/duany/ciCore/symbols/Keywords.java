package net.duany.ciCore.symbols;

public class Keywords {
    public static final Keywords Int = new Keywords();
    public static final Keywords Char = new Keywords();
    public static final Keywords Float = new Keywords();
    public static final Keywords Pointer = new Keywords();

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
