package dev.duanyper.cidcore.memory.real;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.CIdMemoryManager;

import java.util.ArrayList;
import java.util.Arrays;

public class RealMemoryManager implements CIdMemoryManager {
    ArrayList<MemPage> pages;

    @Override
    public void init() {
        pages = new ArrayList<>();
    }

    @Override
    public long allocateMemory(long preferredAddress, int size, int pageProtect) {
        return allocateMemory(size);
    }

    @Override
    public long allocateMemory(int size) {
        for (int i = 0; i < pages.size(); i++) {
            MemPage page = pages.get(i);
            int index = page.allocateBytes(size);
            if (index == -1) continue;
            return i * 65536L + index;
        }
        if (pages.size() < 16 * 8) {
            MemPage newPage;
            if (pages.isEmpty()) {
                newPage = new MemPage(true);
            } else newPage = new MemPage(false);
            pages.add(newPage);
            int index = newPage.allocateBytes(size);
            if (index == -1) {
                return -1;
            }
            return (pages.size() - 1) * 65536L + index;
        }
        return -1;
    }

    @Override
    public byte[] read(long addr, int size) throws CIdRuntimeException {
        int pageIndex = (int) (addr / 65536);
        int index = (int) (addr % 65536);
        return pages.get(pageIndex).readBytes(index, size);
    }

    @Override
    public int write(long addr, int size, byte[] data) throws CIdRuntimeException {
        int pageIndex = (int) (addr / 65536);
        int index = (int) (addr % 65536);
        return pages.get(pageIndex).writeBytes(index, size, data);
    }

    @Override
    public void set(long addr, int size, byte b) throws CIdRuntimeException {
        int pageIndex = (int) (addr / 65536);
        int index = (int) (addr % 65536);
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, b);
        pages.get(pageIndex).writeBytes(index, size, bytes);
    }

    public void freeMemory(int addr, int size) {
        int pageIndex = addr / 65536;
        int index = addr % 65536;
        pages.get(pageIndex).freeBytes(index, size);
    }
}
