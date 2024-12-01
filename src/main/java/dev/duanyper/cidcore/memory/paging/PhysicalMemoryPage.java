package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.memory.DirectMemoryHelper;

public class PhysicalMemoryPage {
    DirectMemoryHelper dmh;
    int pageNumber;

    public PhysicalMemoryPage(int pageNumber) {
        this.pageNumber = pageNumber;
        dmh = new DirectMemoryHelper();
    }
}
