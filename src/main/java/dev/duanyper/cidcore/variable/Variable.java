package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.symbols.Types;

public interface Variable {
    static Variable createWithAllocatedAddress(int address) {
        return null;
    }

    Number getValue();

    Types getType();
    int getAddress();
    Variable procOperation(Variable var, String op);
    int cmp(Variable var);
    String toString();

    int sizeOf();
}
