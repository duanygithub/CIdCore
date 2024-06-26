package net.duany.ciCore;

import net.duany.ciCore.exception.CIdAssert;
import net.duany.ciCore.exception.CIdGrammarException;
import net.duany.ciCore.gramma.*;
import net.duany.ciCore.runtime.ValuedArgTreeNode;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;
import net.duany.ciCore.symbols.TypeLookup;
import net.duany.ciCore.symbols.Variables;
import net.duany.ciCore.variable.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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
        if (readLen != file.length()) {
            System.out.println("Cannot read file completely!");
            return;
        }
        codes = new String(tmp);
        codes = codes.trim();
    }

    public CInterpreter(String s, boolean dummy) {
        codes = s;
    }

    public int start() {
        gp = new GrammarProc();
        gp.analyze(codes);
        try {
            scanFunction();
            Variable res = callFunction("main", new ValuedArgTreeNode());
            return res.getValue().intValue();
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
            scanFunction();
            Variable res = callFunction("main", new ValuedArgTreeNode());
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
                Keywords keywordType = Keywords.string2Keywords(gp.originalCodeBlocks.get(node.lIndex));
                String name = gp.originalCodeBlocks.get(node.lIndex + 1);
                Functions.funcList.put(name, keywordType);
                Functions.codeIndex.put(name, (BlockTreeNode) node.subNode.get(1));
                Functions.argIndex.put(name, (ArgTreeNode) node.subNode.get(0));
            } else if (node.type().equals("funcCall")) {
                String funcIdentifyString = null;
                for (Map.Entry<String, FunctionCallTreeNode> entry : Functions.funcCallIdentifyMap.entrySet()) {
                    if (entry.getValue().equals(node)) {
                        funcIdentifyString = entry.getKey();
                        break;
                    }
                }
                for (int i = 0; i < node.rIndex - node.lIndex; i++) {
                    gp.codeBlocks.remove(node.lIndex);
                }
                gp.codeBlocks.add(node.lIndex, funcIdentifyString);
            }
        }
    }

    public Variable callFunction(String funcName, ValuedArgTreeNode args) throws CIdGrammarException {
        BlockTreeNode block = Functions.codeIndex.get(funcName);
        block.vars.vars = new HashMap<>(args.vars.vars);
        return execBlock(Functions.codeIndex.get(funcName));
    }

    public Variable execBlock(BlockTreeNode block) throws CIdGrammarException {
        if (block == null) return CIdINT.createINT(-1);
        for (TreeNode node : block.subNode) {
            if (gp.originalCodeBlocks.get(node.lIndex).equals("return")) {
                return calcExpression(new StatementTreeNode(node.lIndex + 1, node.rIndex, node));
            } else if (node.type().equals("if")) {
                if (calcExpression(node.subNode.get(0)).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.subNode.get(1));
                    if (result.getType() != Keywords.Void) {
                        return result;
                    }
                }
            } else if (node.type().equals("while")) {
                while (calcExpression(node.subNode.get(0)).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.subNode.get(1));
                    if (result.getType() != Keywords.Void) {
                        return result;
                    }
                }
            } else calcExpression(node);
        }
        return CIdVOID.createVOID();
    }

    private Variable calcExpression(TreeNode treeNode) throws CIdGrammarException {
        String exp;
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        for (TreeNode subNode : treeNode.subNode) {
            if (subNode.type().equals("funcCall")) {
                offset += (subNode.rIndex - subNode.lIndex - 1);
            }
        }
        for (String tmp : gp.codeBlocks.subList(treeNode.lIndex, treeNode.rIndex - offset)) {
            sb.append(tmp).append(" ");
        }
        exp = sb.toString();
        return calcExpression(exp, treeNode);
    }

    private Variable calcExpression(String exp, TreeNode treeNode) throws CIdGrammarException {
        List<String> res = MExp2FExp.convert(exp);
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < res.size(); i++) {
            String cur = res.get(i);
            if (TypeLookup.lookup(cur, treeNode.vars) == TypeLookup.FUNCTION_CALL) {
                FunctionCallTreeNode functionCallTreeNode = Functions.funcCallIdentifyMap.get(cur);
                String funcName = gp.originalCodeBlocks.get(functionCallTreeNode.lIndex);
                ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
                for (TreeNode node : functionCallTreeNode.subNode) {
                    valuedArgTreeNode.vars.vars.put("", calcExpression(node));
                }
                stack.push(callFunction(funcName, valuedArgTreeNode).toString());
            }
            if (cur.matches("(\\+)|(-)|(\\*)|(/)|(\\^)|(\\|)|<<|>>|&|")) {
                String strOp2 = stack.pop();
                String strOp1 = stack.pop();
                if (cur.matches("(\\|)|(>>)|(<<)|&|^")) {
                    if (TypeLookup.lookup(strOp1, treeNode.vars) != TypeLookup.VARIABLE &&
                            TypeLookup.lookup(strOp1, treeNode.vars) != TypeLookup.INTEGER) {
                        throw new CIdGrammarException("位运算的操作数1必须是整数变量或整数常数");
                    }
                    if (TypeLookup.lookup(strOp2, treeNode.vars) != TypeLookup.VARIABLE &&
                            TypeLookup.lookup(strOp2, treeNode.vars) != TypeLookup.INTEGER) {
                        throw new CIdGrammarException("位运算的操作数2必须是整数变量或整数常数");
                    }
                }
                Variable ans = string2Variable(strOp1, treeNode.vars).procOperation(string2Variable(strOp2, treeNode.vars), cur);
                stack.push(ans.toString());
            } else if (cur.matches("(\\+\\+)|(--)|~")) {
                String strOp1 = stack.pop();
                if (TypeLookup.lookup(strOp1, treeNode.vars) == TypeLookup.VARIABLE &&
                        TypeLookup.lookupKeywords(strOp1, treeNode.vars) == Keywords.Int) {
                    stack.push(string2Variable(strOp1, treeNode.vars).procOperation(null, cur).toString());
                }
            } else if (cur.matches(">|<|>=|<=|==")) {
                Variable var2 = string2Variable(stack.pop(), treeNode.vars);
                Variable var1 = string2Variable(stack.pop(), treeNode.vars);
                int cmpResult = var1.cmp(var2);
                switch (cmpResult) {
                    case 1 -> {
                        //var1 小于 var2
                        if (cur.matches("<|(<=)")) {
                            stack.push("1");
                        } else stack.push("0");
                    }
                    case -1 -> {
                        if (cur.matches(">|(>=)")) {
                            stack.push("1");
                        } else stack.push("0");
                    }
                    case 0 -> {
                        if (cur.matches("(>=)|(<=)|(==)")) {
                            stack.push("1");
                        } else stack.push("0");
                    }
                }
            } else if (MExp2FExp.Operation.getValue(cur) != 0) {
                String strOp2 = stack.pop();
                String strOp1 = stack.pop();
                stack.push(string2Variable(strOp1, treeNode.vars).procOperation(string2Variable(strOp2, treeNode.vars), cur).toString());
            } else if (TypeLookup.lookup(cur, treeNode.vars) == TypeLookup.BASICTYPE) {
                if (TypeLookup.lookup(res.get(i + 1), treeNode.vars) != TypeLookup.VARIABLE_FORMAT) continue;
                switch (cur) {
                    case "int" -> {
                        treeNode.vars.vars.put(res.get(i + 1), CIdINT.createINT());
                    }
                    case "float" -> {
                        treeNode.vars.vars.put(res.get(i + 1), CIdFLOAT.createFLOAT());
                    }
                    case "char" -> {
                        treeNode.vars.vars.put(res.get(i + 1), CIdCHAR.createCHAR());
                    }
                }
            } else stack.push(res.get(i));
        }
        return stack.empty() ? null : string2Variable(stack.pop(), treeNode.vars);
    }

    private Variable string2Variable(String str, Variables vars) {
        switch (TypeLookup.lookup(str, vars)) {
            case TypeLookup.INTEGER -> {
                return CIdINT.createINT(str);
            }
            case TypeLookup.FLOAT -> {
                return CIdFLOAT.createFLOAT(str);
            }
            case TypeLookup.VARIABLE -> {
                return vars.vars.get(str);
            }
            default -> {
                return null;
            }
        }
    }

    private boolean checkArg(ArgTreeNode callArg, ArgTreeNode funcArg) {
        ArrayList<Keywords> funcArgTypeArray = new ArrayList<>();
        ArrayList<Keywords> callArgTypeArray = new ArrayList<>();
        return callArg.subNode.size() == funcArg.subNode.size();
    }

    private Keywords getExpressionValueType(List<String> expList, Variables tmpVars) {
        Stack<Keywords> typeStack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for (String s : expList) sb.append(s);
        List<String> resExpList = MExp2FExp.convert(sb.toString());
        for (int i = 0; i < resExpList.size(); i++) {
            if (MExp2FExp.Operation.getValue(resExpList.get(i)) != 0) {
                switch (resExpList.get(i)) {
                    case "+", "-", "*", "/", "%" -> {
                        Keywords type2 = typeStack.pop();
                        Keywords type1 = typeStack.pop();
                        if (type1 == Keywords.Float || type2 == Keywords.Float) {
                            typeStack.push(Keywords.Float);
                        } else if (type1 == Keywords.Pointer && type2 == Keywords.Pointer) {
                            typeStack.push(Keywords.Pointer);
                        } else if (type1 == Keywords.Int || type2 == Keywords.Int) {
                            typeStack.push(Keywords.Int);
                        } else if (type1 == Keywords.Char || type2 == Keywords.Int) {
                            typeStack.push(Keywords.Char);
                        }
                    }
                    case "+=", "-=", "*=", "/=", "%=" -> {
                        Keywords type1 = typeStack.pop();
                        typeStack.push(type1);
                    }
                    case "|=", "^=", "&=", ">>=", "<<=", "|", "^", "&", "<<", ">>" -> {
                        Keywords type2 = typeStack.pop();
                        Keywords type1 = typeStack.pop();
                        typeStack.push(Keywords.Int);
                    }
                    case "++", "--", "~" -> {
                        Keywords type1 = typeStack.pop();
                        typeStack.push(Keywords.Int);
                    }
                    case "A&" -> {
                        Keywords type1 = typeStack.pop();
                        typeStack.push(Keywords.Pointer);
                    }
                }
            } else {
                typeStack.push(TypeLookup.lookupKeywords(resExpList.get(i), tmpVars));
            }
        }
        return typeStack.pop();
    }


}
