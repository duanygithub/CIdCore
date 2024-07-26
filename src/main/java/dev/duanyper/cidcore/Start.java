package dev.duanyper.cidcore;

import dev.duanyper.cidcore.grammar.MExp2FExp;

import java.io.IOException;
import java.util.List;

public class Start {
    static public List<String> codeBlocks;

    public static void main(String[] args) throws IOException {
        //DEBUG ONLY!!!!
        String str = "int f(int* pointer, int n){ return *pointer += n; } int main(){ int a = 666; int b = 4; f(&a, b); return a;}";

        CInterpreter cInterpreter = new CInterpreter(str, false);
        System.out.println(cInterpreter.start());
    }
}