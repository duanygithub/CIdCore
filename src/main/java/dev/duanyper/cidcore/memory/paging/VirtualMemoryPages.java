package dev.duanyper.cidcore.memory.paging;

import java.util.ArrayList;

public class VirtualMemoryPages extends ArrayList<VirtualMemoryPage> {
    public VirtualMemoryPage getWithVirtualAddress(long virtualAddress) {
        virtualAddress &= 4294963200L;
        int pageIndex = (int) (virtualAddress >> 12);
        return get(pageIndex);
    }
}
