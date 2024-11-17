package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdFunctionReturnException;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;

@FunctionalInterface
public interface NativeFunctionInterface {
    void nativeFunc(CInterpreter ci, ValuedArgTreeNode args) throws CIdFunctionReturnException;
    default void invoke(CInterpreter ci, ValuedArgTreeNode args) throws CIdFunctionReturnException {
        nativeFunc(ci, args);
    }
}
