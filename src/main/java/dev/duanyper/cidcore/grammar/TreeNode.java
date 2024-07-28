package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.Start;
import dev.duanyper.cidcore.symbols.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeNode {
    public int lIndex = -1, rIndex = -1;
    String content;
    public ArrayList<TreeNode> subNode = new ArrayList<>();
    public TreeNode parentNode;
    public Variables vars = new Variables();

    public TreeNode(int l, int r, TreeNode parent) {
        lIndex = l;
        rIndex = r;
        parentNode = parent;
        StringBuilder sb = new StringBuilder();
        for (String s : Start.codeBlocks.subList(l, r)) {
            sb.append(s).append(" ");
        }
        content = sb.toString();
        if (type().equals("Function") || type().equals("block")) {
            Variables vars = new Variables();
            vars.vars = new HashMap<>(parent.vars.vars);
            this.vars = vars;
        } else if (!type().equals("root")) {
            vars.vars = parent.vars.vars;
        }
    }

    public TreeNode() {

    }

    public int getLIndex() {
        return lIndex;
    }

    public int getRIndex() {
        return rIndex;
    }

    public String type() {
        return "nul";
    }

    public String toString(List<String> codeBlocks) {
        StringBuilder ret = new StringBuilder();
        for (String str : codeBlocks.subList(lIndex, rIndex)) {
            ret.append(str).append(" ");
        }
        return ret.toString();
    }
}
