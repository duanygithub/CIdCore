package dev.duanyper.cidcore.grammar;

public class StatementTreeNode extends TreeNode {
    @Override
    public String type() {
        return "statement";
    }

    public StatementTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
