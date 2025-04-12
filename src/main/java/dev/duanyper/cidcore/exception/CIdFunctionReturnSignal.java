package dev.duanyper.cidcore.exception;

import dev.duanyper.cidcore.variable.Variable;

public class CIdFunctionReturnSignal extends Exception {
    final Variable retVal;

    public CIdFunctionReturnSignal(Variable retVal) {
        this.retVal = retVal;
    }

    public Variable getRetVal() {
        return retVal;
    }
}
