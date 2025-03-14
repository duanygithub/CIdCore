package dev.duanyper.cidcore.memory;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.paging.PagingMemoryManager;
import dev.duanyper.cidcore.memory.pool.MemoryPool;

import java.nio.ByteBuffer;

public class MemOperator {
    static CIdMemoryManager memoryManager;
    static MemoryPool memoryPool;

    static {
        memoryManager = new PagingMemoryManager();
        memoryManager.init();
        memoryPool = new MemoryPool(memoryManager);
    }

    public static MemoryPool getPool() {
        return memoryPool;
    }

    public static long allocateMemory(int size) {
        return memoryManager.allocateMemory(size);
    }

    public static long allocateMemory(long preferredAddress, int size, int pageProtect) {
        return memoryManager.allocateMemory(preferredAddress, size, pageProtect);
    }

    public static byte[] read(long addr, int size) {
        return memoryManager.read((int) addr, size);
    }

    public static int write(long addr, int size, byte[] data) {
        return memoryManager.write((int) addr, size, data);
    }

    static public int writeInt(long addr, int i) throws CIdRuntimeException {
        byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
        return write(addr, 4, bytes);
    }

    public static int readInt(long addr) throws CIdRuntimeException {
        byte[] bytes = read(addr, 4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    static public int writeFloat(long addr, float f) throws CIdRuntimeException {
        byte[] bytes = ByteBuffer.allocate(4).putFloat(f).array();
        return write(addr, 4, bytes);
    }

    public static float readFloat(long addr) throws CIdRuntimeException {
        byte[] bytes = read(addr, 4);
        return ByteBuffer.wrap(bytes).getFloat();
    }

    static public int writeChar(long addr, char c) throws CIdRuntimeException {
        byte[] bytes = ByteBuffer.allocate(4).putChar(c).array();
        return write(addr, 1, bytes);
    }

    public static char readChar(long addr) throws CIdRuntimeException {
        return (char) (read(addr, 1))[0];
    }

    public static int writeBoolean(long addr, boolean b) throws CIdRuntimeException {
        byte[] bytes = new byte[1];
        if (b) bytes[0] = 1;
        return write(addr, 1, bytes);
    }

    public static boolean readBoolean(long addr) throws CIdRuntimeException {
        return read(addr, 1)[0] != 0;
    }

    public static long writeLong(long addr, long l) throws CIdRuntimeException {
        byte[] bytes = ByteBuffer.allocate(8).putLong(l).array();
        return write(addr, 8, bytes);
    }

    public static long readLong(long addr) throws CIdRuntimeException {
        byte[] bytes = read(addr, 8);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static void set(long addr, int size, byte b) {
        memoryManager.set((int) addr, size, b);
    }


}
