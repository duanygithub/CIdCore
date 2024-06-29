package net.duany.ciCore.gramma;

public class WhileTreeNode extends TreeNode {
    @Override
    public String type() {
        return "while";
    }

    public WhileTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
