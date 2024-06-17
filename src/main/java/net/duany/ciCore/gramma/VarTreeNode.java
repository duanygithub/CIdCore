package net.duany.ciCore.gramma;

public class VarTreeNode extends TreeNode {
    @Override
    public String type() {
        return "var";
    }

    public VarTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
