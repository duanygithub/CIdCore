package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.Start;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.symbols.TypeLookup;
import dev.duanyper.cidcore.symbols.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GrammarProc {
    public List<String> codeBlocks = new ArrayList<>();
    public List<String> originalCodeBlocks = new ArrayList<>();
    public RootTreeNode root;
    public Functions functions;

    public GrammarProc(Functions functions) {
        this.functions = functions;
    }

    public int analyze(String codes) {
        preProcess(codes);
        root = new RootTreeNode(0, codeBlocks.size(), null);
        buildTree(root);
        return 0;
    }

    public RootTreeNode getRoot() {
        return root;
    }

    public void buildTree(TreeNode parentNode) {
        int l = parentNode.lIndex;
        int r = parentNode.rIndex;
        if (r <= l) return;
        if (parentNode.type().equals("arg")) {
            int last = l;
            for (int i = l, splitCount = 0; i < r; i++) {
                if (TypeLookup.lookup(codeBlocks.get(i), parentNode.vars, functions) == TypeLookup.SPLITPOINT || i == r - 1) {
                    if (i == r - 1) {
                        i++;
                    }
                    StatementTreeNode node = new StatementTreeNode(last, i, parentNode);
                    buildTree(node);
                    parentNode.subNode.add(node);
                    last = i + 1;
                    splitCount++;
                }
            }
            return;
        }
        for (int i = l; i < r; i++) {
            String str = codeBlocks.get(i);
            switch (TypeLookup.lookup(str, parentNode.vars, functions)) {
                case TypeLookup.BASICTYPE -> {
                    if (codeBlocks.get(i + 1).matches("\\w+")) {
                        if (codeBlocks.get(i + 2).equals("(")) {
                            int funcBegin = i;
                            FunctionTreeNode functionTreeNode = new FunctionTreeNode(i, i + 2, parentNode);
                            parentNode.subNode.add(functionTreeNode);
                            i += 3;
                            int argStart = i;
                            int tmp = 1;
                            for (; tmp != 0; i++) {
                                if (codeBlocks.get(i).equals("(")) tmp++;
                                else if (codeBlocks.get(i).equals(")")) tmp--;
                            }
                            ArgTreeNode argTreeNode = new ArgTreeNode(argStart, i - 1, functionTreeNode);
                            buildTree(argTreeNode);
                            functionTreeNode.subNode.add(argTreeNode);
                            tmp = 1;
                            int blockStart = i;
                            i++;
                            for (; tmp != 0; i++) {
                                if (codeBlocks.get(i).equals("{")) tmp++;
                                else if (codeBlocks.get(i).equals("}")) tmp--;
                            }
                            BlockTreeNode blockTreeNode = new BlockTreeNode(blockStart + 1, i - 1, functionTreeNode);
                            buildTree(blockTreeNode);
                            functionTreeNode.subNode.add(blockTreeNode);
                            Types keywordType = Types.string2Keywords(str);
                            String name = codeBlocks.get(funcBegin + 1);
                            functions.funcList.put(name, keywordType);
                            functions.codeIndex.put(name, blockTreeNode);
                            functions.argIndex.put(name, argTreeNode);
                            i--;
                        } else {
                            int varStart = i;
                            int tmp = 0;
                            while (true) {
                                if (codeBlocks.get(i).equals("(")) tmp++;
                                else if (codeBlocks.get(i).equals(")")) {
                                    tmp--;
                                } else if (TypeLookup.lookup(codeBlocks.get(i), parentNode.vars, functions) == TypeLookup.SPLITPOINT) {
                                    if (tmp == 0) break;
                                }
                                i++;
                            }
                            int varEnd = i;
                            VarTreeNode varTreeNode = new VarTreeNode(varStart, i, parentNode);
                            /*
                            if (i - varStart > 1) {
                                i = varStart + 1;
                                StatementTreeNode statementTreeNode = new StatementTreeNode(i, varEnd, parentNode);
                                buildTree(statementTreeNode);
                                varTreeNode.subNode.add(statementTreeNode);
                            }
                            */
                            parentNode.subNode.add(varTreeNode);
                        }
                    }
                }
                case TypeLookup.FUNCTION -> {
                    int funcCallStart = i;
                    if (!codeBlocks.get(++i).equals("(")) {
                        System.out.println("不标准的函数调用");
                        break;
                    }
                    i++;
                    int parCnt = 1;
                    while (parCnt != 0) {
                        if (codeBlocks.get(i).equals("(")) {
                            parCnt++;
                        } else if (codeBlocks.get(i).equals(")")) {
                            parCnt--;
                        }
                        i++;
                    }
                    FunctionCallTreeNode functionCallTreeNode = new FunctionCallTreeNode(funcCallStart, i, parentNode);
                    ArgTreeNode argTreeNode = new ArgTreeNode(funcCallStart + 2, i - 1, parentNode);
                    buildTree(argTreeNode);
                    functionCallTreeNode.subNode.add(argTreeNode);
                    parentNode.subNode.add(functionCallTreeNode);
                }
                case TypeLookup.PROC_CONTROL -> {
                    if (str.equals("if")) {
                        int tmp = 0, ifBegin = i;
                        do {
                            i++;
                            if (codeBlocks.get(i).equals("(")) tmp++;
                            else if (codeBlocks.get(i).equals(")")) tmp--;
                        } while (tmp != 0);
                        IfStatementTreeNode ifStatementTreeNode = new IfStatementTreeNode(ifBegin, i + 1, parentNode);
                        ArgTreeNode argTreeNode = new ArgTreeNode(ifBegin + 2, i, ifStatementTreeNode);
                        buildTree(argTreeNode);
                        ifStatementTreeNode.subNode.add(argTreeNode);
                        if (codeBlocks.get(i + 1).equals("{")) {
                            int blockBegin = i + 2;
                            do {
                                i++;
                                if (codeBlocks.get(i).equals("{")) tmp++;
                                else if (codeBlocks.get(i).equals("}")) tmp--;
                            } while (tmp != 0);
                            BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, i, ifStatementTreeNode);
                            buildTree(blockTreeNode);
                            ifStatementTreeNode.subNode.add(blockTreeNode);
                        } else {
                            int blockBegin = i + 1;
                            while (!codeBlocks.get(i).equals(";")) i++;
                            BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, i, ifStatementTreeNode);
                            buildTree(blockTreeNode);
                            ifStatementTreeNode.subNode.add(blockTreeNode);
                        }
                        parentNode.subNode.add(ifStatementTreeNode);
                    } else if (str.equals("while")) {
                        if (parentNode.type().equals("do")) {
                            i += 2;
                            int conditionBegin = i + 2;
                            int tmp = 1;
                            while (tmp > 0) {
                                if (codeBlocks.get(i).equals("(")) tmp++;
                                else if (codeBlocks.get(i).equals(")")) tmp--;
                                i++;
                            }
                            ArgTreeNode argTreeNode = new ArgTreeNode(conditionBegin, i - 1, parentNode);
                            buildTree(argTreeNode);
                            parentNode.subNode.add(argTreeNode);
                        } else {
                            int whileBegin = i;
                            i += 2;
                            int conditionBegin = i, conditionEnd = 0, blockBegin = 0, blockEnd = 0;
                            int tmp = 1;
                            while (tmp > 0) {
                                if (codeBlocks.get(i).equals("(")) tmp++;
                                else if (codeBlocks.get(i).equals(")")) tmp--;
                                i++;
                            }
                            conditionEnd = i - 1;
                            i++;
                            if (codeBlocks.get(i).equals("{")) {
                                tmp = 1;
                                i++;
                                blockBegin = i;
                                while (tmp > 0) {
                                    if (codeBlocks.get(i).equals("{")) tmp++;
                                    else if (codeBlocks.get(i).equals("}")) tmp--;
                                    i++;
                                }
                                blockEnd = i - 1;
                            } else {
                                blockBegin = i;
                                //for (; !codeBlocks.get(i).equals(";"); i++) ;
                                while (!codeBlocks.get(i).equals(";")) i++;
                                blockEnd = i;
                            }
                            WhileTreeNode whileTreeNode = new WhileTreeNode(whileBegin, blockEnd + 2, parentNode);
                            ArgTreeNode argTreeNode = new ArgTreeNode(conditionBegin, conditionEnd, whileTreeNode);
                            buildTree(argTreeNode);
                            whileTreeNode.subNode.add(argTreeNode);
                            BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, blockEnd, whileTreeNode);
                            buildTree(blockTreeNode);
                            whileTreeNode.subNode.add(blockTreeNode);
                            parentNode.subNode.add(whileTreeNode);
                            i++;
                        }
                    } else if (str.equals("for")) {
                        i += 2;
                        int conditionBegin = i, conditionEnd, blockBegin, blockEnd;
                        int tmp = 1;
                        while (tmp > 0) {
                            if (codeBlocks.get(i).equals("(")) tmp++;
                            else if (codeBlocks.get(i).equals(")")) tmp--;
                            i++;
                        }
                        conditionEnd = i - 1;
                        i++;
                        if (codeBlocks.get(i).equals("{")) {
                            tmp = 1;
                            i++;
                            blockBegin = i;
                            while (tmp > 0) {
                                if (codeBlocks.get(i).equals("{")) tmp++;
                                else if (codeBlocks.get(i).equals("}")) tmp--;
                                i++;
                            }
                            blockEnd = i - 1;
                        } else {
                            blockBegin = i;
                            //for (; !codeBlocks.get(i).equals(";"); i++) ;
                            while (!codeBlocks.get(i).equals(";")) i++;
                            blockEnd = i;
                        }
                        ForTreeNode forTreeNode = new ForTreeNode(conditionEnd - 2, blockEnd + 1, parentNode);
                        ArgTreeNode argTreeNode = new ArgTreeNode(conditionBegin, i - 1, forTreeNode);
                        buildTree(argTreeNode);
                        forTreeNode.subNode.add(argTreeNode);
                        BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, blockEnd, forTreeNode);
                        buildTree(blockTreeNode);
                        forTreeNode.subNode.add(blockTreeNode);
                        parentNode.subNode.add(forTreeNode);
                    }
                }
                case TypeLookup.RETURN -> {
                    int returnBegin = i;
                    for (int j = returnBegin; j < r; j++) {
                        if (originalCodeBlocks.get(j).equals(";")) {
                            i = j;
                            break;
                        }
                    }
                    StatementTreeNode returnTreeNode = new StatementTreeNode(returnBegin, i, parentNode);
                    StatementTreeNode statementTreeNode = new StatementTreeNode(returnBegin + 1, i, returnTreeNode);
                    buildTree(statementTreeNode);
                    returnTreeNode.subNode.add(statementTreeNode);
                    parentNode.subNode.add(returnTreeNode);
                }
                case TypeLookup.STRUCT -> {
                    int blockBegin = i + 1;
                    int tmp = 0;
                    do {
                        if (codeBlocks.get(i).equals("{")) tmp++;
                        if (codeBlocks.get(i).equals("}")) tmp--;
                        i++;
                    } while (tmp != 0);
                    StructureTreeNode structureTreeNode = new StructureTreeNode(blockBegin, i, parentNode);
                    BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin + 2, i - 1, structureTreeNode);
                    buildTree(blockTreeNode);
                    structureTreeNode.subNode.add(structureTreeNode);
                }
                case TypeLookup.BLOCK_START -> {
                    int tmp = 0, blockStart = i + 1;
                    do {
                        if (codeBlocks.get(i).equals("{")) tmp++;
                        if (codeBlocks.get(i).equals("}")) tmp--;
                        i++;
                    } while (tmp != 0);
                    BlockTreeNode blockTreeNode = new BlockTreeNode(blockStart, i - 2, parentNode);
                    parentNode.subNode.add(blockTreeNode);
                }
                default -> {
                    int begin = i;
                    boolean somethingUseful = false;
                    for (; i < r && !codeBlocks.get(i).matches(";"); i++) {
                        if (TypeLookup.lookup(codeBlocks.get(i), parentNode.vars, functions) == TypeLookup.FUNCTION) {
                            somethingUseful = true;
                        }
                    }
                    StatementTreeNode statementTreeNode = new StatementTreeNode(begin, i, parentNode);
                    if (somethingUseful) buildTree(statementTreeNode);
                    parentNode.subNode.add(statementTreeNode);
                }
            }
        }
    }

    public int preProcess(String codes) {
        //先把代码分割一遍
        originalCodeBlocks = new ArrayList<>(codeBlocks = splitCodes(codes));
        Start.codeBlocks = codeBlocks;
        //计算代码块总数
        int codeSize = 0;
        for (String s : codeBlocks) {
            codeSize += s.length();
        }
        if (codeSize == 0) {
            return -1;
        }
        //目前没用
        int codesAddr = MemOperator.allocateMemory(codeSize);
        if (codesAddr == -1) {
            return -1;
        }
        expendHeader("");
        return 0;
    }

    private int expendHeader(String headerName) {
        return 0;
    }

    public List<String> splitCodes(String codes) {
        try {
            codes = codes.replaceAll("\r\n", "\n");
        } catch (NullPointerException ignore) {
        }
        Stack<Integer> parStack = new Stack<Integer>();//用于识别圆括号
        Stack<Integer> xpnStack = new Stack<Integer>();//用于识别注释
        boolean bInQua = false,//在双引号内
                bInPar = false;//在圆括号内
        List<String> statements = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean disableSpace = false,
                disableEnter = false,
                disableUntilEnter = false;
        for (int i = 0; i < codes.length(); i++) {
            char c = codes.charAt(i), pre, nxt;//当前字符和之前的字符
            //防止访问codes[-1]而引发异常
            try {
                pre = codes.charAt(i - 1);
            } catch (Exception ignore) {
                pre = '\0';
            }
            //防止访问codes[length + 1]而引发异常
            try {
                nxt = codes.charAt(i + 1);
            } catch (Exception ignore) {
                nxt = '\0';
            }
            //检查由/**/括起来的注释
            if (c == '/' && nxt == '*' && !bInQua) {
                xpnStack.add(0);
            }
            if (c == '*' && nxt == '/' && !bInQua) {
                xpnStack.pop();
                i = i + 1;
                continue;
            }
            if (!xpnStack.empty()) {
                continue;
            }
            //检查标志
            if (disableUntilEnter) {
                //取消添加字符，直到遇到换行
                if (c == '\n' && !bInQua) disableUntilEnter = false;
                continue;
            }
            //判断字符，大多数都要分字符串内外来确保分割准确率
            if (c == '\n') {
                if (!bInQua) {
                    disableSpace = false;
                    if (sb.length() != 0) {
                        statements.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    continue;
                }
            } else if (c == '.') {
                if (String.valueOf(nxt).matches("[0-9]")) {
                    if (String.valueOf(pre).matches("[0-9]") || pre == ' ' || MExp2FExp.Operation.getValue(String.valueOf(pre)) != 0) {
                        sb.append(c);
                        continue;
                    }
                }
            } else if (c == '\t') {
                //将字符串中的制表符替换为空格，其它不用管，直接跳过
                if (bInQua) sb.append(' ');
                continue;
            } else if (c == '/' && nxt == '/' && !bInQua) {
                //注释没用，跳过这行
                disableUntilEnter = true;
                continue;
            } else if ((c == ';' || c == ',') && !bInQua) {
                //遇到分号和逗号提交一次
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                sb.append(c);
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                continue;
            } else if (c == ' ' && !bInQua/* && !bInPar*/) {
                if (sb.length() != 0 && !disableSpace) {
                    //遇到非字符串中的空格就提交一次
                    statements.add(sb.toString());
                    sb.delete(0, sb.length());
                }
                if (disableSpace)//没啥用处
                {
                    sb.append(' ');
                }
                continue;
            } else if (c == '\"' && pre != '\\') {
                //记录字符串的开始与终止
                bInQua = !bInQua;
            } else if ((c == '}' || c == '{' || c == ',') && !bInQua && !bInPar) {
                //大括号和逗号分开来放好像更方便
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                statements.add(String.valueOf(c));
                continue;
            } else if (c == '(' && !bInQua) {
                //识别括号的开始
                parStack.add(1);
                bInPar = !parStack.empty();
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                statements.add("(");
                continue;
            } else if (c == ')' && !bInQua) {
                //识别括号的终止
                parStack.pop();
                bInPar = !parStack.empty();
                statements.add(sb.toString());
                sb.delete(0, sb.length());
            } else if (c == '#' && !bInQua) {
                //这里原来检查预处理指令的，检查到了这行就不用换行了
                //但现在感觉还是换行比较好
//                disableSpace = true;
            } else if (MExp2FExp.Operation.getValue(String.valueOf(c)) != 0) {
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                if (c == '*' && nxt != '=') {
                    do {
                        i++;
                        c = codes.charAt(i);
                        statements.add("*");
                    } while (c == '*');
                } else {
                    while (MExp2FExp.Operation.getValue(String.valueOf(c)) != 0) {
                        c = codes.charAt(i);
                        if (MExp2FExp.Operation.getValue(String.valueOf(c)) != 0) sb.append(c);
                        i++;
                    }
                    statements.add(sb.toString());
                    sb.delete(0, sb.length());
                    i--;
                }
            }
            sb.append(c);
            if (i == codes.length() - 1) statements.add(sb.toString().trim());
        }
        for (int i = 0; i < statements.size(); i++) {
            statements.set(i, statements.get(i).trim());
            if (statements.get(i).equals("")) {
                statements.remove(i);
                i--;
            }
        }
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).matches("[\\+-]")) {
                try {
                    if (MExp2FExp.Operation.getValue(statements.get(i - 1)) != 0 && MExp2FExp.Operation.getValue(statements.get(i + 1)) == 0) {
                        if (statements.get(i).equals("-")) {
                            statements.set(i, "-" + statements.get(i + 1));
                        } else statements.set(i, statements.get(i + 1));
                        statements.remove(i + 1);
                        i--;
                    }
                } catch (IndexOutOfBoundsException ignore) {
                }
            }
            if (TypeLookup.lookup(statements.get(i), null, functions) == TypeLookup.BASICTYPE ||
                    TypeLookup.lookup(statements.get(i), null, functions) == TypeLookup.DECLEAR_POINTER) {
                while (statements.get(i + 1).equals("*")) {
                    statements.set(i, statements.get(i) + "*");
                    statements.remove(i + 1);
                }
            }
        }
        return statements;
    }
}
