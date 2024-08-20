package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.symbols.Types;

public class CIdSTRUCT implements Variable {

    @Override
    public Number getValue() {
        return null;
    }

    @Override
    public Types getType() {
        return null;
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
    public int sizeOf() {
        return 0;
    }
}
