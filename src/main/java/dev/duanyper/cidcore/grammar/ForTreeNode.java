package dev.duanyper.cidcore.grammar;

public class ForTreeNode extends TreeNode {
    @Override
    public String type() {
        return "for";
    }

    public ForTreeNode(int lIndex, int rIndex, TreeNode parentNode) {
        super(lIndex, rIndex, parentNode);
    }
}
