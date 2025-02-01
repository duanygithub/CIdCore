package dev.duanyper.cidcore.libraries;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.CIdType;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.variable.CIdPOINTER;
import dev.duanyper.cidcore.variable.Variable;

import java.util.ArrayList;

public class CStdIO implements CIdNativeLibrary {
    public static void printf(CInterpreter ci, ValuedArgTreeNode args) throws CIdGrammarException {
        if (!(args.argMap.get("%0") instanceof CIdPOINTER) || !((CIdPOINTER) args.argMap.get("%0")).isString()) {
            throw new CIdGrammarException("printf的第一个参数必须为char*类型");
        }
        String format = ((CIdPOINTER) args.argMap.get("%0")).toString();
        ArrayList<Object> varObjects = new ArrayList<>();
        for (Variable var : args.argMap.values()) {
            if (var instanceof CIdPOINTER && ((CIdPOINTER) var).isString()) {
                varObjects.add(var.toString());
            } else {
                varObjects.add(var.getValue());
            }
        }
        varObjects.remove(0);
        String text = String.format(format, varObjects.toArray());
        System.out.print(text);
    }

    public static Functions include() {
        Functions functions = new Functions();
        functions.funcList.put("printf", CIdType.Void);
        functions.nativeFunctions.put("printf", CStdIO::printf);
        return functions;
    }
}
