package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.symbols.CIdType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureDescriptor {
    public final Map<CIdType, String> members = new HashMap<>();
    public final String name;

    public StructureDescriptor(StructureTreeNode treeNode, List<String> codeBlocks) {
        for (TreeNode cur : treeNode.children.get(0).children) {
            CIdType type = CIdType.string2Type(codeBlocks.get(cur.lIndex));
            String name = codeBlocks.get(cur.lIndex + 1);
            if (type == CIdType.Struct) {
                if (cur instanceof StructureTreeNode) {
                    //结构体在当前结构体中定义
                    type = CIdType.createStructType(new StructureDescriptor((StructureTreeNode) cur, codeBlocks));
                    if (name.equals("{")) {
                        name = codeBlocks.get(cur.rIndex - 1);
                    }
                } else {
                    //TODO: 结构体在全局定义，此时应该从全局范围查找
                }
            }
            members.put(type, name);
        }
        if (!(codeBlocks.get(treeNode.lIndex + 1)).equals("{")) {
            name = codeBlocks.get(treeNode.lIndex + 1);
        } else name = "<unnamed>";
    }
}
