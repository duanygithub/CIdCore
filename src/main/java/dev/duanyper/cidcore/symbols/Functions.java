package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.grammar.ArgTreeNode;
import dev.duanyper.cidcore.grammar.BlockTreeNode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Functions {
    public Map<String, CIdType> funcList = new HashMap<>();
    public Map<String, BlockTreeNode> codeIndex = new HashMap<>();
    public Map<String, ArgTreeNode> argIndex = new HashMap<>();
    public Map<String, Method> nativeFunctions = new HashMap<>();
}
