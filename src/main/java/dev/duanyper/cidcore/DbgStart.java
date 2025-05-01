package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.libraries.CStdIO;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class DbgStart {

    static public List<String> codeBlocks;

    public static void main(String[] args) throws CIdGrammarException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CInterpreter ci = CInterpreter.create("bubblesort.c", null, CStdIO.include());
        ci.start();
        //List<String> tokens = Arrays.asList("b", "=", "a", "++");
        //MExp2FExp.InfixToPostfixParser parser = new MExp2FExp.InfixToPostfixParser(tokens);
        //System.out.println(parser.parse());
    }
}