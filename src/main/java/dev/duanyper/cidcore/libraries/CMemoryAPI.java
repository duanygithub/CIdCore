package dev.duanyper.cidcore.libraries;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdFunctionReturnException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.CIdPointerType;
import dev.duanyper.cidcore.symbols.CIdType;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.variable.CIdINT;
import dev.duanyper.cidcore.variable.CIdPOINTER;

public class CMemoryAPI {
    public static void VirtualAlloc(CInterpreter cInterpreter, ValuedArgTreeNode args) throws CIdFunctionReturnException {
        long preferredAddress = ((CIdINT) args.argMap.get("%0")).getValue();
        int size = ((CIdINT) args.argMap.get("%1")).getValue();
        int protect = ((CIdINT) args.argMap.get("%2")).getValue();
        long addr = MemOperator.allocateMemory(preferredAddress, size, protect);
        throw new CIdFunctionReturnException(CIdPOINTER.createPOINTER(1, addr, CIdType.Void));
    }

    public static Functions include() {
        Functions functions = new Functions();
        functions.funcList.put("VirtualAlloc", CIdPointerType.createPointerType(1, CIdType.Void));
        functions.nativeFunctions.put("VirtualAlloc", CMemoryAPI::VirtualAlloc);
        return functions;
    }
}
