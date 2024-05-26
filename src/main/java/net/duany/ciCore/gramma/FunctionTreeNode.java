package net.duany.ciCore.gramma;

public class FunctionTreeNode extends TreeNode {
    @Override
    public String type() {
        return "Function";
    }

    public FunctionTreeNode(int l, int r) {
        super(l, r);
    }
}
