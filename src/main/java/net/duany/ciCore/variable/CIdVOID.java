package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

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
    public Keywords getType() {
        return Keywords.Void;
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
