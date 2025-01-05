package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.grammar.StructureDescriptor;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdPointerType;
import dev.duanyper.cidcore.symbols.CIdType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CIdSTRUCT implements Variable {
    int size;
    final long addr;
    final StructureDescriptor descriptor;
    final Map<String, Integer> membersAddressOffsets = new HashMap<>();
    final Map<CIdType, String> members;

    public static CIdSTRUCT createSTRUCT(StructureDescriptor descriptor) {
        return new CIdSTRUCT(descriptor, 0);
    }

    public CIdSTRUCT(StructureDescriptor descriptor, long addr) {
        this.descriptor = descriptor;
        members = new HashMap<>(descriptor.members);
        int curOffset = 0;
        for (int i = 0; i < descriptor.members.size(); i++) {
            membersAddressOffsets.put((String) descriptor.members.keySet().toArray()[i], curOffset);
            curOffset += CIdType.getSize((CIdType) descriptor.members.values().toArray()[i]);
        }
        this.addr = addr == 0 ? MemOperator.allocateMemory(size) : addr;
    }

    public static CIdSTRUCT createWithAllocatedAddress(long addr, StructureDescriptor descriptor) {
        return new CIdSTRUCT(descriptor, addr);
    }

    public Variable getMember(int index) throws CIdRuntimeException {
        int offset, memberSize;
        CIdType memberType;
        offset = (int) membersAddressOffsets.values().toArray()[index];
        memberType = (CIdType) members.keySet().toArray()[index];
        memberSize = CIdType.getSize(memberType);
        if (memberType == CIdType.Int) {
            return CIdINT.createINT(CIdINT.createWithAllocatedAddress(addr + offset).getValue());
        }
        if (memberType == CIdType.Boolean) {
            return CIdBOOLEAN.createBOOLEAN(!Objects.equals(CIdBOOLEAN.createWithAllocatedAddress(addr + offset).getValue(), 0));
        }
        if (memberType instanceof CIdPointerType) {
            CIdPOINTER originalPointer = CIdPOINTER.createWithAllocatedAddress(addr + offset, 1, CIdType.Void);
            return CIdPOINTER.createPOINTER(((CIdPointerType) memberType).lvl, originalPointer.getValue(), ((CIdPointerType) memberType).type);
        }
        if (memberType == CIdType.Char) {
            return CIdCHAR.createCHAR(CIdCHAR.createWithAllocatedAddress(addr + offset).getValue());
        }
        return CIdVOID.createVOID();
    }

    public void setMember(int index, Variable value) {

    }
    @Override
    public Number getValue() {
        return null;
    }

    @Override
    public CIdType getType() {
        return null;
    }

    @Override
    public long getAddress() {
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
