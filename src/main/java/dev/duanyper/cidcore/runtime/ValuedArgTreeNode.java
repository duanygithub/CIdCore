package dev.duanyper.cidcore.runtime;

import dev.duanyper.cidcore.grammar.TreeNode;
import dev.duanyper.cidcore.variable.Variable;

import java.util.HashMap;
import java.util.Map;

public class ValuedArgTreeNode extends TreeNode {
    public final Map<String, Variable> argMap = new HashMap<>();

    public ValuedArgTreeNode() {
        super();
        lIndex = 0;
        rIndex = 0;
    }

    public static ValuedArgTreeNode create(String name, Variable var) {
        ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
        valuedArgTreeNode.argMap.put(name, var);
        return valuedArgTreeNode;
    }

    public ValuedArgTreeNode add(String name, Variable var) {
        this.argMap.put(name, var);
        return this;
    }
}
