package dev.duanyper.cidcore.runtime;

import java.util.Stack;

public class CIdRuntimeStack extends Stack<CIdRuntimeStackFrame> {
    static ThreadLocal<CIdRuntimeStack> current = new ThreadLocal<>();

    public static CIdRuntimeStack getCurrent() {
        CIdRuntimeStack ret = current.get();
        if (ret == null)
            current.set(ret = new CIdRuntimeStack());
        return ret;
    }
}
