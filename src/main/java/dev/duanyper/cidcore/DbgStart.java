package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.libraries.CStdIO;

import java.util.List;

public class DbgStart {

    static public List<String> codeBlocks;

    /*
        public static void printf(CInterpreter cInterpreter, ValuedArgTreeNode arg) {
            Variable var = arg.argMap.get("%0");
            System.out.println(var.toString());
        }

        public static void main(String[] args) {
            //DEBUG ONLY!!!!
            String str = "int main(){int a = 1; int *p = &a; int **p1 = &p; printf(**p1); return 0;}";
            Functions functions = new Functions();
            functions.funcList.put("printf", CIdType.Void);
            functions.nativeFunctions.put("printf", DbgStart::printf);
            new CIdWrapper().executeProgram(str, functions, null);
        }
         */
    public static void main(String[] args) throws CIdGrammarException {
        CInterpreter ci = CInterpreter.create("test.c", null, CStdIO.include());
        ci.start();
    }
}