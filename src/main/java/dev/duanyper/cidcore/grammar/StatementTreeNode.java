package dev.duanyper.cidcore.grammar;

import java.util.List;

public class StatementTreeNode extends TreeNode {
    @Override
    public String type() {
        return "statement";
    }

    public List<String> postfixExpression;
    public StatementTreeNode(int l, int r, TreeNode parent) {
        super(l, r, parent);
    }
}
