package net.duany.ciCore;

import net.duany.ciCore.memory.MemOperator;

import java.io.IOException;
import java.util.Arrays;

public class Start {
    public static void main(String[] args) throws IOException {
        CInterpreter ci = new CInterpreter("C:\\Users\\duany\\test.c.txt");
        ci.start();
    }
}
