package dev.duanyper.cidcore;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            DataInputStream cout = new DataInputStream(System.in);
            String code = cout.readLine();
            CInterpreter ci = new CInterpreter(code, false);
            int retVal = ci.start();
            System.out.println();
            System.out.println("***程序返回 " + retVal + " ***");
            return;
        }
        File codeFile = new File(args[0]);
        if (codeFile.exists()) {
            CInterpreter ci = new CInterpreter(args[0]);
            int retVal = ci.start();
            System.out.println();
            System.out.println("***程序返回 " + retVal + " .");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String argString : args) {
                sb.append(argString).append(' ');
            }
            CInterpreter ci = new CInterpreter(sb.toString(), false);
            int retVal = ci.start();
            System.out.println();
            System.out.println("***程序返回 " + retVal + " .");
        }
    }
}