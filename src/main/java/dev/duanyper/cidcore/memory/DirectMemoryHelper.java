package dev.duanyper.cidcore.memory;

import dev.duanyper.cidcore.exception.CIdFatalException;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DirectMemoryHelper {
    static Unsafe unsafe = null;
    long address;

    public DirectMemoryHelper() {
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
        commit();
    }

    public void commit() {
        address = unsafe.allocateMemory(4096);
        unsafe.setMemory(address, 1024L, (byte) 0);
    }

    public void free() {
        unsafe.freeMemory(address);
    }
}
