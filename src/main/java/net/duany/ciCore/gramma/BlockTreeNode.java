package net.duany.ciCore.gramma;

public class BlockTreeNode extends TreeNode {
    @Override
    public String type() {
        return "block";
    }

    public BlockTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
