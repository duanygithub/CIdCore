package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

public class CIdPOINTER implements Variable {
    int addr;
    int level = 0;
    Keywords targetType;

    protected CIdPOINTER(int address, int lvl, Keywords type) {
        addr = address;
        level = lvl;
    }

    public static CIdPOINTER createPOINTER(int lvl, int pAddress, Keywords type) {
        int address = MemOperator.allocateMemory(4);
        MemOperator.writeInt(address, pAddress);
        return new CIdPOINTER(address, lvl, type);
    }

    public int setValue(int address) {
        return MemOperator.writeInt(addr, address);
    }

    @Override
    public Integer getValue() {
        return MemOperator.readInt(addr);
    }

    @Override
    public Keywords getType() {
        return Keywords.Pointer;
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
    public String toString() {
        return String.format("%x", MemOperator.readInt(addr));
    }
}
