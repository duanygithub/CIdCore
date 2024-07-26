package dev.duanyper.cidcore.grammar;

public class ArgTreeNode extends TreeNode {
    @Override
    public String type() {
        return "arg";
    }

    public ArgTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
