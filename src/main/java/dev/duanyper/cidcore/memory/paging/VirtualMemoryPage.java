package dev.duanyper.cidcore.memory.paging;

public class VirtualMemoryPage {
    PhysicalMemoryPage physicalPage;
    PageProtect protect;
    boolean isMissing;

    public VirtualMemoryPage(PhysicalMemoryPage physical) {
        physicalPage = physical;
    }

    public boolean isMissing() {
        return isMissing;
    }

    public void setMissing(boolean missing) {
        isMissing = missing;
    }

    public PageProtect getProtect() {
        return protect;
    }

    public void setProtect(PageProtect protect) {
        this.protect = protect;
    }
}
