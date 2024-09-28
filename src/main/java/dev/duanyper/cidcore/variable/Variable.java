package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.symbols.CIdType;

public interface Variable {
    static Variable createWithAllocatedAddress(int address) {
        return null;
    }

    Number getValue();

    CIdType getType();
    int getAddress();
    Variable procOperation(Variable var, String op);
    int cmp(Variable var);
    String toString();

    int sizeOf();
}
