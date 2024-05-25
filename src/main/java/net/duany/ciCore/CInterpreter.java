package net.duany.ciCore;

import net.duany.ciCore.gramma.GrammaProc;
import net.duany.ciCore.gramma.MExp2FExp;
import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;
import net.duany.ciCore.symbols.Variables;
import net.duany.ciCore.variable.CIdINT;
import net.duany.ciCore.variable.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CInterpreter {
    String codes;
    GrammaProc gp;
    public  CInterpreter() {

    }
    public CInterpreter(String f) throws IOException {
        File file = new File(f);
        if(!file.exists()) {
            System.out.println("Cannot read file completely!");
            return;
        }
        byte[] tmp = new byte[(int) file.length() + 1];
        FileInputStream inputStream = new FileInputStream(file);
        int readLen = inputStream.read(tmp);
        if(readLen != file.length()) {
            System.out.println("Cannot read file completely!");
            return;
        }
        codes = new String(tmp);
        codes = codes.trim();
    }
    public int start() {
        gp = new GrammaProc();
        gp.analyze(codes);
        return runCode();
    }
    public int start(String c) {
        codes = c;
        gp = new GrammaProc();
        gp.analyze(codes);
        return runCode();
    }
    private int runCode() {
        Stack<Integer> callDepth = new Stack<>();
        int i = Functions.codesIndex.get("main");
        StringBuilder exp = new StringBuilder();
        do {
            if (gp.codeBlocks.get(i).equals("{")) {
                callDepth.add(0);
            } else if (gp.codeBlocks.get(i).equals("}")) {
                callDepth.pop();
            } else if (gp.codeBlocks.get(i).equals("int")) {
                Variables.vars.put(gp.codeBlocks.get(i + 1), CIdINT.createINT(0));
            } else if (!gp.codeBlocks.get(i).equals(";")) {
                exp.append(gp.codeBlocks.get(i));
            } else {
                calcExpression(exp.toString());
                exp.delete(0, exp.length());
            }
            i++;
        } while(!callDepth.empty());
        return 0;
    }
    private String calcExpression(String exp) {
        List<String> res = MExp2FExp.convert(exp);
        Stack<String> stack = new Stack<>();
        int i = 0;
        stack.push(res.get(i));
        i++;
        while(!stack.empty()) {
            String topEle = stack.peek();
            if(Functions.funcList.get(topEle) != null) {
                //函数调用
                if(Functions.codesIndex.get(topEle) == -1) {
                    stack.pop();
                    String newTop = stack.pop();
                    Variable var = Variables.vars.get(newTop);
                    if(var != null) {
                        Functions.NativeFunction.runNativeFunction_String1(topEle, String.valueOf(var.getValue()));
                    }
                }
            } else if(MExp2FExp.Operation.getValue(topEle) != 0) {
                switch (topEle) {
                    case "=": {
                        stack.pop();
                        String num1 = stack.pop();
                        String num2 = stack.pop();
                        Variable var = Variables.vars.get(num2);
                        if(Variables.vars.get(num2) != null) {
                            if(var.getType().equals(Keywords.Int)) {
                                stack.push(Integer.toString(((CIdINT)var).setValue(Integer.parseInt(num1))));
                            }
                        }
                        break;
                    }
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case "&":
                    case "|":
                    case "^":
                    case ">>":
                    case "<<": {
                        stack.pop();
                        String num1 = stack.pop();
                        String num2 = stack.pop();
                        Variable var = Variables.vars.get(num2);
                        if (Variables.vars.get(num2) != null) {
                            if (var.getType().equals(Keywords.Int)) {
                                stack.push(Integer.toString(((CIdINT) var).procOperation(CIdINT.createINT(num1), topEle).getValue().intValue()));
                            }
                        }
                        break;
                    }
                }
            }
            try{stack.push(res.get(i));}catch (IndexOutOfBoundsException exception){break;}
            i++;
        }
        return stack.empty() ? "" : stack.pop();
    }


}
