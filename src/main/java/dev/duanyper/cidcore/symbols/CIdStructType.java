package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.grammar.StructureDescriptor;

public class CIdStructType extends CIdType {
    public final StructureDescriptor descriptor;

    public CIdStructType(StructureDescriptor sd) {
        descriptor = sd;
    }
}
