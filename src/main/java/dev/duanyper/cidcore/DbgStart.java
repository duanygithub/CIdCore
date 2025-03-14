package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.libraries.CStdIO;

import java.lang.reflect.InvocationTargetException;
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
    public static void main(String[] args) throws CIdGrammarException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CInterpreter ci = CInterpreter.create("test.c", null, CStdIO.include());
        ci.start();
        /*
        GrammarProc gp = new GrammarProc(new Functions());
        List<String> code = gp.splitCodes("int ** a;");
        for (int i = 0; i < code.size(); i++)
        {
            if (code.get(i).equals("*")) code.set(i, "A*");
        }
        for (int i = 0; i < code.size(); i++) {
            try {
                if (code.get(i).equals("[") && code.get(i + 2).equals("]")) {
                    if (isMatch(code.get(i + 1), NUMBER) || isMatch(code.get(i + 1), HEX_NUMBER)) {
                        String index = code.remove(i + 1);
                        code.remove(i + 1);
                        code.set(i, "[" + index + "]");
                    }
                }
            } catch (IndexOutOfBoundsException ignore){}
        }
        System.out.println(code);
        Method parseSuffixExpression = MExp2FExp.class.getDeclaredMethod("parseSuffixExpression", List.class);
        parseSuffixExpression.setAccessible(true);
        System.out.println(parseSuffixExpression.invoke(null, code));

         */
        /*
        GrammarProc gp = new GrammarProc(new Functions());
        int ret = gp.analyze("int *a = 0, b = 3, c[100] = {0, 1, 2, 3};");
        System.out.println(ret);

         */
    }
}