package dev.duanyper.cidcore;

import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.CIdType;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.variable.Variable;
import dev.duanyper.cidcore.wrapper.CIdWrapper;

import java.util.List;

public class DbgStart {
    static public List<String> codeBlocks;

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
}