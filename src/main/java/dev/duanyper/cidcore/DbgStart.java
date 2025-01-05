package dev.duanyper.cidcore;

import dev.duanyper.cidcore.memory.paging.PageProtect;
import dev.duanyper.cidcore.memory.paging.PageTable;
import dev.duanyper.cidcore.memory.paging.PagingManager;

import java.util.Arrays;
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
    public static void main(String[] args) {
        PageTable dummy = PagingManager.getCurrentPageTable();
        long addr = PagingManager.allocateMemory(0, 4096, PageProtect.PAGE_READWRITE);
        byte[] b = {1, 2, 3};
        PagingManager.writeMemory(addr, b, b.length);
        System.out.println(Arrays.toString(PagingManager.readMemory(addr, 10)));
        return;
    }
}