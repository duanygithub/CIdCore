package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.symbols.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureDescriptor {
    Map<Types, String> members = new HashMap<>();

    public StructureDescriptor(StructureTreeNode treeNode, List<String> codeBlocks) {
        for (TreeNode cur : treeNode.subNode) {
            if (cur.type().equals("var")) {
                continue;
            }
            String type = codeBlocks.get(cur.lIndex);
            String name = codeBlocks.get(cur.lIndex + 1);
            members.put(Types.string2Keywords(type), name);
        }
    }
}
