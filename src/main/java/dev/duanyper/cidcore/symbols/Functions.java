package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.grammar.ArgTreeNode;
import dev.duanyper.cidcore.grammar.BlockTreeNode;

import java.util.HashMap;
import java.util.Map;

public class Functions {
    public final Map<String, CIdType> funcList = new HashMap<>();
    public final Map<String, BlockTreeNode> codeIndex = new HashMap<>();
    public final Map<String, ArgTreeNode> argIndex = new HashMap<>();
    public final Map<String, NativeFunctionInterface> nativeFunctions = new HashMap<>();
}
