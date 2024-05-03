package net.duany.ciCore;

import net.duany.ciCore.expression.MExp2FExp;
import net.duany.ciCore.memory.MemOperator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Start {
    public static void main(String[] args) throws IOException {
        //DEBUG ONLY!!!!
        /*
        CInterpreter ci = new CInterpreter("C:\\Users\\duany\\test.c.txt");
        ci.start();
         */
        String str = "sum = 1+(printf(\"1+1\"+add2)+add3)";
        System.out.println(MExp2FExp.convert(str));
    }
}