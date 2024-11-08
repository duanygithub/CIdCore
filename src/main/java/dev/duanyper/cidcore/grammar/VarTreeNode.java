package dev.duanyper.cidcore.grammar;

public class VarTreeNode extends StatementTreeNode {
    @Override
    public String type() {
        return "var";
    }

    public VarTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
