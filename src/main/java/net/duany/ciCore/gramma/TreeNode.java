package net.duany.ciCore.gramma;

import net.duany.ciCore.Start;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    int lIndex = -1, rIndex = -1;
    String content;
    ArrayList<TreeNode> subNode = new ArrayList<>();

    public TreeNode(int l, int r) {
        lIndex = l;
        rIndex = r;
        StringBuilder sb = new StringBuilder();
        for (String s : Start.codeBlocks.subList(l, r)) {
            sb.append(s).append(" ");
        }
        content = sb.toString();
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
