package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.exception.CIdRuntimeException;

import java.util.HashMap;

public class PageTable extends HashMap<Long, VirtualMemoryPage> {
    long tid;

    public PageTable(long thread) {
        tid = thread;
    }

    public void addSingleAddressMappingToTable(long virtualAddress, long physicalAddress) {
        if ((virtualAddress & 4095) != 0 || (physicalAddress & 4095) != 0) {
            throw new CIdRuntimeException("页表操作地址必须为页面大小的整数倍");
        }
        PhysicalMemoryPage physicalPage = PagingManager.getPhysicalPages().get((int) (physicalAddress / 4096));
        VirtualMemoryPage virtualPage = new VirtualMemoryPage(physicalPage, virtualAddress);
        addTableElement(virtualPage);
    }

    public void removeSingleAddressMapping(long virtualAddress) {
        if ((virtualAddress & 4095) != 0) {
            throw new CIdRuntimeException("页表操作地址必须为页面大小的整数倍");
        }
        VirtualMemoryPages virtualPages = PagingManager.getVirtualPages().get(tid);
        removeTableElement(virtualPages.getWithVirtualAddress(virtualAddress));
    }

    public void addTableElement(VirtualMemoryPage page) {
        put(tid, page);
    }

    public void removeTableElement(VirtualMemoryPage page) {
        remove(tid, page);
    }

    public VirtualMemoryPage getVirtualPageWithVirtualAddress(long virtualAddress) {
        return PagingManager.getVirtualPages().get(tid).getWithVirtualAddress(virtualAddress);
    }

    public PhysicalMemoryPage getPhysicalPageWithVirtualAddress(long virtualAddress) {
        return getVirtualPageWithVirtualAddress(virtualAddress).physicalPage;
    }
}
