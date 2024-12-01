package dev.duanyper.cidcore.exception;

public class CIdRuntimeException extends RuntimeException {
    public CIdRuntimeException(String msg) {
        super(msg);
    }

    public CIdRuntimeException(String msg, Integer codeIndex) {
        super(msg + "  代码索引: " + codeIndex.toString());
    }
}
