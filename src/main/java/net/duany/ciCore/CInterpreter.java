package net.duany.ciCore;

import net.duany.ciCore.exception.CIdAssert;
import net.duany.ciCore.exception.CIdGrammarException;
import net.duany.ciCore.gramma.*;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;
import net.duany.ciCore.symbols.TypeLookup;
import net.duany.ciCore.symbols.Variables;
import net.duany.ciCore.variable.CIdINT;
import net.duany.ciCore.variable.Variable;

import java.io.*;
import java.util.*;

public class CInterpreter {
    String codes;
    GrammarProc gp;
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
        gp = new GrammarProc();
        gp.analyze(codes);
        try {
            Variable res = callFunction("main", Functions.argIndex.get("main"));
            return (Integer) res.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    public int start(String c) {
        codes = c;
        gp = new GrammarProc();
        gp.analyze(codes);
        try {
            Variable res = callFunction("main", Functions.argIndex.get("main"));
            return (Integer) res.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void scanFunction() {
        RootTreeNode root = gp.getRoot();
        for (TreeNode node : root.subNode) {
            if (node.type().equals("Function")) {
                Keywords keywordType = Keywords.string2Keywords(gp.codeBlocks.get(node.lIndex));
                String name = gp.codeBlocks.get(node.lIndex + 1);
                Functions.funcList.put(name, keywordType);
                Functions.codeIndex.put(name, (BlockTreeNode) node.subNode.get(1));
                Functions.argIndex.put(name, (ArgTreeNode) node.subNode.get(0));
            }
        }
    }

    private Variable callFunction(String funcName, ArgTreeNode args) throws CIdGrammarException {
        BlockTreeNode funcBlock = Functions.codeIndex.get(funcName);
        if (funcBlock == null) return CIdINT.createINT(-1);
        ArgTreeNode funcArgBlock = Functions.argIndex.get(funcName);
        Queue<TreeNode> treeQueue = new LinkedList<>();
        treeQueue.add(funcBlock);
        TreeNode cur;
        while (!treeQueue.isEmpty()) {
            cur = treeQueue.poll();
            treeQueue.addAll(cur.subNode);
            String type = cur.type();
            int l = cur.lIndex;
            int r = cur.rIndex;
            switch (type) {
                case "funcCall" -> {
                    String calledFuncName = gp.codeBlocks.get(l);
                    ArgTreeNode arg = (ArgTreeNode) cur.subNode.get(0);
                    if (Functions.funcList.get(calledFuncName) != null) {
                        callFunction(calledFuncName, arg);
                    }

                }
            }
        }
        return CIdINT.createINT(0);
    }

    /*
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
    */
    private Variable calcExpression(TreeNode treeNode) {
        String exp;
        StringBuilder sb = new StringBuilder();
        for (String tmp : gp.codeBlocks.subList(treeNode.lIndex, treeNode.rIndex)) {
            sb.append(tmp);
        }
        exp = sb.toString();
        return calcExpression(exp, treeNode);
    }

    private Variable calcExpression(String exp, TreeNode treeNode) {
        List<String> res = MExp2FExp.convert(exp);
        Stack<String> stack = new Stack<>();
        int i = 0;
        stack.push(res.get(i));
        i++;
        while (!stack.empty()) {
            String topEle = stack.peek();
            if (Functions.funcList.get(topEle) != null) {
                //函数调用
                if (Functions.codeIndex.get(topEle) == null) {
                    stack.pop();
                    String newTop = stack.pop();
                    Variable var = treeNode.vars.vars.get(newTop);
                    if (var != null) {
                        Functions.NativeFunction.runNativeFunction_String1(topEle, String.valueOf(var.getValue()));
                    }
                }
            } else if(MExp2FExp.Operation.getValue(topEle) != 0) {
                switch (topEle) {
                    case "=": {
                        stack.pop();
                        String num1 = stack.pop();
                        String num2 = stack.pop();
                        Variable var = treeNode.vars.vars.get(num2);
                        if (treeNode.vars.vars.get(num2) != null) {
                            if (var.getType().equals(Keywords.Int)) {
                                stack.push(Integer.toString(((CIdINT) var).setValue(Integer.parseInt(num1))));
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
                        Variable var = treeNode.vars.vars.get(num2);
                        if (treeNode.vars.vars.get(num2) != null) {
                            if (var.getType().equals(Keywords.Int)) {
                                stack.push(Integer.toString(((CIdINT) var).procOperation(CIdINT.createINT(num1), topEle).getValue().intValue()));
                            }
                        }
                        break;
                    }
                }
            }
            try {
                stack.push(res.get(i));
            } catch (IndexOutOfBoundsException exception) {
                break;
            }
            i++;
        }
        return stack.empty() ? null : string2Variable(stack.pop());
    }

    private Variable string2Variable(String str) {
        switch (TypeLookup.lookup(str)) {
            case TypeLookup.INTEGER -> {
                return CIdINT.createINT(str);
            }
            default -> {
                return null;
            }
        }
    }

}
