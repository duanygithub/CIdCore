package net.duany.ciCore.gramma;

public class StatementTreeNode extends TreeNode {
    @Override
    public String type() {
        return "statement";
    }

    public StatementTreeNode(int l, int r) {
        super(l, r);
    }
}
