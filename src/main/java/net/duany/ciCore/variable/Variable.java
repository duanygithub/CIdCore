package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

public interface Variable {
    Number getValue();
    Keywords getType();
    Variable procOperation(Variable var, String op);
    int cmp(Variable var);

    String toString();
}
