package dev.duanyper.cidcore.memory;

public interface CIdMemoryManager {
    void init();

    long allocateMemory(int size);

    long allocateMemory(long preferredAddress, int size, int pageProtect);

    byte[] read(long addr, int size);

    int write(long addr, int size, byte[] data);

    void set(long addr, int size, byte b);
}
