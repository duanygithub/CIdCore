package dev.duanyper.cidcore.exception;

import dev.duanyper.cidcore.variable.Variable;

public class CIdFunctionReturnException extends Exception{
    Variable retVal;
    public CIdFunctionReturnException(Variable retVal) {
        this.retVal = retVal;
    }

    public Variable getRetVal() {
        return retVal;
    }
}
