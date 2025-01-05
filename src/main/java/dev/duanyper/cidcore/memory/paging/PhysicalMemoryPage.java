package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.exception.CIdFatalException;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import static dev.duanyper.cidcore.memory.paging.MathHelper.divide4096;

public class PhysicalMemoryPage {
    static Unsafe unsafe = null;
    long address;
    int pageIndex;
    int totPage = 0;

    LinkedList<VirtualMemoryPage> linkedVirtualPages;

    public PhysicalMemoryPage() {
        if (totPage >= divide4096(PagingMemoryManager.maxMemorySize)) {
            throw new OutOfMemoryError();
        }
        if (unsafe == null) {
            try {
                Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
                unsafeConstructor.setAccessible(true);
                unsafe = unsafeConstructor.newInstance();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new CIdFatalException("无法初始化堆外内存分配, 原因: " + e.getMessage());
            }
        }
        pageIndex = totPage;
        totPage++;
        linkedVirtualPages = new LinkedList<>();
        commit();
    }

    public void commit() {
        address = unsafe.allocateMemory(4096);
        unsafe.setMemory(address, 1024L, (byte) 0);
    }

    public void free() {
        unsafe.freeMemory(address);
    }

    public byte[] read(long addr, int size) {
        if (size > 4096) {
            throw new IndexOutOfBoundsException(addr);
        }
        byte[] ret = new byte[size];
        for (int i = 0; i < size; i++) {
            ret[i] = unsafe.getByte(i + address + addr);
        }
        return ret;
    }

    public void write(long addr, byte[] data, int size) {
        if (addr > 4096) {
            throw new IndexOutOfBoundsException(addr);
        }
        for (int i = 0; i < size; i++) {
            unsafe.putByte(i + address + addr, data[i]);
        }
    }
}
