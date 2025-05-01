package dev.duanyper.cidcore.runtime;

import dev.duanyper.cidcore.grammar.GrammarProc;
import dev.duanyper.cidcore.grammar.TreeNode;
import dev.duanyper.cidcore.symbols.Variables;

public class CIdRuntimeStackFrame {
    Variables variables;
    TreeNode treeNode;
    GrammarProc grammarProc;

    public CIdRuntimeStackFrame(Variables variables, TreeNode treeNode, GrammarProc grammarProc) {
        this.treeNode = treeNode;
        this.variables = variables;
        this.grammarProc = grammarProc;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    public Variables getVariables() {
        return variables;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    enum FrameType {
        FUNCTION_FRAME, STATEMENT_FRAME
    }
}
