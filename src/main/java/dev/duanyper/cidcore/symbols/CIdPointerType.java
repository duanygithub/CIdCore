package dev.duanyper.cidcore.symbols;

public class CIdPointerType extends CIdType {
    public final int lvl;
    public final CIdType type;

    public CIdPointerType(int lvl, CIdType type) {
        this.lvl = lvl;
        this.type = type;
    }
}
