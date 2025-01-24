package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.grammar.ArgTreeNode;
import dev.duanyper.cidcore.grammar.BlockTreeNode;

import java.util.HashMap;
import java.util.Map;

public class Functions {
    public final Map<String, CIdType> funcList = new HashMap<>();
    public final Map<String, BlockTreeNode> codeIndex = new HashMap<>();
    public final Map<String, ArgTreeNode> argIndex = new HashMap<>();
    public final Map<String, NativeFunctionInterface> nativeFunctions = new HashMap<>();

    public void merge(Functions fs) throws CIdGrammarException {
        for (var e : fs.funcList.entrySet()) {
            if (funcList.containsKey(e.getKey())) throw new CIdGrammarException("相同的函数定义");
            funcList.put(e.getKey(), e.getValue());
        }
        codeIndex.putAll(fs.codeIndex);
        argIndex.putAll(fs.argIndex);
        nativeFunctions.putAll(fs.nativeFunctions);
    }
}
