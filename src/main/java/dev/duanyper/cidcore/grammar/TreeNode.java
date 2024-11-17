package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.DbgStart;
import dev.duanyper.cidcore.symbols.Variables;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    public int lIndex = -1, rIndex = -1;
    String content;
    public ArrayList<TreeNode> subNode = new ArrayList<>();
    public TreeNode parentNode;
    public Variables vars = new Variables();
    public List<String> codeBlocks;

    public TreeNode(int l, int r, TreeNode parent) {
        lIndex = l;
        rIndex = r;
        parentNode = parent;
        StringBuilder sb = new StringBuilder();
        for (String s : DbgStart.codeBlocks.subList(l, r)) {
            sb.append(s).append(" ");
        }
        content = sb.toString();
        if (type().equals("Function") || type().equals("block")) {
            this.vars = new Variables(parent.vars);
        } else if (!type().equals("root")) {
            vars = parent.vars;
            codeBlocks = parent.codeBlocks;
        }
    }

    public TreeNode() {
    }
    public String type() {
        return "nul";
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (String str : codeBlocks.subList(lIndex, rIndex)) {
            ret.append(str).append(" ");
        }
        return ret.toString();
    }
}
