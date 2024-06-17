package net.duany.ciCore.gramma;

public class RootTreeNode extends TreeNode {
    @Override
    public String type() {
        return "root";
    }

    public RootTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
