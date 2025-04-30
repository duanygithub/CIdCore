package dev.duanyper.cidcore;

import dev.duanyper.cidcore.exception.CIdFunctionReturnSignal;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
            } catch (CIdFunctionReturnSignal e) {
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
                return calcExpression(node.children.get(0));
            } else if (node instanceof IfStatementTreeNode) {
                if (calcExpression(node.children.get(0).children.get(0)).getValue().intValue() != 0) {
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
                if (!init.children.isEmpty() && init.children.get(0) instanceof VarTreeNode)
                    calcExpression(init.children.get(0));
                else calcExpression(init);
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
                int pointerLevel = 0;
                for (int i = statementTreeNode.lIndex; statementTreeNode.codeBlocks.get(i).equals("*"); i++) {
                    pointerLevel++;
                }
                String name = statementTreeNode.codeBlocks.get(statementTreeNode.lIndex + pointerLevel);
                if (pointerLevel > 0
                        || (statementTreeNode.lIndex + pointerLevel + 1 < statementTreeNode.rIndex
                        && statementTreeNode.codeBlocks.get(statementTreeNode.lIndex + pointerLevel + 1).equals("["))
                ) {
                    long pAddress = 0;
                    boolean isArray = false;
                    if (statementTreeNode.codeBlocks.get(statementTreeNode.lIndex + pointerLevel + 1).equals("[")) {
                        try {
                            int count = Integer.parseInt(statementTreeNode.codeBlocks.get(statementTreeNode.lIndex + pointerLevel + 2));
                            if (count <= 0) throw new NumberFormatException();
                            pAddress = MemOperator.getPool().allocateMemory(count * CIdType.getSize(typeString));
                            isArray = true;
                            if (!statementTreeNode.children.isEmpty()) {
                                long i = pAddress;
                                for (TreeNode initExpression : statementTreeNode.children.get(0).children) {
                                    Variable result = calcExpression(initExpression);
                                    byte[] data = MemOperator.read(result.getAddress(), result.sizeOf());
                                    MemOperator.write(i, CIdType.getSize(typeString), data);
                                    i += CIdType.getSize(typeString);
                                }
                            }
                            pointerLevel++;
                        } catch (NumberFormatException e) {
                            throw new CIdGrammarException("数组元素个数必须为正整数");
                        }
                    }
                    ret = CIdPOINTER.createPOINTER(pointerLevel, pAddress, CIdType.string2Type(typeString));
                    treeNode.vars.put(name, ret);
                    if (!isArray)
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
        List<String> res = ((StatementTreeNode) treeNode).postfixExpression;
        if (res == null) {
            res = ((StatementTreeNode) treeNode).postfixExpression = MExp2FExp.convert(treeNode.lIndex, treeNode.rIndex, new Environment(functions, treeNode.codeBlocks));
        }
        Stack<Variable> stack = new Stack<>();
        for (int i = 0; i < res.size(); i++) {
            String cur = res.get(i);
            if (cur.matches("\\([0-9]+")) {
                String funcName = res.get(i - 1);
                int argCount = Integer.parseInt(cur.substring(1));
                ArgTreeNode argTreeNode = functions.argIndex.get(funcName);
                ValuedArgTreeNode valuedArgTreeNode = new ValuedArgTreeNode();
                if (argCount > 0) {
                    for (int j = 0; j < argCount; j++) {
                        String argName;
                        if (argTreeNode == null) {
                            argName = "%" + (argCount - j - 1);
                        } else {
                            int nameIndex = argTreeNode.children.get(argCount - j - 1).lIndex + 1;
                            while (gp.codeBlocks.get(nameIndex).equals("*")) nameIndex++;
                            argName = gp.codeBlocks.get(nameIndex);
                        }
                        valuedArgTreeNode.argMap.put(argName, stack.pop());
                    }
                }
                stack.push(callFunction(funcName, valuedArgTreeNode));
            } else if (cur.equals("-_unary") || cur.equals("+_unary")) {
                if (cur.charAt(0) == '-') {
                    stack.push(CIdINT.createINT(0).procOperation(stack.pop(), "-"));
                }
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
            } else if (cur.matches("sizeof(_unary)?")) {
                stack.push(CIdINT.createINT(stack.pop().sizeOf()));
            } else if (cur.matches("(A&)|(A\\*)")) {
                if (cur.equals("A&")) {
                    Variable varOp1 = stack.pop();
                    if (varOp1 instanceof CIdVOID)
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
                        stack.push(CIdPOINTER.createWithAllocatedAddress(addr, ((CIdPointerType) pointer.getTargetType()).lvl, ((CIdPointerType) pointer.getTargetType()).type));
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
            } else if (cur.equals("[")) {
                Variable varOp2 = stack.pop();
                Variable varOp1 = stack.pop();
                if (varOp2 instanceof CIdPOINTER) {
                    Variable tmp;
                    tmp = varOp2;
                    varOp2 = varOp1;
                    varOp1 = tmp;
                }
                if (varOp2.getType() != CIdType.Int || !(varOp1 instanceof CIdPOINTER)) {
                    throw new CIdGrammarException("索引\"" + varOp2 + "\"不是下标!");
                }
                long addr = ((CIdPOINTER) varOp1).getValue();
                addr += (long) (int) varOp2.getValue() * CIdType.getSize(((CIdPOINTER) varOp1).getTargetType());
                CIdType elementType = ((CIdPOINTER) varOp1).getTargetType();
                if (elementType.equals(CIdType.Int)) {
                    stack.push(CIdINT.createWithAllocatedAddress(addr));
                } else if (elementType.equals(CIdType.Float)) {
                    stack.push(CIdFLOAT.createWithAllocatedAddress(addr));
                } else if (elementType.equals(CIdType.Char)) {
                    stack.push(CIdCHAR.createWithAllocatedAddress(addr));
                } else if (elementType.equals(CIdType.Boolean)) {
                    stack.push(CIdBOOLEAN.createWithAllocatedAddress(addr));
                } else if (elementType instanceof CIdPointerType) {
                    stack.push(CIdPOINTER.createWithAllocatedAddress(addr, ((CIdPointerType) elementType).lvl, ((CIdPointerType) elementType).type));
                }
            } else if (MExp2FExp.Operation.getValue(cur) != 0) {
                Variable varOp2 = stack.pop();
                Variable varOp1 = stack.pop();
                stack.push(varOp1.procOperation(varOp2, cur));
            } else if (!functions.funcList.containsKey(cur)) {
                stack.push(string2Variable(cur, treeNode.vars));
            }
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
            case TypeLookup.FUNCTION -> {
                return CIdVOID.createVOID();
            }
            default -> throw new CIdGrammarException("未声明的符号: " + str);
        }
    }

    private boolean checkArg(ArgTreeNode callArg, ArgTreeNode funcArg) {
        ArrayList<CIdType> funcArgTypeArray = new ArrayList<>();
        ArrayList<CIdType> callArgTypeArray = new ArrayList<>();
        return callArg.children.size() == funcArg.children.size();
    }
}
