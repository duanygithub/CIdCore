package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.symbols.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureDescriptor {
    public Map<Types, String> members = new HashMap<>();
    public Map<String, Types> pointers = new HashMap<>();
    public Map<String, StructureDescriptor> structures = new HashMap<>();
    public String name;

    public StructureDescriptor(StructureTreeNode treeNode, List<String> codeBlocks) {
        for (TreeNode cur : treeNode.subNode) {
            if (!(cur instanceof VarTreeNode)) {
                continue;
            }
            Types type = Types.string2Keywords(codeBlocks.get(cur.lIndex));
            String name = codeBlocks.get(cur.lIndex + 1);
            members.put(type, name);
            if (type == Types.Pointer) {
                pointers.put(name, Types.getPointerTypes(codeBlocks.get(cur.lIndex)));
            }
            if (type == Types.Struct) {
            }
        }
        if (!(codeBlocks.get(treeNode.lIndex + 1)).equals("{")) {
            name = codeBlocks.get(treeNode.lIndex + 1);
        } else name = "<unnamed>";
    }
}
