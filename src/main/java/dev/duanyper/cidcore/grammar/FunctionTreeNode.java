package dev.duanyper.cidcore.grammar;

public class FunctionTreeNode extends TreeNode {
    public String format;

    @Override
    public String type() {
        return "Function";
    }

    public FunctionTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
