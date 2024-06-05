package net.duany.ciCore.gramma;

public class FunctionCallTreeNode extends TreeNode {
    @Override
    public String type() {
        return "funcCall";
    }

    public FunctionCallTreeNode(int l, int r) {
        super(l, r);
    }
}
