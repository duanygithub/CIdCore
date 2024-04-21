package net.duany.ciCore.memory;

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
}
