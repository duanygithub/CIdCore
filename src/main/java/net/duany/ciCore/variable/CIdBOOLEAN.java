package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

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

    @Override
    public Number getValue() {
        return null;
    }

    @Override
    public Keywords getType() {
        return null;
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
