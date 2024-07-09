package net.duany.ciCore.exception;

public class CIdAssert {
    public static final String INCORRECT_TYPE = "类型未定义";

    public static void _assert(boolean b, String failMessage) throws CIdGrammarException {
        if (!b) {
            switch (failMessage) {
                case INCORRECT_TYPE: {
                    throw new CIdGrammarException(failMessage);
                }
            }
        }
    }
}
