package net.duany.ciCore.variable;

import net.duany.ciCore.symbols.Keywords;

public interface Variable {
    int addr = 0;
    public Number getValue();
    public Keywords getType();
    public Variable procOperation(Variable var, String op);
}
