package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

public class CIdINT implements Variable {
    int addr;

    private CIdINT(int address) {
        addr = address;
    }

    @Override
    protected void finalize() {
        MemOperator.freeMemory(addr, 4);
    }

    public static CIdINT createINT(String str) {
        return createINT(Integer.parseInt(str));
    }

    public static CIdINT createINT(int n) {
        int address = MemOperator.allocateMemory(4);
        MemOperator.writeInt(address, n);
        return new CIdINT(address);
    }

    public int setValue(int n) {
        MemOperator.writeInt(addr, n);
        return n;
    }

    @Override
    public Integer getValue() {
        return MemOperator.readInt(addr);
    }

    @Override
    public Keywords getType() {
        return Keywords.Int;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        if(var.getType().equals(Keywords.Int)) {
            int value = getValue();
            return switch (op) {
                case "+" -> createINT(value + var.getValue().intValue());
                case "-" -> createINT(value - var.getValue().intValue());
                case "*" -> createINT(value * var.getValue().intValue());
                case "/" -> CIdFLOAT.createFLOAT((float) value / var.getValue().intValue());
                case "%" -> createINT(value % var.getValue().intValue());
                case ">>" -> createINT(value >> var.getValue().intValue());
                case "<<" -> createINT(value << var.getValue().intValue());
                case "&" -> createINT(value & var.getValue().intValue());
                case "|" -> createINT(value | var.getValue().intValue());
                case "~" -> createINT(~value);
                case "!" -> createINT(value == 0 ? 1 : 0);
                case "^" -> createINT(value ^ var.getValue().intValue());

                default -> null;
            };
        } else if(var.getType().equals(Keywords.Float)) {
            int value = getValue();
            return switch (op) {
                case "+" -> CIdFLOAT.createFLOAT(value + var.getValue().floatValue());
                case "-" -> CIdFLOAT.createFLOAT(value - var.getValue().floatValue());
                case "*" -> CIdFLOAT.createFLOAT(value * var.getValue().floatValue());
                case "/" -> CIdFLOAT.createFLOAT(value / var.getValue().floatValue());
                case "%" -> CIdFLOAT.createFLOAT(value % var.getValue().floatValue());
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) {
        int value = getValue();
        float val = (float) var.getValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }
}
