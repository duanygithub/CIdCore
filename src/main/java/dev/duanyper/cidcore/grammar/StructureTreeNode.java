package dev.duanyper.cidcore.grammar;

public class StructureTreeNode extends TreeNode {
    public String type() {
        return "struct";
    }

    public StructureTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
