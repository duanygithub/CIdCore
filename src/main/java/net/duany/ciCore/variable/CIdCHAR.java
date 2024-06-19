package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

public class CIdCHAR implements Variable {
    int addr;

    public CIdCHAR(int address) {
        addr = address;
    }

    public static Variable createCHAR(int n) {
        int address = MemOperator.allocateMemory(1);
        MemOperator.writeChar(address, (char) n);
        return new CIdCHAR(address);
    }

    @Override
    public Integer getValue() {
        return (int) MemOperator.readChar(addr);
    }

    @Override
    public Keywords getType() {
        return Keywords.Char;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if (!var.getType().equals(Keywords.Int)) return this;
        int value = getValue();
        return switch (op) {
            case "+" -> createCHAR(value + (int) var.getValue());
            case "-" -> createCHAR(value - (int) var.getValue());
            default -> this;
        };
    }

    @Override
    public int cmp(Variable var) {
        int value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    @Override
    public String toString() {
        return String.valueOf(MemOperator.readChar(addr));

    }
}
