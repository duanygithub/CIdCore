package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.exception.CIdRuntimeException;

import java.util.ArrayList;

import static dev.duanyper.cidcore.memory.paging.MathHelper.*;

public class PageTable extends ArrayList<VirtualMemoryPage> {
    long tid;

    public PageTable(long thread) {
        super();
        tid = thread;
        add(new VirtualMemoryPage(null, 0));
    }

    public VirtualMemoryPage getWithVirtualAddress(long virtualAddress) {
        virtualAddress = round4096(virtualAddress);
        int pageIndex = (int) divide4096(virtualAddress);
        try {
            return get(pageIndex);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void addSingleAddressMappingToTable(long virtualAddress, long physicalAddress) {
        if (mod4096(virtualAddress) != 0 || mod4096(physicalAddress) != 0) {
            throw new CIdRuntimeException("页表操作地址必须为页面大小的整数倍");
        }
        PhysicalMemoryPage physicalPage;
        try {
            physicalPage = PagingManager.getPhysicalPages().get((int) divide4096(physicalAddress));
        } catch (IndexOutOfBoundsException e) {
            physicalPage = new PhysicalMemoryPage();
            PagingManager.getPhysicalPages().add(physicalPage);
        }
        VirtualMemoryPage virtualPage;
        try {
            virtualPage = get((int) (virtualAddress >> 12));
        } catch (IndexOutOfBoundsException e) {
            virtualPage = new VirtualMemoryPage(physicalPage, virtualAddress);
            addTableElement(virtualPage);
            return;
        }
        virtualPage.physicalPage = physicalPage;
    }

    public void removeSingleAddressMapping(long virtualAddress) {
        if (mod4096(virtualAddress) != 0) {
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
