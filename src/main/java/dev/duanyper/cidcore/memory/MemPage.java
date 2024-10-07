package dev.duanyper.cidcore.memory;

import dev.duanyper.cidcore.exception.CIdRuntimeException;

public class MemPage {
    byte[] page = new byte[65536];
    byte[] allocMap = new byte[8192];
    int bytesAllocated = 0;
    public MemPage(boolean bDisableFirst4KB) {
        if(bDisableFirst4KB) {
            for(int i = 0; i < 0x1000 / 8; i++) {
                allocMap[i] = (byte)0xFF;
            }
            bytesAllocated += 0x1000;
        }
    }
    public int getBytesAllocated() {return bytesAllocated;}
    public int getFreeBytesCount() {return 65536 - bytesAllocated;}
    public int allocateBytes(int size) {
        if(getFreeBytesCount() < size) {
            return -1;
        }
        for(int i = 0; i < 65536 - size; i += size) {
            if(i % size != 0)continue;
            boolean bAvailable = true;
            for(int j = 0; j < size; j++) {
                int mapIndex = (i + j) / 8;
                int curAllocStatus = allocMap[mapIndex] & (1 << ((i + j) % 8));
                if(curAllocStatus != 0) {
                    bAvailable = false;
                }
            }
            if(bAvailable) {
                for(int j = 0; j < size; j++) {
                    int mapIndex = (i + j) / 8;
                    allocMap[mapIndex] |= (byte) (1 << ((i + j) % 8));
                }
                bytesAllocated += size;
                return i;
            }
        }
        return -1;
    }

    public byte[] readBytes(int index, int size) throws CIdRuntimeException {
        for(int i = 0; i < size; i++) {
            int mapIndex = (index + i) / 8;
            if ((allocMap[mapIndex] & (1 << ((index + i) % 8))) == 0) {
                throw new CIdRuntimeException("无效的内存访问");
            }
        }
        byte[] data = new byte[size];
        System.arraycopy(page, index, data, 0, size);
        return data;
    }

    public int writeBytes(int index, int size, byte[] data) throws CIdRuntimeException {
        for(int i = 0; i < size; i++) {
            int mapIndex = (index + i) / 8;
            if ((allocMap[mapIndex] & (1 << ((index + i) % 8))) == 0) {
                throw new CIdRuntimeException("无效的内存访问");
            }
        }
        if (size >= 0) System.arraycopy(data, 0, page, index, size);
        return 0;
    }
    public void freeBytes(int index, int size) {
        for(int i = 0; i < size; i++) {
            int mapIndex = (index + i) / 8;
            allocMap[mapIndex] &= (byte) ~(1 << ((index + i) % 8));
        }
    }
}
