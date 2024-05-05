package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

public class CIdFLOAT implements Variable{
    float value;
    public CIdFLOAT(float f) {
        value = f;
    }
    public static Variable createFLOAT(String str) {
        return new CIdFLOAT(Float.parseFloat(str));
    }
    public static Variable createFLOAT(float f) {
        return new CIdFLOAT(f);
    }
    public  void setValue(float f) {
        value = f;
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public Keywords getType() {
        return Keywords.Float;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if(var.getType().equals(Keywords.Float)) {
            return switch (op) {
                case "+" -> new CIdFLOAT(value + (float) var.getValue());
                case "-" -> new CIdFLOAT(value - (float) var.getValue());
                case "*" -> new CIdFLOAT(value * (float) var.getValue());
                case "/" -> new CIdFLOAT(value / (float) var.getValue());
                case "%" -> new CIdFLOAT(value % (float) var.getValue());
                default -> null;
            };
        }else if(var.getType().equals(Keywords.Int)) {
            return switch (op) {
                case "+" -> new CIdFLOAT(value + (int) var.getValue());
                case "-" -> new CIdFLOAT(value - (int) var.getValue());
                case "*" -> new CIdFLOAT(value * (int) var.getValue());
                case "/" -> new CIdFLOAT(value / (int) var.getValue());
                case "%" -> new CIdFLOAT(value % (int) var.getValue());
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
