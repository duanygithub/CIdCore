package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.grammar.*;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.symbols.TypeLookup;
import dev.duanyper.cidcore.symbols.Types;
import dev.duanyper.cidcore.symbols.Variables;
import dev.duanyper.cidcore.variable.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class CInterpreter {
    String codes;
    GrammarProc gp;
    Functions functions;

    public CInterpreter() {
    }

    public CInterpreter(Functions functions) {
        this.functions = functions;
    }

    public CInterpreter(String f) throws IOException {
        File file = new File(f);
        if (!file.exists()) {
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

    public void setFunctions(Functions functions) {
        this.functions = functions;
    }

    public int start() {
        if (functions == null)
            functions = new Functions();
        gp = new GrammarProc(functions);
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
        if (functions == null)
            functions = new Functions();
        codes = c;
        gp = new GrammarProc(functions);
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

    public void clear() {
        codes = "";
        gp = null;
        functions = null;
    }

    public void setGrammarProc(GrammarProc grammarProc) {
        gp = grammarProc;
    }

    private void scanFunction() {
        /*
        RootTreeNode root = gp.getRoot();
        for (TreeNode node : root.subNode) {
            if (node.type().equals("Function")) {
                Types keywordType = Types.string2Keywords(gp.originalCodeBlocks.get(node.lIndex));
                String name = gp.originalCodeBlocks.get(node.lIndex + 1);
                Functions.funcList.put(name, keywordType);
                Functions.codeIndex.put(name, (BlockTreeNode) node.subNode.get(1));
                Functions.argIndex.put(name, (ArgTreeNode) node.subNode.get(0));
            }
        }
         */
    }

    public Variable callFunction(String funcName, ValuedArgTreeNode args) throws CIdGrammarException, CIdRuntimeException {
        BlockTreeNode block = functions.codeIndex.get(funcName);
        if (block == null) {
            try {
                Method method = functions.nativeFunctions.get(funcName);
                Variable var = (Variable) method.invoke(Start.class, new Object[]{this, args});
                if (var == null) return CIdVOID.createVOID();
                else return var;
            } catch (Exception e) {
                throw new CIdRuntimeException("无法调用native函数");
            }
        }
        block.vars.vars.clear();
        block.vars.vars.putAll(args.argMap);
        return execBlock(functions.codeIndex.get(funcName));
    }

    public Variable execBlock(BlockTreeNode block) throws CIdGrammarException, CIdRuntimeException {
        if (block == null) return CIdINT.createINT(-1);
        for (TreeNode node : block.subNode) {
            if (gp.codeBlocks.get(node.lIndex).equals("return")) {
                StatementTreeNode statementTreeNode = new StatementTreeNode(node.lIndex + 1, node.rIndex, node.parentNode);
                gp.buildTree(statementTreeNode);
                return calcExpression(/*statementTreeNode*/node.subNode.get(0));
            } else if (node instanceof IfStatementTreeNode) {
                if (calcExpression(node.subNode.get(0)).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.subNode.get(1));
                    if (result.getType() != Types.Void) {
                        return result;
                    }
                }
            } else if (node instanceof WhileTreeNode) {
                while (calcExpression(node.subNode.get(0)).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.subNode.get(1));
                    if (result.getType() != Types.Void) {
                        return result;
                    }
                }
            } else if (node instanceof DoTreeNode) {
                do {
                    Variable result = execBlock((BlockTreeNode) node.subNode.get(1));
                    if (result.getType() != Types.Void) {
                        return result;
                    }
                } while (calcExpression(node.subNode.get(0)).getValue().intValue() != 0);
            } else if (node instanceof ForTreeNode) {
                TreeNode init = node.subNode.get(0).subNode.get(0);
                TreeNode condition = node.subNode.get(0).subNode.get(1);
                TreeNode it = node.subNode.get(0).subNode.get(2);
                calcExpression(init);
                while (calcExpression(condition).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.subNode.get(1));
                    if (result.getType() != Types.Void) {
                        return result;
                    }
                    calcExpression(condition);
                }
            } else calcExpression(node);
        }
        return CIdVOID.createVOID();
    }

    public Variable calcExpression(TreeNode treeNode) throws CIdGrammarException, CIdRuntimeException {
        String exp;
        StringBuilder sb = new StringBuilder();
        for (String tmp : gp.codeBlocks.subList(treeNode.lIndex, treeNode.rIndex)) {
            sb.append(tmp).append(" ");
        }
        exp = sb.toString();
        return calcExpression(exp, treeNode);
    }

    private Variable calcExpression(String exp, TreeNode treeNode) throws CIdGrammarException, CIdRuntimeException {
        Map<String, FunctionCallTreeNode> tempFuncCallMap = new HashMap<>();
        if (treeNode instanceof FunctionCallTreeNode) {
            tempFuncCallMap.put(gp.codeBlocks.get(treeNode.lIndex), (FunctionCallTreeNode) treeNode);
        }
        for (TreeNode node : treeNode.subNode) {
            if (node instanceof FunctionCallTreeNode) {
                tempFuncCallMap.put(gp.codeBlocks.get(node.lIndex), (FunctionCallTreeNode) node);
            }
        }
        List<String> res = MExp2FExp.convert(exp, functions);
        Stack<Variable> stack = new Stack<>();
        for (int i = 0; i < res.size(); i++) {
            String cur = res.get(i);
            if (TypeLookup.lookup(cur, treeNode.vars, functions) == TypeLookup.FUNCTION) {
                FunctionCallTreeNode functionCallTreeNode = tempFuncCallMap.get(cur);
                String funcName = gp.originalCodeBlocks.get(functionCallTreeNode.lIndex);
                ArgTreeNode argTreeNode = functions.argIndex.get(funcName);
                ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
                ArgTreeNode realArgTreeNode = (ArgTreeNode) functionCallTreeNode.subNode.get(0);
                if (realArgTreeNode.lIndex < realArgTreeNode.rIndex) {
                    for (int j = 0; j < realArgTreeNode.subNode.size(); j++) {
                        String argName = "";
                        if (argTreeNode == null) {
                            argName = "%" + j;
                        } else argName = gp.codeBlocks.get(argTreeNode.subNode.get(j).lIndex + 1);
                        valuedArgTreeNode.argMap.put(argName, calcExpression(realArgTreeNode.subNode.get(j)));
                    }
                }
                stack.push(callFunction(funcName, valuedArgTreeNode));
            } else if (cur.matches("(A&)|(A\\*)")) {
                if (cur.equals("A&")) {
                    Variable varOp1 = stack.pop();
                    if (!treeNode.vars.vars.containsValue(varOp1))
                        throw new CIdGrammarException("取地址对象必须为变量");
                    stack.push(CIdPOINTER.createPOINTER(
                            varOp1.getType() == Types.Pointer ? ((CIdPOINTER) varOp1).getLevel() + 1 : 1,
                            varOp1.getAddress(),
                            varOp1.getType() == Types.Pointer ? ((CIdPOINTER) varOp1).getTargetType() : varOp1.getType()
                    ));
                } else if (cur.equals("A*")) {
                    Variable varOp1 = stack.pop();
                    if (varOp1.getType() != Types.Pointer) {
                        throw new CIdGrammarException("取值对象必须为指针变量");
                    }
                    int addr = varOp1.getValue().intValue();
                    CIdPOINTER pointer = (CIdPOINTER) varOp1;
                    if (pointer.getTargetType().equals(Types.Int)) {
                        stack.push(CIdINT.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType().equals(Types.Float)) {
                        stack.push(CIdFLOAT.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType().equals(Types.Char)) {
                        stack.push(CIdCHAR.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType().equals(Types.Boolean)) {
                        stack.push(CIdBOOLEAN.createWithAllocatedAddress(addr));
                    }
                }
            } else if (cur.matches("(\\+)|(-)|(\\*)|(/)|(\\^)|(\\|)|<<|>>|&|")) {
                Variable varOp2 = stack.pop();
                Variable varOp1 = stack.pop();
                if (cur.matches("(\\|)|(>>)|(<<)|&|^")) {
                    if (varOp1.getType() != Types.Int) {
                        throw new CIdGrammarException("位运算的操作数1必须是整数变量或整数常数");
                    }
                    if (varOp2.getType() != Types.Int) {
                        throw new CIdGrammarException("位运算的操作数2必须是整数变量或整数常数");
                    }
                }
                stack.push(varOp1.procOperation(varOp2, cur));
            } else if (cur.matches("(\\+\\+)|(--)|~")) {
                Variable varOp1 = stack.pop();
                if (varOp1.getType() != Types.Int && varOp1.getType() != Types.Pointer) {
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
            } else if (TypeLookup.lookup(cur, treeNode.vars, functions) == TypeLookup.BASICTYPE) {
                if (TypeLookup.lookup(res.get(i + 1), treeNode.vars, functions) != TypeLookup.VARIABLE_FORMAT) continue;
                Variable variable = null;
                switch (cur) {
                    case "int" -> treeNode.vars.vars.put(res.get(i + 1), (variable = CIdINT.createINT()));
                    case "float" -> treeNode.vars.vars.put(res.get(i + 1), (variable = CIdFLOAT.createFLOAT()));
                    case "char" -> treeNode.vars.vars.put(res.get(i + 1), (variable = CIdCHAR.createCHAR()));
                }
                stack.push(variable);
            } else if (TypeLookup.lookup(cur, treeNode.vars, functions) == TypeLookup.DECLEAR_POINTER) {
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
                Types type;
                switch (typeStr) {
                    case "int" -> type = Types.Int;
                    case "float" -> type = Types.Float;
                    case "char" -> type = Types.Char;
                    case "void" -> type = Types.Void;
                    default -> type = null;
                }
                treeNode.vars.vars.put(res.get(i + 1), CIdPOINTER.createPOINTER(pointerLevel, 0, type));
            } else stack.push(string2Variable(cur, treeNode.vars));
        }
        return stack.empty() ? null : stack.pop();
    }

    private Variable string2Variable(String str, Variables vars) {
        switch (TypeLookup.lookup(str, vars, functions)) {
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
        ArrayList<Types> funcArgTypeArray = new ArrayList<>();
        ArrayList<Types> callArgTypeArray = new ArrayList<>();
        return callArg.subNode.size() == funcArg.subNode.size();
    }
    /*
    private Types getExpressionValueType(List<String> expList, Variables tmpVars) {
        Stack<Types> typeStack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for (String s : expList) sb.append(s);
        List<String> resExpList = MExp2FExp.convert(sb.toString(), functions);
        for (int i = 0; i < resExpList.size(); i++) {
            if (MExp2FExp.Operation.getValue(resExpList.get(i)) != 0) {
                switch (resExpList.get(i)) {
                    case "+", "-", "*", "/", "%" -> {
                        Types type2 = typeStack.pop();
                        Types type1 = typeStack.pop();
                        if (type1 == Types.Float || type2 == Types.Float) {
                            typeStack.push(Types.Float);
                        } else if (type1 == Types.Pointer && type2 == Types.Pointer) {
                            typeStack.push(Types.Pointer);
                        } else if (type1 == Types.Int || type2 == Types.Int) {
                            typeStack.push(Types.Int);
                        } else if (type1 == Types.Char || type2 == Types.Int) {
                            typeStack.push(Types.Char);
                        }
                    }
                    case "+=", "-=", "*=", "/=", "%=" -> {
                        Types type1 = typeStack.pop();
                        typeStack.push(type1);
                    }
                    case "|=", "^=", "&=", ">>=", "<<=", "|", "^", "&", "<<", ">>" -> {
                        Types type2 = typeStack.pop();
                        Types type1 = typeStack.pop();
                        typeStack.push(Types.Int);
                    }
                    case "++", "--", "~" -> {
                        Types type1 = typeStack.pop();
                        typeStack.push(Types.Int);
                    }
                    case "A&" -> {
                        Types type1 = typeStack.pop();
                        typeStack.push(Types.Pointer);
                    }
                }
            } else {
                typeStack.push(TypeLookup.lookupKeywords(resExpList.get(i), tmpVars, functions));
            }
        }
        return typeStack.pop();
    }

     */
}
