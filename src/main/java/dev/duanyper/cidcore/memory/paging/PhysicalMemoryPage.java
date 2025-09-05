package dev.duanyper.cidcore.memory.paging;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import static dev.duanyper.cidcore.memory.paging.MathHelper.divide4096;

public class PhysicalMemoryPage {
    ByteBuffer buffer;
    int pageIndex;
    int totPage = 0;

    LinkedList<VirtualMemoryPage> linkedVirtualPages;

    public PhysicalMemoryPage() {
        if (totPage >= divide4096(PagingMemoryManager.maxMemorySize)) {
            throw new OutOfMemoryError();
        }
        pageIndex = totPage;
        totPage++;
        linkedVirtualPages = new LinkedList<>();
        commit();
    }

    public void commit() {
        buffer = ByteBuffer.allocateDirect(4096);
        buffer.order(ByteOrder.nativeOrder());
        byte[] zeros = new byte[4096];
        buffer.put(zeros);
        buffer.position(0);
    }

    public void free() {
        buffer = null;
    }

    public byte[] read(long addr, int size) {
        if (size > 4096) {
            throw new IndexOutOfBoundsException(addr);
        }
        byte[] ret = new byte[size];
        int position = buffer.position();
        buffer.position((int) addr);
        buffer.get(ret, 0, size);
        buffer.position(position);
        return ret;
    }

    public void write(long addr, byte[] data, int size) {
        if (addr > 4096) {
            throw new IndexOutOfBoundsException(addr);
        }
        int position = buffer.position();
        buffer.position((int) addr);
        buffer.put(data, 0, size);
        buffer.position(position);
    }
}
