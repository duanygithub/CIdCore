package dev.duanyper.cidcore;

import dev.duanyper.cidcore.wrapper.CIdShell;

import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            CIdShell.loop();
            return;
        }
        CInterpreter interpreter = CInterpreter.create(args[0], null, null);
    }
}