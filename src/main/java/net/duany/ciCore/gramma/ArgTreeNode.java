package net.duany.ciCore.gramma;

public class ArgTreeNode extends TreeNode {
    @Override
    public String type() {
        return "arg";
    }

    public ArgTreeNode(int l, int r) {
        super(l, r);
    }
}
