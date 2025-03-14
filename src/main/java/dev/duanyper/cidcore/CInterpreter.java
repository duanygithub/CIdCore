package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdFunctionReturnException;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.grammar.*;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.runtime.Environment;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.*;
import dev.duanyper.cidcore.variable.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class CInterpreter {
    static int tot = 0;
    String codes;
    GrammarProc gp;
    Functions functions;

    private CInterpreter(Functions functions) {
        this.functions = functions;
    }

    private CInterpreter(File file) throws IOException {
        if (!file.exists()) {
            System.out.println("Cannot read file completely!");
            return;
        }
        byte[] tmp = new byte[(int) file.length() + 1];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            int readLen = inputStream.read(tmp);
            if (readLen != file.length()) {
                System.out.println("Cannot read file completely!");
                return;
            }
            codes = new String(tmp);
            codes = codes.trim();
        } catch (IOException e) {
        }
    }

    private CInterpreter(String s) {
        codes = s;
    }

    public void setFunctions(Functions functions) {
        this.functions = functions;
    }

    public static CInterpreter create(String file, String codes, Functions functions) {
        if ((file != null && codes != null)) {
            return null;
        }
        CInterpreter ci;
        if (file != null) {
            try {
                ci = new CInterpreter(new File(file));
            } catch (IOException e) {
                ci = null;
            }
        } else if (codes != null) {
            ci = new CInterpreter(codes);
        } else {
            ci = new CInterpreter(functions);
        }
        if (ci != null) {
            ci.setFunctions(functions);
        }
        return ci;
    }

    public static CInterpreter createEmpty() {
        return CInterpreter.create(null, null, null);
    }

    public int start() throws CIdGrammarException {
        if (functions == null)
            functions = new Functions();
        gp = new GrammarProc(functions);
        gp.analyze(codes);
        try {
            scanFunction();
            Variable res = callFunction("main", new ValuedArgTreeNode());
            try {
                return res.getValue().intValue();
            } catch (NullPointerException e) {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int start(String c) throws CIdGrammarException {
        if (functions == null)
            functions = new Functions();
        codes = c;
        gp = new GrammarProc(functions);
        gp.analyze(codes);
        try {
            scanFunction();
            Variable res = callFunction("main", new ValuedArgTreeNode());
            try {
                return (Integer) res.getValue();
            } catch (NullPointerException e) {
                return 0;
            }
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
    }

    public Variable callFunction(String funcName, ValuedArgTreeNode args) throws CIdGrammarException, CIdRuntimeException {
        if (functions.funcList.get(funcName) == null) {
            throw new CIdGrammarException("找不到函数 " + funcName + " 的声明");
        }
        BlockTreeNode block = functions.codeIndex.get(funcName);
        if (block == null) {
            Variable retVal = CIdVOID.createVOID();
            NativeFunctionInterface method = functions.nativeFunctions.get(funcName);
            if (method == null) {
                throw new CIdGrammarException("找不到函数 " + funcName + " 的定义");
            }
            try {
                method.invoke(this, args);
                return CIdVOID.createVOID();
            } catch (CIdFunctionReturnException e) {
                retVal = e.getRetVal();
            }
            return retVal;
        }
        block.vars.clear();
        block.vars.putAll(args.argMap);
        return execBlock(block);
    }

    public Variable execBlock(BlockTreeNode block) throws CIdGrammarException, CIdRuntimeException {
        if (block == null) return CIdINT.createINT(-1);
        for (TreeNode node : block.children) {
            if (gp.codeBlocks.get(node.lIndex).equals("return")) {
                StatementTreeNode statementTreeNode = new StatementTreeNode(node.lIndex + 1, node.rIndex, node.parentNode);
                gp.buildTree(statementTreeNode);
                return calcExpression(/*statementTreeNode*/node.children.get(0));
            } else if (node instanceof IfStatementTreeNode) {
                if (calcExpression(node.children.get(0)).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.children.get(1));
                    if (result.getType() != CIdType.Void) {
                        return result;
                    }
                }
            } else if (node instanceof WhileTreeNode) {
                while (calcExpression(node.children.get(0).children.get(0)).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.children.get(1));
                    if (result.getType() != CIdType.Void) {
                        return result;
                    }
                }
            } else if (node instanceof DoTreeNode) {
                do {
                    Variable result = execBlock((BlockTreeNode) node.children.get(1));
                    if (result.getType() != CIdType.Void) {
                        return result;
                    }
                } while (calcExpression(node.children.get(0)).getValue().intValue() != 0);
            } else if (node instanceof ForTreeNode) {
                TreeNode init = node.children.get(0).children.get(0);
                TreeNode condition = node.children.get(0).children.get(1);
                TreeNode it = node.children.get(0).children.get(2);
                calcExpression(init);
                while (calcExpression(condition).getValue().intValue() != 0) {
                    Variable result = execBlock((BlockTreeNode) node.children.get(1));
                    if (result.getType() != CIdType.Void) {
                        return result;
                    }
                    calcExpression(it);
                }
            } else calcExpression(node);
        }
        return CIdVOID.createVOID();
    }

    public Variable calcExpression(TreeNode treeNode) throws CIdGrammarException, CIdRuntimeException {
        if (treeNode instanceof VarTreeNode) {
            Variable ret = null;
            String typeString = treeNode.codeBlocks.get(treeNode.lIndex);
            for (var statementTreeNode : treeNode.children) {
                String name = statementTreeNode.codeBlocks.get(statementTreeNode.lIndex);
                if (name.equals("*")) {
                    int pointerLevel = 0;
                    for (int i = statementTreeNode.lIndex; statementTreeNode.codeBlocks.get(i).equals("*"); i++) {
                        pointerLevel++;
                    }
                    name = statementTreeNode.codeBlocks.get(statementTreeNode.lIndex + pointerLevel);
                    ret = switch (typeString) {
                        case "int" -> CIdPOINTER.createPOINTER(pointerLevel, 0, CIdType.Int);
                        case "float" -> CIdPOINTER.createPOINTER(pointerLevel, 0, CIdType.Float);
                        case "char" -> CIdPOINTER.createPOINTER(pointerLevel, 0, CIdType.Char);
                        case "bool" -> CIdPOINTER.createPOINTER(pointerLevel, 0, CIdType.Boolean);
                        case "void" -> CIdPOINTER.createPOINTER(pointerLevel, 0, CIdType.Void);
                        default -> throw new IllegalStateException("Unexpected value: " + typeString);
                    };
                    treeNode.vars.put(name, ret);
                    calcExpression(new StatementTreeNode(statementTreeNode.lIndex + pointerLevel, statementTreeNode.rIndex, treeNode));
                } else {
                    ret = switch (typeString) {
                        case "int" -> CIdINT.createINT();
                        case "float" -> CIdFLOAT.createFLOAT();
                        case "char" -> CIdCHAR.createCHAR();
                        case "bool" -> CIdBOOLEAN.createBOOLEAN(false);
                        case "void" -> throw new CIdGrammarException("不可定义void类型变量");
                        default -> throw new IllegalStateException("Unexpected value: " + typeString);
                    };
                    treeNode.vars.put(name, ret);
                    calcExpression(statementTreeNode);
                }
            }
            assert ret != null;
            return ret;
        }
        Map<String, FunctionCallTreeNode> tempFuncCallMap = new HashMap<>();
        Queue<TreeNode> bfs = new ArrayDeque<>();
        bfs.add(treeNode);
        while (!bfs.isEmpty()) {
            TreeNode cur = bfs.poll();
            if (cur instanceof FunctionCallTreeNode) {
                tempFuncCallMap.put(gp.codeBlocks.get(cur.lIndex), (FunctionCallTreeNode) cur);
            }
            bfs.addAll(cur.children);
        }
        List<String> res = ((StatementTreeNode) treeNode).postfixExpression;
        if (res == null) {
            res = ((StatementTreeNode) treeNode).postfixExpression = MExp2FExp.convert(treeNode.lIndex, treeNode.rIndex, new Environment(functions, treeNode.codeBlocks));
        }
        Stack<Variable> stack = new Stack<>();
        for (int i = 0; i < res.size(); i++) {
            String cur = res.get(i);
            if (TypeLookup.lookup(cur, treeNode.vars, functions) == TypeLookup.FUNCTION) {
                FunctionCallTreeNode functionCallTreeNode = tempFuncCallMap.get(cur);
                String funcName = gp.codeBlocks.get(functionCallTreeNode.lIndex);
                ArgTreeNode argTreeNode = functions.argIndex.get(funcName);
                ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
                ArgTreeNode realArgTreeNode = (ArgTreeNode) functionCallTreeNode.children.get(0);
                if (realArgTreeNode.lIndex < realArgTreeNode.rIndex) {
                    for (int j = 0; j < realArgTreeNode.children.size(); j++) {
                        String argName;
                        if (argTreeNode == null) {
                            argName = "%" + j;
                        } else argName = gp.codeBlocks.get(argTreeNode.children.get(j).lIndex + 1);
                        valuedArgTreeNode.argMap.put(argName, stack.pop());
                    }
                }
                stack.push(callFunction(funcName, valuedArgTreeNode));
            } else if (cur.matches("\"([^\"]*)\"")) {
                String str = cur.substring(1, cur.length() - 1);
                str = str.replace("\\n", "\n");
                str = str.replace("\\r", "\r");
                str = str.replace("\\t", "\t");
                str = str.replace("\\0", "\0");
                byte[] strb = {0, 0, 0, 0};
                try {
                    strb = str.getBytes("UTF-32");
                } catch (Exception ignored) {
                }
                long addr = (int) MemOperator.allocateMemory(strb.length + 4);
                MemOperator.set(addr + strb.length, 4, (byte) 0);
                MemOperator.write(addr, strb.length, strb);
                stack.push(CIdPOINTER.createPOINTER(1, addr, CIdType.Char));
            } else if (cur.matches("(A&)|(A\\*)")) {
                if (cur.equals("A&")) {
                    Variable varOp1 = stack.pop();
                    if (!treeNode.vars.containsValue(varOp1))
                        throw new CIdGrammarException("取地址对象必须为变量");
                    stack.push(CIdPOINTER.createPOINTER(
                            varOp1.getType() instanceof CIdPointerType ? ((CIdPOINTER) varOp1).getLevel() + 1 : 1,
                            varOp1.getAddress(),
                            varOp1.getType()
                    ));
                } else if (cur.equals("A*")) {
                    Variable varOp1 = stack.pop();
                    if (!(varOp1.getType() instanceof CIdPointerType)) {
                        throw new CIdGrammarException("取值对象必须为指针变量");
                    }
                    int addr = varOp1.getValue().intValue();
                    CIdPOINTER pointer = (CIdPOINTER) varOp1;
                    if (pointer.getTargetType().equals(CIdType.Int)) {
                        stack.push(CIdINT.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType().equals(CIdType.Float)) {
                        stack.push(CIdFLOAT.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType().equals(CIdType.Char)) {
                        stack.push(CIdCHAR.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType().equals(CIdType.Boolean)) {
                        stack.push(CIdBOOLEAN.createWithAllocatedAddress(addr));
                    } else if (pointer.getTargetType() instanceof CIdPointerType) {
                        stack.push(CIdPOINTER.createWithAllocatedAddress(addr, pointer.getLevel() - 1, ((CIdPointerType) pointer.getTargetType()).type));
                    }
                }
            } else if (cur.equals("sizeof")) {
                Variable varOp1 = stack.pop();
                stack.push(CIdINT.createINT(varOp1.sizeOf()));
            } else if (cur.matches("(\\+)|(-)|(\\*)|(/)|(\\^)|(\\|)|<<|>>|&|")) {
                Variable varOp2 = stack.pop();
                Variable varOp1 = stack.pop();
                if (cur.matches("(\\|)|(>>)|(<<)|&|^")) {
                    if (varOp1.getType() != CIdType.Int) {
                        throw new CIdGrammarException("位运算的操作数1必须是整数变量或整数常数");
                    }
                    if (varOp2.getType() != CIdType.Int) {
                        throw new CIdGrammarException("位运算的操作数2必须是整数变量或整数常数");
                    }
                }
                stack.push(varOp1.procOperation(varOp2, cur));
            } else if (cur.matches("(\\+\\+)|(--)|~")) {
                Variable varOp1 = stack.pop();
                if (varOp1.getType() == CIdType.Int || (varOp1.getType() instanceof CIdPointerType)) {
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
            } /*else if (TypeLookup.lookup(cur, treeNode.vars, functions) == TypeLookup.BASICTYPE) {
                if (TypeLookup.lookup(res.get(i + 1), treeNode.vars, functions) != TypeLookup.VARIABLE_FORMAT) continue;
                Variable variable = null;
                switch (cur) {
                    case "int" -> treeNode.vars.put(res.get(i + 1), (variable = CIdINT.createINT()));
                    case "float" -> treeNode.vars.put(res.get(i + 1), (variable = CIdFLOAT.createFLOAT()));
                    case "char" -> treeNode.vars.put(res.get(i + 1), (variable = CIdCHAR.createCHAR()));
                }
                stack.push(variable);
            } else if (TypeLookup.lookup(cur, treeNode.vars, functions) == TypeLookup.DECLARE_POINTER) {
                CIdPointerType pointerType = CIdType.getPointerType(cur);
                treeNode.vars.put(res.get(i + 1), CIdPOINTER.createPOINTER(pointerType.lvl, 0, pointerType.type));
            }*/ else stack.push(string2Variable(cur, treeNode.vars));
        }
        return stack.empty() ? null : stack.pop();
    }

    private Variable string2Variable(String str, Variables vars) throws CIdGrammarException {
        switch (TypeLookup.lookup(str, vars, functions)) {
            case TypeLookup.INTEGER -> {
                return CIdINT.createINT(str);
            }
            case TypeLookup.FLOAT -> {
                return CIdFLOAT.createFLOAT(str);
            }
            case TypeLookup.VARIABLE -> {
                return vars.get(str);
            }
            case TypeLookup.BOOLEAN -> {
                return CIdBOOLEAN.createBOOLEAN(str.equals("true"));
            }
            default -> {
                throw new CIdGrammarException("未声明的符号: " + str);
            }
        }
    }

    private boolean checkArg(ArgTreeNode callArg, ArgTreeNode funcArg) {
        ArrayList<CIdType> funcArgTypeArray = new ArrayList<>();
        ArrayList<CIdType> callArgTypeArray = new ArrayList<>();
        return callArg.children.size() == funcArg.children.size();
    }
}
