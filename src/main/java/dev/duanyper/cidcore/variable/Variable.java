package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.symbols.Keywords;

public interface Variable {
    Number getValue();
    Keywords getType();
    int getAddress();
    Variable procOperation(Variable var, String op);
    int cmp(Variable var);
    String toString();
}
