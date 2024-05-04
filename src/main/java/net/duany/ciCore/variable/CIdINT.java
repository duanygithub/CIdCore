package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

public class CIdINT implements Variable{
    int value;
    public CIdINT(int n) {
        value = n;

    }
    public Variable createINT(String str) {
        return new CIdINT(Integer.parseInt(str));
    }
    public Variable createINT(int n) {
        return new CIdINT(n);
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
}
