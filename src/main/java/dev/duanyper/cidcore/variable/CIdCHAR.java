package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.Keywords;

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

    public static CIdCHAR createWithAllocatedAddress(int address) {
        return new CIdCHAR(address);
    }

    public int setValue(char c) {
        return MemOperator.writeChar(addr, c);
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
    public int getAddress() {
        return addr;
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
