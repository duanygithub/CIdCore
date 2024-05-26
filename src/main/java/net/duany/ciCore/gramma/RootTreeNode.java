package net.duany.ciCore.gramma;

public class RootTreeNode extends TreeNode {
    @Override
    public String type() {
        return "root";
    }

    public RootTreeNode(int l, int r) {
        super(l, r);
    }
}
