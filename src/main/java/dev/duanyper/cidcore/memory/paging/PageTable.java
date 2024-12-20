package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.exception.CIdRuntimeException;

import java.util.ArrayList;

public class PageTable extends ArrayList<VirtualMemoryPage> {
    long tid;

    public PageTable(long thread) {
        super(1024 * 1024);
        tid = thread;
    }

    public VirtualMemoryPage getWithVirtualAddress(long virtualAddress) {
        virtualAddress &= 4294963200L;
        int pageIndex = (int) (virtualAddress >> 12);
        return get(pageIndex);
    }

    public void addSingleAddressMappingToTable(long virtualAddress, long physicalAddress) {
        if ((virtualAddress & 4095) != 0 || (physicalAddress & 4095) != 0) {
            throw new CIdRuntimeException("页表操作地址必须为页面大小的整数倍");
        }
        PhysicalMemoryPage physicalPage = PagingManager.getPhysicalPages().get((int) (physicalAddress / 4096));
        VirtualMemoryPage virtualPage = get((int) (virtualAddress >> 12));
        if (virtualPage == null) {
            virtualPage = new VirtualMemoryPage(physicalPage, virtualAddress);
            addTableElement(virtualPage);
        } else virtualPage.physicalPage = physicalPage;
    }

    public void removeSingleAddressMapping(long virtualAddress) {
        if ((virtualAddress & 4095) != 0) {
            throw new CIdRuntimeException("页表操作地址必须为页面大小的整数倍");
        }
        removeTableElement(getWithVirtualAddress(virtualAddress));
    }

    public void addTableElement(VirtualMemoryPage page) {
        if (size() >= 1024 * 1024) {
            throw new OutOfMemoryError("CId::allocateMemory");
        }
        add(page);
    }

    public void removeTableElement(VirtualMemoryPage page) {
        page.physicalPage = null;
    }

    public VirtualMemoryPage getVirtualPageWithVirtualAddress(long virtualAddress) {
        return getWithVirtualAddress(virtualAddress);
    }

    public PhysicalMemoryPage getPhysicalPageWithVirtualAddress(long virtualAddress) {
        return getVirtualPageWithVirtualAddress(virtualAddress).physicalPage;
    }
}
