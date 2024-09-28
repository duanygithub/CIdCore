package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.symbols.CIdType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureDescriptor {
    public Map<CIdType, String> members = new HashMap<>();
    public Map<String, CIdType> pointers = new HashMap<>();
    public Map<String, StructureDescriptor> structures = new HashMap<>();
    public String name;

    public StructureDescriptor(StructureTreeNode treeNode, List<String> codeBlocks) {
        for (TreeNode cur : treeNode.subNode) {
            if (!(cur instanceof VarTreeNode)) {
                continue;
            }
            CIdType type = CIdType.string2Keywords(codeBlocks.get(cur.lIndex));
            String name = codeBlocks.get(cur.lIndex + 1);
            members.put(type, name);
            if (type == CIdType.Pointer) {
                pointers.put(name, CIdType.getPointerType(codeBlocks.get(cur.lIndex)));
            }
            if (type == CIdType.Struct) {
            }
        }
        if (!(codeBlocks.get(treeNode.lIndex + 1)).equals("{")) {
            name = codeBlocks.get(treeNode.lIndex + 1);
        } else name = "<unnamed>";
    }
}
