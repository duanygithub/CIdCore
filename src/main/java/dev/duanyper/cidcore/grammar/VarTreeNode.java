package dev.duanyper.cidcore.grammar;

public class VarTreeNode extends StatementTreeNode {
    @Override
    public String type() {
        return "var";
    }

    public VarTreeNode(int l, int r, TreeNode parent, String name) {
        super(l, r, parent);
    }
}
