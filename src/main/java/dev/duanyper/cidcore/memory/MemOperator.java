package dev.duanyper.cidcore.memory;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MemOperator {
    static ArrayList<MemPage> pages = new ArrayList<>();
    static public int allocateMemory(int size) {
        for(int i = 0; i < pages.size(); i++) {
            MemPage page = pages.get(i);
            int index = page.allocateBytes(size);
            if(index == -1)continue;
            int address = i * 65536 + index;
            return address;
        }
        if(pages.size() < 16 * 8) {
            MemPage newPage;
            if(pages.size() == 0) {
                newPage = new MemPage(true);
            }
            else newPage = new MemPage(false);
            pages.add(newPage);
            int index = newPage.allocateBytes(size);
            if(index == -1) {
                return -1;
            }
            int address = (pages.size() - 1) * 65536 + index;
            return address;
        }
        return -1;
    }
    static public byte[] read(int addr, int size) {
        int pageIndex = addr / 65536;
        int index = addr % 65536;
        return pages.get(pageIndex).readBytes(index, size);
    }

    static public int write(int addr, int size, byte[] data) {
        int pageIndex = addr / 65536;
        int index = addr % 65536;
        return pages.get(pageIndex).writeBytes(index, size, data);
    }

    static public void freeMemory(int addr, int size) {
        int pageIndex = addr / 65536;
        int index = addr % 65536;
        pages.get(pageIndex).freeBytes(index, size);
    }

    /*
    static public byte[] float2Bytes(float flt) {
        return ByteBuffer.allocate(4).putFloat(flt).array();
    }

     */
    private static byte[] reverseByteArray(byte[] bytes, int n) {
        byte[] res = new byte[n];
        for (int i = 0; i < n; i++) {
            res[n - 1 - i] = bytes[i];
        }
        return res;
    }

    static public int writeInt(int addr, int i) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
        return write(addr, 4, reverseByteArray(bytes, 4));
    }

    public static int readInt(int addr) {
        byte[] bytes = read(addr, 4);
        byte[] intb = reverseByteArray(bytes, 4);
        return ByteBuffer.wrap(intb).getInt();
    }

    static public int writeFloat(int addr, float f) {
        byte[] bytes = ByteBuffer.allocate(4).putFloat(f).array();
        return write(addr, 4, reverseByteArray(bytes, 4));
    }

    public static float readFloat(int addr) {
        byte[] bytes = read(addr, 4);
        byte[] floatb = reverseByteArray(bytes, 4);
        return ByteBuffer.wrap(floatb).getFloat();
    }

    static public int writeChar(int addr, char c) {
        byte[] bytes = ByteBuffer.allocate(4).putChar(c).array();
        return write(addr, 1, bytes);
    }

    public static char readChar(int addr) {
        return (char) (read(addr, 1))[0];
    }

    public static int writeBoolean(int addr, boolean b) {
        byte[] bytes = new byte[1];
        if (b) bytes[0] = 1;
        return write(addr, 1, bytes);
    }

    public static boolean readBoolean(int addr) {
        return read(addr, 1)[0] != 0;
    }
}
