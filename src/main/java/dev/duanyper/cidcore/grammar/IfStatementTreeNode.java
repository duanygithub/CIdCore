package dev.duanyper.cidcore.grammar;

public class IfStatementTreeNode extends TreeNode {
    @Override
    public String type() {
        return "if";
    }

    public IfStatementTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
