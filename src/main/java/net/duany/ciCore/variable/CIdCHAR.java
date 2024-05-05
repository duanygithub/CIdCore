package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

public class CIdCHAR implements Variable {
    int value;

    public CIdCHAR(char c) {
        value = (int) c;
    }

    public CIdCHAR(int n) {
        value = n;
    }

    public static Variable createCHAR(int n) {
        return new CIdCHAR(n);
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public Keywords getType() {
        return Keywords.Char;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if (!var.getType().equals(Keywords.Int)) return this;
        return switch (op) {
            case "+" -> new CIdCHAR(value + (int) var.getValue());
            case "-" -> new CIdCHAR(value - (int) var.getValue());
            default -> this;
        };
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
