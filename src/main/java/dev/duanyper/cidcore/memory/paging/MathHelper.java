package dev.duanyper.cidcore.memory.paging;

public class MathHelper {
    public static long round4096(long n) {
        return n & 4294963200L;
    }

    public static long divide4096(long n) {
        return n >> 12;
    }

    public static long mod4096(long n) {
        return n & 4095;
    }
}
