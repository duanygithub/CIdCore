package dev.duanyper.cidcore.memory.paging;

public class PageProtect {
    public static final int PAGE_NOACCESS = 0;          //0000
    public static final int PAGE_READ = 1;              //0001
    public static final int PAGE_WRITE = 2;             //0010
    public static final int PAGE_READWRITE = 3;         //0011
    public static final int PAGE_EXECUTE = 4;           //0100
    public static final int PAGE_EXECUTE_READ = 5;      //0101
    public static final int PAGE_EXECUTE_WRITE = 6;     //0110
    public static final int PAGE_EXECUTE_READWRITE = 7; //0111
    public static final int PAGE_WRITECOPY = 11;        //1011
    public static final int PAGE_EXECUTE_WRITECOPY = 15;//1111

    boolean haveProtect(int protect1, int protect2) {
        return (protect1 & protect2) != 0;
    }
}
