package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.symbols.Variables;

import java.util.List;

public class RootTreeNode extends TreeNode {
    @Override
    public String type() {
        return "root";
    }

    public RootTreeNode(int l, int r, TreeNode parent, List<String> codeBlocks) {
        super(l, r, parent);
        vars = new Variables();
        this.codeBlocks = codeBlocks;
    }
}
