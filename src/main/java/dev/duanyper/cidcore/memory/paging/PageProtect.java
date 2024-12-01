package dev.duanyper.cidcore.memory.paging;

public class PageProtect {
    public static final int PAGE_NOACCESS = 0;          //0000不能读不能写
    public static final int PAGE_READ = 1;              //0001能读不能写
    public static final int PAGE_WRITE = 2;             //0010能写不能读
    public static final int PAGE_READWRITE = 3;         //0011能读能写
    public static final int PAGE_EXECUTE = 4;           //0100能执行不能读写
    public static final int PAGE_EXECUTE_READ = 5;      //0101能执行能读不能写
    public static final int PAGE_EXECUTE_WRITE = 6;     //0110能执行能写不能读
    public static final int PAGE_EXECUTE_READWRITE = 7; //0111能执行和读写
    public static final int PAGE_WRITECOPY = 11;        //1011能读写，写操作将导致页面复制
    public static final int PAGE_EXECUTE_WRITECOPY = 15;//1111能执行和读写，写操作将导致页面复制

    public static boolean haveProtect(int protect, int requiredProtect) {
        return (protect & requiredProtect) != 0;
    }
}
