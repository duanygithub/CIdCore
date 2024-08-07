package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.Keywords;

public class CIdBOOLEAN implements Variable {
    int addr;

    private CIdBOOLEAN(int address) {
        addr = address;
    }

    @Override
    protected void finalize() {
        MemOperator.freeMemory(addr, 1);
    }

    public static CIdBOOLEAN createBOOLEAN(boolean bool) {
        int address = MemOperator.allocateMemory(1);
        MemOperator.writeBoolean(address, bool);
        return new CIdBOOLEAN(address);
    }

    public static CIdBOOLEAN createWithAllocatedAddress(int address) {
        return new CIdBOOLEAN(address);
    }

    public int setValue(boolean b) {
        return MemOperator.writeBoolean(addr, b);
    }

    @Override
    public Number getValue() {
        return null;
    }

    @Override
    public Keywords getType() {
        return null;
    }

    @Override
    public int getAddress() {
        return addr;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        return null;
    }

    @Override
    public int cmp(Variable var) {
        return 0;
    }
}
