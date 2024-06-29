package net.duany.ciCore.runtime;

import net.duany.ciCore.gramma.TreeNode;
import net.duany.ciCore.variable.Variable;

import java.util.ArrayList;

public class ValuedArgTreeNode extends TreeNode {
    public ArrayList<Variable> argList = new ArrayList<>();

    public ValuedArgTreeNode() {
        super();
        lIndex = 0;
        rIndex = 0;
    }

    public static ValuedArgTreeNode create(Variable var) {
        ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
        valuedArgTreeNode.argList.add(var);
        return valuedArgTreeNode;
    }

    public ValuedArgTreeNode add(Variable var) {
        this.argList.add(var);
        return this;
    }
}
