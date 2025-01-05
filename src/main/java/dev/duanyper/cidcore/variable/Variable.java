package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.symbols.CIdType;

public interface Variable {
    static Variable createWithAllocatedAddress(int address) {
        return null;
    }

    Number getValue() throws CIdRuntimeException;

    CIdType getType();

    long getAddress();

    Variable procOperation(Variable var, String op) throws CIdRuntimeException;

    int cmp(Variable var) throws CIdRuntimeException;
    String toString();

    int sizeOf();
}
