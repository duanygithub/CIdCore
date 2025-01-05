package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdType;

public class CIdCHAR implements Variable {
    final long addr;

    private CIdCHAR(long address) {
        addr = address;
    }

    public static CIdCHAR createCHAR(int n) throws CIdRuntimeException {
        long address = MemOperator.allocateMemory(1);
        MemOperator.writeChar(address, (char) n);
        return new CIdCHAR(address);
    }

    public static CIdCHAR createCHAR() {
        return new CIdCHAR(MemOperator.allocateMemory(1));
    }

    public static CIdCHAR createWithAllocatedAddress(long address) {
        return new CIdCHAR(address);
    }

    public int setValue(char c) throws CIdRuntimeException {
        return MemOperator.writeChar(addr, c);
    }

    @Override
    public Integer getValue() throws CIdRuntimeException {
        return (int) MemOperator.readChar(addr);
    }

    @Override
    public CIdType getType() {
        return CIdType.Char;
    }

    @Override
    public long getAddress() {
        return addr;
    }

    @Override
    public Variable procOperation(Variable var, String op) throws CIdRuntimeException {
        if (!var.getType().equals(CIdType.Int)) return this;
        int value = getValue();
        return switch (op) {
            case "+" -> createCHAR(value + (int) var.getValue());
            case "-" -> createCHAR(value - (int) var.getValue());
            default -> this;
        };
    }

    @Override
    public int cmp(Variable var) throws CIdRuntimeException {
        int value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    @Override
    public String toString() {
        try {
            return String.valueOf(MemOperator.readChar(addr));
        } catch (CIdRuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int sizeOf() {
        return 1;
    }
}
