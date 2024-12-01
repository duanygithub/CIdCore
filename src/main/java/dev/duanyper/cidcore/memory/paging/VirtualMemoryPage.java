package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.exception.CIdPageFaultException;
import dev.duanyper.cidcore.exception.CIdRuntimeException;

public class VirtualMemoryPage {
    PhysicalMemoryPage physicalPage;
    Integer protect;
    long virtualAddress;
    boolean isMissing;

    public VirtualMemoryPage(PhysicalMemoryPage physical, long virtualAddress) {
        physicalPage = physical;
        this.virtualAddress = virtualAddress;
    }

    public boolean isMissing() {
        return isMissing;
    }
    public void setMissing(boolean missing) {
        isMissing = missing;
    }

    public Integer getProtect() {
        return protect;
    }

    public void setProtect(Integer protect) {
        this.protect = protect;
    }

    public byte[] read(long addr, int size) {
        if (!PageProtect.haveProtect(protect, PageProtect.PAGE_READ)) {
            throw new CIdRuntimeException(String.format("访问违规, 试图读取0x%x, 但该页面无法读取", addr + virtualAddress));
        }
        if (isMissing) {
            throw new CIdPageFaultException();
        }
        return physicalPage.read(addr, size);
    }

    public void write(long addr, byte[] data, int size) {
        if (!PageProtect.haveProtect(protect, PageProtect.PAGE_WRITE)) {
            throw new CIdRuntimeException(String.format("访问违规, 试图写入0x%x, 但该页面无法写入", addr + virtualAddress));
        }
        if (isMissing) {
            throw new CIdPageFaultException();
        }
        physicalPage.write(addr, data, size);
    }
}
