package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

public class CIdINT implements Variable{
    int value;
    public CIdINT(int n) {
        value = n;
    }
    public static Variable createINT(String str) {
        return new CIdINT(Integer.parseInt(str));
    }
    public static Variable createINT(int n) {
        return new CIdINT(n);
    }
    public int setValue(int n) {
        value = n;
        return n;
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public Keywords getType() {
        return Keywords.Int;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if(var.getType().equals(Keywords.Int)) {
            return switch (op) {
                case "+" -> new CIdINT(value + (int) var.getValue());
                case "-" -> new CIdINT(value - (int) var.getValue());
                case "*" -> new CIdINT(value * (int) var.getValue());
                case "/" -> new CIdFLOAT((float) value / (int) var.getValue());
                case "%" -> new CIdINT(value % (int) var.getValue());
                case ">>" -> new CIdINT(value >> (int) var.getValue());
                case "<<" -> new CIdINT(value << (int) var.getValue());
                case "&" -> new CIdINT(value & (int) var.getValue());
                case "|" -> new CIdINT(value | (int) var.getValue());
                case "~" -> new CIdINT(~value);
                case "!" -> new CIdINT(value == 0 ? 1 : 0);
                case "^" -> new CIdINT(value ^ (int) var.getValue());

                default -> null;
            };
        } else if(var.getType().equals(Keywords.Float)) {
            return switch (op) {
                case "+" -> new CIdFLOAT(value + (float) var.getValue());
                case "-" -> new CIdFLOAT(value - (float) var.getValue());
                case "*" -> new CIdFLOAT(value * (float) var.getValue());
                case "/" -> new CIdFLOAT(value / (float) var.getValue());
                case "%" -> new CIdFLOAT(value % (float) var.getValue());
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) {
        float val = (float) var.getValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }
}
