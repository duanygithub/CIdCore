package dev.duanyper.cidcore.exception;

public class CIdPageFaultException extends RuntimeException {
    public CIdPageFaultException() {
        super("缺页异常");
    }
}
