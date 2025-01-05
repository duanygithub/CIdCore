package dev.duanyper.cidcore.memory.paging;

import dev.duanyper.cidcore.memory.CIdMemoryManager;

import java.util.Arrays;

public class PagingMemoryManager implements CIdMemoryManager {
    static long maxMemorySize = 4 * 1024 * 1024;

    @Override
    public void init() {
        PagingManager.init();
    }

    @Override
    public long allocateMemory(int size) {
        return allocateMemory(0, size, PageProtect.PAGE_EXECUTE_READWRITE);
    }

    @Override
    public long allocateMemory(long preferredAddress, int size, int pageProtect) {
        return PagingManager.allocateMemory(preferredAddress, size, pageProtect);
    }

    @Override
    public byte[] read(long addr, int size) {
        return PagingManager.readMemory(addr, size);
    }

    @Override
    public int write(long addr, int size, byte[] data) {
        PagingManager.writeMemory(addr, data, size);
        return 0;
    }

    @Override
    public void set(long addr, int size, byte b) {
        byte[] data = new byte[size];
        Arrays.fill(data, b);
        write(addr, size, data);
    }
}
