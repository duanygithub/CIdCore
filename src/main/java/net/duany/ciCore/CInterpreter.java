package net.duany.ciCore;

import net.duany.ciCore.exception.CIdAssert;
import net.duany.ciCore.exception.CIdGrammarException;
import net.duany.ciCore.gramma.*;
import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.runtime.ValuedArgTreeNode;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;
import net.duany.ciCore.symbols.TypeLookup;
import net.duany.ciCore.symbols.Variables;
import net.duany.ciCore.variable.*;

import java.io.*;
import java.security.Key;
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

    public void setGrammarProc(GrammarProc grammarProc) {
        gp = grammarProc;
    }

    private void scanFunction() {
        /*
        RootTreeNode root = gp.getRoot();
        for (TreeNode node : root.subNode) {
            if (node.type().equals("Function")) {
                Keywords keywordType = Keywords.string2Keywords(gp.originalCodeBlocks.get(node.lIndex));
                String name = gp.originalCodeBlocks.get(node.lIndex + 1);
                Functions.funcList.put(name, keywordType);
                Functions.codeIndex.put(name, (BlockTreeNode) node.subNode.get(1));
                Functions.argIndex.put(name, (ArgTreeNode) node.subNode.get(0));
            }
        }
         */
    }

    public Variable callFunction(String funcName, ValuedArgTreeNode args) throws CIdGrammarException {
        BlockTreeNode block = Functions.codeIndex.get(funcName);
        block.vars.vars.clear();
        block.vars.vars.putAll(args.argMap);
        return execBlock(Functions.codeIndex.get(funcName));
    }

    public Variable execBlock(BlockTreeNode block) throws CIdGrammarException {
        if (block == null) return CIdINT.createINT(-1);
        for (TreeNode node : block.subNode) {
            if (gp.codeBlocks.get(node.lIndex).equals("return")) {
                StatementTreeNode statementTreeNode = new StatementTreeNode(node.lIndex + 1, node.rIndex, node.parentNode);
                gp.buildTree(statementTreeNode);
                return calcExpression(/*statementTreeNode*/node.subNode.get(0));
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

            }
        }
        for (String tmp : gp.codeBlocks.subList(treeNode.lIndex, treeNode.rIndex - offset)) {
            sb.append(tmp).append(" ");
        }
        exp = sb.toString();
        return calcExpression(exp, treeNode);
    }

    private Variable calcExpression(String exp, TreeNode treeNode) throws CIdGrammarException {
        Map<String, FunctionCallTreeNode> tempFuncCallMap = new HashMap<>();
        for (TreeNode node : treeNode.subNode) {
            if (node.type().equals("funcCall")) {
                tempFuncCallMap.put(gp.codeBlocks.get(node.lIndex), (FunctionCallTreeNode) node);
            }
        }
        List<String> res = MExp2FExp.convert(exp);
        Stack<Variable> stack = new Stack<>();
        for (int i = 0; i < res.size(); i++) {
            String cur = res.get(i);
            if (TypeLookup.lookup(cur, treeNode.vars) == TypeLookup.FUNCTION) {
                FunctionCallTreeNode functionCallTreeNode = tempFuncCallMap.get(cur);
                String funcName = gp.originalCodeBlocks.get(functionCallTreeNode.lIndex);
                ArgTreeNode argTreeNode = Functions.argIndex.get(funcName);
                ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
                ArgTreeNode realArgTreeNode = (ArgTreeNode) functionCallTreeNode.subNode.get(0);
                if (realArgTreeNode.lIndex < realArgTreeNode.rIndex - 1) {
                    if (realArgTreeNode.subNode.size() == 0) {
                        valuedArgTreeNode.argMap.put(gp.codeBlocks.get(argTreeNode.lIndex + 1), calcExpression(realArgTreeNode));
                    } else {
                        for (int j = 0; j < realArgTreeNode.subNode.size(); j++) {
                            valuedArgTreeNode.argMap.put(gp.codeBlocks.get(argTreeNode.subNode.get(j).lIndex + 1), calcExpression(realArgTreeNode.subNode.get(j)));
                        }
                    }
                }
                stack.push(callFunction(funcName, valuedArgTreeNode));
            } else if (cur.matches("(A&)|(A\\*)")) {
                if (cur.equals("A&")) {
                    Variable varOp1 = stack.pop();
                    if (!treeNode.vars.vars.containsValue(varOp1))
                        throw new CIdGrammarException("取地址对象必须为变量");
                    stack.push(CIdPOINTER.createPOINTER(
                            varOp1.getType() == Keywords.Pointer ? ((CIdPOINTER) varOp1).getLevel() + 1 : 1,
                            varOp1.getAddress(),
                            varOp1.getType() == Keywords.Pointer ? ((CIdPOINTER) varOp1).getTargetType() : varOp1.getType()
                    ));
                } else if (cur.equals("A*")) {
                    Variable varOp1 = stack.pop();
                    if (varOp1.getType() != Keywords.Pointer) {
                        throw new CIdGrammarException("取值对象必须为指针变量");
                    }
                    int addr = varOp1.getValue().intValue();
                    CIdPOINTER pointer = (CIdPOINTER) varOp1;
                    if (pointer.getTargetType().equals(Keywords.Int)) {
                        stack.push(CIdINT.createINT(MemOperator.readInt(addr)));
                    } else if (pointer.getTargetType().equals(Keywords.Float)) {
                        stack.push(CIdFLOAT.createFLOAT(MemOperator.readFloat(addr)));
                    } else if (pointer.getTargetType().equals(Keywords.Char)) {
                        stack.push(CIdCHAR.createCHAR(MemOperator.readChar(addr)));
                    } else if (pointer.getTargetType().equals(Keywords.Boolean)) {
                        stack.push(CIdBOOLEAN.createBOOLEAN(MemOperator.readBoolean(addr)));
                    }
                }
            } else if (cur.matches("(\\+)|(-)|(\\*)|(/)|(\\^)|(\\|)|<<|>>|&|")) {
                Variable varOp2 = stack.pop();
                Variable varOp1 = stack.pop();
                if (cur.matches("(\\|)|(>>)|(<<)|&|^")) {
                    if (varOp1.getType() != Keywords.Int) {
                        throw new CIdGrammarException("位运算的操作数1必须是整数变量或整数常数");
                    }
                    if (varOp2.getType() != Keywords.Int) {
                        throw new CIdGrammarException("位运算的操作数2必须是整数变量或整数常数");
                    }
                }
                stack.push(varOp1.procOperation(varOp2, cur));
            } else if (cur.matches("(\\+\\+)|(--)|~")) {
                Variable varOp1 = stack.pop();
                if (varOp1.getType() != Keywords.Int && varOp1.getType() != Keywords.Pointer) {
                    stack.push(varOp1.procOperation(null, cur));
                }
            } else if (cur.matches(">|<|>=|<=|==")) {
                Variable var2 = stack.pop();
                Variable var1 = stack.pop();
                int cmpResult = var1.cmp(var2);
                switch (cmpResult) {
                    case 1 -> {
                        //var1 小于 var2
                        if (cur.matches("<|(<=)")) {
                            stack.push(CIdBOOLEAN.createBOOLEAN(true));
                        } else stack.push(CIdBOOLEAN.createBOOLEAN(false));
                    }
                    case -1 -> {
                        if (cur.matches(">|(>=)")) {
                            stack.push(CIdBOOLEAN.createBOOLEAN(true));
                        } else stack.push(CIdBOOLEAN.createBOOLEAN(false));
                    }
                    case 0 -> {
                        if (cur.matches("(>=)|(<=)|(==)")) {
                            stack.push(CIdBOOLEAN.createBOOLEAN(true));
                        } else stack.push(CIdBOOLEAN.createBOOLEAN(false));
                    }
                }
            } else if (MExp2FExp.Operation.getValue(cur) != 0) {
                Variable varOp2 = stack.pop();
                Variable varOp1 = stack.pop();
                stack.push(varOp1.procOperation(varOp2, cur));
            } else if (TypeLookup.lookup(cur, treeNode.vars) == TypeLookup.BASICTYPE) {
                if (TypeLookup.lookup(res.get(i + 1), treeNode.vars) != TypeLookup.VARIABLE_FORMAT) continue;
                Variable variable = null;
                switch (cur) {
                    case "int" -> treeNode.vars.vars.put(res.get(i + 1), (variable = CIdINT.createINT()));
                    case "float" -> treeNode.vars.vars.put(res.get(i + 1), (variable = CIdFLOAT.createFLOAT()));
                    case "char" -> treeNode.vars.vars.put(res.get(i + 1), (variable = CIdCHAR.createCHAR()));
                }
                stack.push(variable);
            } else if (TypeLookup.lookup(cur, treeNode.vars) == TypeLookup.DECLEAR_POINTER) {
                int pointerLevel = 0, pointerBegin = 0;
                for (int j = 0; j < cur.length(); j++) {
                    if (cur.charAt(j) == '*') {
                        if (pointerBegin == 0) {
                            pointerBegin = j;
                        }
                        pointerLevel++;
                    }
                }
                String typeStr = cur.substring(0, pointerBegin);
                Keywords type;
                switch (typeStr) {
                    case "int" -> type = Keywords.Int;
                    case "float" -> type = Keywords.Float;
                    case "char" -> type = Keywords.Char;
                    case "void" -> type = Keywords.Void;
                    default -> type = null;
                }
                treeNode.vars.vars.put(res.get(i + 1), CIdPOINTER.createPOINTER(pointerLevel, 0, type));
            } else stack.push(string2Variable(cur, treeNode.vars));
        }
        return stack.empty() ? null : stack.pop();
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
            case TypeLookup.BOOLEAN -> {
                return CIdBOOLEAN.createBOOLEAN(str.equals("true"));
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
