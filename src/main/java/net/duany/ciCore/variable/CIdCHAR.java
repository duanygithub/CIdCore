package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

public class CIdCHAR implements Variable {
    int addr;

    private CIdCHAR(int address) {
        addr = address;
    }

    @Override
    protected void finalize() {
        MemOperator.freeMemory(addr, 1);
    }

    public static CIdCHAR createCHAR(int n) {
        int address = MemOperator.allocateMemory(1);
        MemOperator.writeChar(address, (char) n);
        return new CIdCHAR(address);
    }

    public static CIdCHAR createCHAR() {
        return new CIdCHAR(MemOperator.allocateMemory(1));
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
        switch (op) {
            case "+":
                return createCHAR(value + (int) var.getValue());
            case "-":
                return createCHAR(value - (int) var.getValue());
            default:
                return this;
        }
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
