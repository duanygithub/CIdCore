package dev.duanyper.cidcore.grammar;

public class FunctionCallTreeNode extends StatementTreeNode {
    @Override
    public String type() {
        return "funcCall";
    }

    public FunctionCallTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
