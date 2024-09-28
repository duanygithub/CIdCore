package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.symbols.CIdType;

public class CIdVOID implements Variable {
    int addr;

    public static CIdVOID createVOID() {
        return new CIdVOID();
    }

    @Override
    public Number getValue() {
        return null;
    }

    @Override
    public CIdType getType() {
        return CIdType.Void;
    }

    @Override
    public int getAddress() {
        return 0;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        return null;
    }

    @Override
    public int cmp(Variable var) {
        return 0;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int sizeOf() {
        return 0;
    }
}
