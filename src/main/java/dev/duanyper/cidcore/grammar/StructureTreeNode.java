package dev.duanyper.cidcore.grammar;

public class StructureTreeNode extends TreeNode {
    public final StructureDescriptor descriptor;
    public String type() {
        return "struct";
    }

    public StructureTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
        descriptor = new StructureDescriptor(this, codeBlocks);
    }
}
