package net.duany.ciCore.exception;

public class CIdRuntimeException extends Exception {
    public CIdRuntimeException(String msg) {
        super(msg);
    }

    public CIdRuntimeException(String msg, Integer codeIndex) {
        super(msg + "  代码索引: " + codeIndex.toString());
    }
}
