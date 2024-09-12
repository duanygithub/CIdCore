package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.grammar.StructureDescriptor;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.Types;

import java.util.HashMap;
import java.util.Map;

public class CIdSTRUCT implements Variable {
    int size, addr;
    StructureDescriptor descriptor;
    Map<String, Integer> membersAddressOffsets = new HashMap<>();
    Map<Types, String> members;

    public static CIdSTRUCT createSTRUCT(StructureDescriptor descriptor) {
        return new CIdSTRUCT(descriptor, 0);
    }

    public static CIdSTRUCT createWithAllocatedAddress(int addr, StructureDescriptor descriptor) {
        return new CIdSTRUCT(descriptor, addr);
    }

    public CIdSTRUCT(StructureDescriptor descriptor, int addr) {
        this.descriptor = descriptor;
        members = new HashMap<>(descriptor.members);
        int curOffset = 0;
        for (int i = 0; i < descriptor.members.size(); i++) {
            membersAddressOffsets.put((String) descriptor.members.keySet().toArray()[i], curOffset);
            curOffset += Types.getSize((Types) descriptor.members.values().toArray()[i]);
        }
        this.addr = addr == 0 ? MemOperator.allocateMemory(size) : addr;
    }

    public Variable getMember(int index) {
        int offset, memberSize;
        Types memberType;
        offset = (int) membersAddressOffsets.values().toArray()[index];
        memberType = (Types) members.keySet().toArray()[index];
        memberSize = Types.getSize(memberType);
        if (memberType == Types.Int) return CIdINT.createWithAllocatedAddress(addr + offset);
        if (memberType == Types.Boolean) return CIdBOOLEAN.createWithAllocatedAddress(addr + offset);
        if (memberType == Types.Pointer) return CIdPOINTER.createWithAllocatedAddress(addr + offset, 1, Types.Void);
        if (memberType == Types.Char) return CIdCHAR.createWithAllocatedAddress(addr + offset);
        return CIdVOID.createVOID();
    }

    public void setMember(int index, Variable value) {

    }
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
