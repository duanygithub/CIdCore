package dev.duanyper.cidcore.memory.paging;

import java.util.ArrayList;
import java.util.HashMap;

public class PagingManager {
    //储存所有物理内存页
    static ArrayList<PhysicalMemoryPage> physicalMemoryPages = null;
    static int curFreePhysicalMemoryPage = 0;
    //储存每个线程(CId进程)的页表
    static HashMap<Long, PageTable> pageTables = new HashMap<>();

    static {
        init();
    }

    static public ArrayList<PhysicalMemoryPage> getPhysicalPages() {
        return physicalMemoryPages;
    }

    static public PageTable getCurrentPageTable() {
        PageTable ret = pageTables.get(Thread.currentThread().getId());
        if (ret == null) {
            return createNewPageTable();
        }
        return ret;
    }

    public PagingManager() {
    }

    public static void init() {
        physicalMemoryPages = new ArrayList<>(1024 * 1024);
    }

    public static PageTable createNewPageTable() {
        PageTable newPageTable = new PageTable(Thread.currentThread().getId());
        pageTables.put(Thread.currentThread().getId(), newPageTable);
        return newPageTable;
    }

    static int getNextFreePhysicalPage() {
        curFreePhysicalMemoryPage++;
        if (curFreePhysicalMemoryPage > 1024 * 1024) {
            curFreePhysicalMemoryPage = 0;
        }
        return curFreePhysicalMemoryPage;
    }

    public static int allocateMemory(long preferredAddress, long size) {
        long tid = Thread.currentThread().getId();
        long roundedVirtualAddress = preferredAddress & 4294963200L;
        PageTable currentPageTable = getCurrentPageTable();
        if (preferredAddress == 0 || currentPageTable.getVirtualPageWithVirtualAddress(preferredAddress) == null) {
            //该内存页没有被分配，将使用此内存页
            for (int i = 0; size > 0; i++, size -= 4096) {
                currentPageTable.addSingleAddressMappingToTable(
                        roundedVirtualAddress + ((long) i << 12),
                        (long) getNextFreePhysicalPage() << 12);
            }
        }
        //该内存页已经被分配，需要重新寻找空闲内存页
        for (VirtualMemoryPage page : currentPageTable) {
            if (page.physicalPage == null) {
                roundedVirtualAddress = page.virtualAddress;
            }
        }
        if (roundedVirtualAddress == 0) {
            if (currentPageTable.size() >= 1024 * 1024) {
                throw new OutOfMemoryError("CId::allocateMemory");
            }
            roundedVirtualAddress = (long) currentPageTable.size() << 12;
        }
        for (int i = 0; size > 0; i++, size -= 4096) {
            currentPageTable.addSingleAddressMappingToTable(
                    roundedVirtualAddress + ((long) i << 12),
                    (long) getNextFreePhysicalPage() << 12);
        }
        return 0;
    }
}
