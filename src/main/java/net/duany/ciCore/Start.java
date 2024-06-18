package net.duany.ciCore;

import net.duany.ciCore.gramma.GrammarProc;
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
        /*
        CInterpreter ci = new CInterpreter("C:\\Users\\duany\\test.c.txt");
        ci.start();
         */
        /*
        String str = "int main(int argc, char* argv) {int i = 1 + printf(def, def+ 1);}";
        Functions.funcList.put("printf", Keywords.Int);
        Functions.codeIndex.put("printf", null);
        new GrammarProc().analyze(str);
        return;
         */
        int addr = MemOperator.allocateMemory(4);
        System.out.println(addr);
        MemOperator.writeInt(addr, 114514);
        for (byte b : MemOperator.read(addr, 4)) {
            System.out.println(Integer.toHexString(b));
        }
        System.out.println(MemOperator.readInt(addr));
    }
}