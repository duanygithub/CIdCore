package dev.duanyper.cidcore.memory.pool;

import dev.duanyper.cidcore.memory.CIdMemoryManager;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MemoryPool {
    private static final int PAGE_SIZE = 4096;

    private final CIdMemoryManager memoryManager;
    private final TreeMap<Long, Integer> freeList;
    private final Map<Long, Integer> allocationMap;

    public MemoryPool(CIdMemoryManager memoryManager) {
        this.memoryManager = memoryManager;
        this.freeList = new TreeMap<>();
        this.allocationMap = new HashMap<>();
    }

    public long allocateMemory(int size) {
        if (size <= 0) throw new IllegalArgumentException("大小必须为正数");

        size = alignToPowerOfTwo(size);

        // Check free list for suitable block
        for (Map.Entry<Long, Integer> entry : freeList.entrySet()) {
            long addr = entry.getKey();
            int freeSize = entry.getValue();

            if (freeSize >= size && addr % size == 0) {
                freeList.remove(addr);

                if (freeSize > size) {
                    freeList.put(addr + size, freeSize - size);
                }

                allocationMap.put(addr, size);
                return addr;
            }
        }

        // No suitable block found, allocate a new page
        int pages = (int) Math.ceil((double) size / PAGE_SIZE);
        long addr = memoryManager.allocateMemory(pages * PAGE_SIZE);

        if (pages * PAGE_SIZE > size) {
            freeList.put(addr + size, pages * PAGE_SIZE - size);
        }

        allocationMap.put(addr, size);
        return addr;
    }

    public void free(long addr) {
        Integer size = allocationMap.remove(addr);
        if (size == null) {
            throw new IllegalArgumentException("无效或已经释放的地址");
        }

        mergeFreeList(addr, size);
    }

    public byte[] read(long addr, int size) {
        if (isNotAllocated(addr, size)) {
            throw new IllegalArgumentException("无效的地址或大小");
        }

        return memoryManager.read(addr, size);
    }

    public void write(long addr, int size, byte[] data) {
        if (isNotAllocated(addr, size)) {
            throw new IllegalArgumentException("无效的地址或大小");
        }

        memoryManager.write(addr, size, data);
    }

    public void set(long addr, int size, byte b) {
        if (isNotAllocated(addr, size)) {
            throw new IllegalArgumentException("无效的地址或大小");
        }

        memoryManager.set(addr, size, b);
    }

    public int readInt(long addr) {
        byte[] data = read(addr, 4);
        return (data[3] & 0xFF) << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
    }

    public void writeInt(long addr, int value) {
        byte[] data = new byte[4];
        data[0] = (byte) (value & 0xFF);
        data[1] = (byte) ((value >> 8) & 0xFF);
        data[2] = (byte) ((value >> 16) & 0xFF);
        data[3] = (byte) ((value >> 24) & 0xFF);
        write(addr, 4, data);
    }

    public float readFloat(long addr) {
        return Float.intBitsToFloat(readInt(addr));
    }

    public void writeFloat(long addr, float value) {
        writeInt(addr, Float.floatToIntBits(value));
    }

    public boolean readBoolean(long addr) {
        return read(addr, 1)[0] != 0;
    }

    public void writeBoolean(long addr, boolean value) {
        write(addr, 1, new byte[]{(byte) (value ? 1 : 0)});
    }

    public char readChar(long addr) {
        return (char) read(addr, 1)[0];
    }

    public void writeChar(long addr, char value) {
        write(addr, 1, new byte[]{(byte) value});
    }

    public long readLong(long addr) {
        byte[] data = read(addr, 8);
        return ((long) (data[7] & 0xFF) << 56) | ((long) (data[6] & 0xFF) << 48) |
                ((long) (data[5] & 0xFF) << 40) | ((long) (data[4] & 0xFF) << 32) |
                ((long) (data[3] & 0xFF) << 24) | ((long) (data[2] & 0xFF) << 16) |
                ((long) (data[1] & 0xFF) << 8) | (data[0] & 0xFF);
    }

    public void writeLong(long addr, long value) {
        byte[] data = new byte[8];
        for (int i = 0; i < 8; i++) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        write(addr, 8, data);
    }

    private void mergeFreeList(long addr, int size) {
        Long lowerKey = freeList.lowerKey(addr);
        Long higherKey = freeList.higherKey(addr);

        if (lowerKey != null && lowerKey + freeList.get(lowerKey) == addr) {
            size += freeList.remove(lowerKey);
            addr = lowerKey;
        }

        if (higherKey != null && addr + size == higherKey) {
            size += freeList.remove(higherKey);
        }

        freeList.put(addr, size);
    }

    private boolean isNotAllocated(long addr, int size) {
        for (Map.Entry<Long, Integer> entry : allocationMap.entrySet()) {
            long allocAddr = entry.getKey();
            int allocSize = entry.getValue();

            if (addr >= allocAddr && addr + size <= allocAddr + allocSize) {
                return false;
            }
        }
        return true;
    }

    private int alignToPowerOfTwo(int size) {
        int aligned = 1;
        while (aligned < size) {
            aligned *= 2;
        }
        return aligned;
    }
}
