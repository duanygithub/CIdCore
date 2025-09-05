package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.libraries.CStdIO;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class DbgStart {

    static public List<String> codeBlocks;

    public static void main(String[] args) throws CIdGrammarException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (args.length != 0) {
            CInterpreter ci = CInterpreter.create(args[0], null, CStdIO.include());
            ci.start();
            return;
        }
        CInterpreter ci = CInterpreter.create("bubblesort.c", null, CStdIO.include());
        ci.start();
    }
}