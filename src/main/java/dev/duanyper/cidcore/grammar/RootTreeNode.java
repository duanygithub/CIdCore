package dev.duanyper.cidcore.grammar;

import java.util.HashMap;

public class RootTreeNode extends TreeNode {
    @Override
    public String type() {
        return "root";
    }

    public RootTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
        vars.vars = new HashMap<>();
    }
}
