package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdFunctionReturnSignal;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;

@FunctionalInterface
public interface NativeFunctionInterface {
    void nativeFunc(CInterpreter ci, ValuedArgTreeNode args) throws CIdFunctionReturnSignal, CIdGrammarException;

    default void invoke(CInterpreter ci, ValuedArgTreeNode args) throws CIdFunctionReturnSignal, CIdGrammarException {
        nativeFunc(ci, args);
    }
}
