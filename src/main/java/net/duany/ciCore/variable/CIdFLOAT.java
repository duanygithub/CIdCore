package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

public class CIdFLOAT implements Variable {
    int addr;

    public CIdFLOAT(int address) {
        addr = address;
    }

    public static CIdFLOAT createFLOAT(String str) {
        return createFLOAT(Float.parseFloat(str));
    }

    public static CIdFLOAT createFLOAT(float f) {
        int address = MemOperator.allocateMemory(4);
        MemOperator.writeFloat(address, f);
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
    public Keywords getType() {
        return Keywords.Float;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if(var.getType().equals(Keywords.Float)) {
            float value = getValue();
            return switch (op) {
                case "+" -> createFLOAT(value + (float) var.getValue());
                case "-" -> createFLOAT(value - (float) var.getValue());
                case "*" -> createFLOAT(value * (float) var.getValue());
                case "/" -> createFLOAT(value / (float) var.getValue());
                case "%" -> createFLOAT(value % (float) var.getValue());
                default -> null;
            };
        }else if(var.getType().equals(Keywords.Int)) {
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
}
