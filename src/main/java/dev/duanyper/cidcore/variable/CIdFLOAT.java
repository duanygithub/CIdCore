package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.Types;

public class CIdFLOAT implements Variable {
    int addr;

    private CIdFLOAT(int address) {
        addr = address;
    }

    @Override
    protected void finalize() {
        MemOperator.freeMemory(addr, 4);
    }

    public static CIdFLOAT createFLOAT(String str) {
        return createFLOAT(Float.parseFloat(str));
    }

    public static CIdFLOAT createFLOAT(float f) {
        int address = MemOperator.allocateMemory(4);
        MemOperator.writeFloat(address, f);
        return new CIdFLOAT(address);
    }

    public static CIdFLOAT createFLOAT() {
        return new CIdFLOAT(MemOperator.allocateMemory(4));
    }

    public static CIdFLOAT createWithAllocatedAddress(int address) {
        return new CIdFLOAT(address);
    }

    public void setValue(float f) {
        MemOperator.writeFloat(addr, f);
    }

    @Override
    public Float getValue() {
        return MemOperator.readFloat(addr);
    }

    @Override
    public Types getType() {
        return Types.Float;
    }

    @Override
    public int getAddress() {
        return addr;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if (var.getType().equals(Types.Float)) {
            float value = getValue();
            return switch (op) {
                case "+" -> createFLOAT(value + (float) var.getValue());
                case "-" -> createFLOAT(value - (float) var.getValue());
                case "*" -> createFLOAT(value * (float) var.getValue());
                case "/" -> createFLOAT(value / (float) var.getValue());
                case "%" -> createFLOAT(value % (float) var.getValue());
                default -> null;
            };
        } else if (var.getType().equals(Types.Int)) {
            float value = getValue();
            return switch (op) {
                case "+" -> createFLOAT(value + (int) var.getValue());
                case "-" -> createFLOAT(value - (int) var.getValue());
                case "*" -> createFLOAT(value * (int) var.getValue());
                case "/" -> createFLOAT(value / (int) var.getValue());
                case "%" -> createFLOAT(value % (int) var.getValue());
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) {
        float value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    @Override
    public String toString() {
        return ((Float) MemOperator.readFloat(addr)).toString();
    }

    @Override
    public int sizeOf() {
        return 4;
    }
}
