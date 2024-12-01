package dev.duanyper.cidcore.memory.paging;

import java.util.ArrayList;
import java.util.HashMap;

public class PagingManager {
    static ArrayList<PhysicalMemoryPage> physicalMemoryPages = null;
    static HashMap<Long, VirtualMemoryPages> virtualMemoryPages;

    static public ArrayList<PhysicalMemoryPage> getPhysicalPages() {
        return physicalMemoryPages;
    }

    static public HashMap<Long, VirtualMemoryPages> getVirtualPages() {
        return virtualMemoryPages;
    }

    public PagingManager() {
    }

    public static void init() {
        physicalMemoryPages = new ArrayList<>(1024 * 1024);
        virtualMemoryPages = new HashMap<>(1024 * 1024);
    }

    public static int allocateMemory(long preferredAddress, long size) {
        long tid = Thread.currentThread().getId();
        return 0;
    }
}
