package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdType;

public class CIdBOOLEAN implements Variable {
    int addr;

    private CIdBOOLEAN(int address) {
        addr = address;
    }

    @Override
    protected void finalize() {
        MemOperator.freeMemory(addr, 1);
    }

    public static CIdBOOLEAN createBOOLEAN(boolean bool) throws CIdRuntimeException {
        int address = MemOperator.allocateMemory(1);
        MemOperator.writeBoolean(address, bool);
        return new CIdBOOLEAN(address);
    }

    public static CIdBOOLEAN createWithAllocatedAddress(int address) {
        return new CIdBOOLEAN(address);
    }

    public int setValue(boolean b) throws CIdRuntimeException {
        return MemOperator.writeBoolean(addr, b);
    }

    @Override
    public Number getValue() throws CIdRuntimeException {
        return MemOperator.readBoolean(addr) ? 1 : 0;
    }

    @Override
    public CIdType getType() {
        return CIdType.Boolean;
    }

    @Override
    public int getAddress() {
        return addr;
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
        return 1;
    }

    @Override
    public String toString() {
        try {
            return getValue().intValue() == 0 ? "false" : "true";
        } catch (CIdRuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
