package dev.duanyper.cidcore.exception;

public class CIdFatalException extends CIdRuntimeException {
    public CIdFatalException(String message) {
        super(message);
    }

    public CIdFatalException(Exception e) {
        super(e.getMessage());
    }
}
