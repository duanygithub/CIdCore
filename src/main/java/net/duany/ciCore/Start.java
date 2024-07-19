package net.duany.ciCore;

import net.duany.ciCore.gramma.GrammarProc;
import net.duany.ciCore.gramma.MExp2FExp;
import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class Start {
    static public List<String> codeBlocks;

    public static void main(String[] args) throws IOException {
        //DEBUG ONLY!!!!
        String str = "int f(int* pointer, int n){ return *pointer + n; } int main(){ int a = 666; int b = 4; return f(&a, b) + 1; }";
        Functions.funcList.put("printf", Keywords.Int);
        Functions.codeIndex.put("printf", null);
        CInterpreter cInterpreter = new CInterpreter(str, false);
        List<String> res = MExp2FExp.convert(str);
        System.out.println(cInterpreter.start());
    }
}