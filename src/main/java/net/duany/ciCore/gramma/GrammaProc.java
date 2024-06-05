package net.duany.ciCore.gramma;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;
import net.duany.ciCore.symbols.TypeLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GrammaProc {
    public List<String> codeBlocks = new ArrayList<>();
    RootTreeNode root;

    public int analyze(String codes) {
        preProcess(codes);
        root = new RootTreeNode(0, codeBlocks.size() - 1);
        buildTree(root);
        return 0;
    }

    public void buildTree(TreeNode parentNode) {
        int l = parentNode.lIndex;
        int r = parentNode.rIndex;
        if (r <= l) return;
        int lastSplitPoint = l - 1;
        int viewedR = l - 1;
        for (int i = l; i <= r; i++) {
            String str = codeBlocks.get(i);
            switch (TypeLookup.lookup(str)) {
                case TypeLookup.BASICTYPE -> {
                    if (codeBlocks.get(i + 1).matches("\\w+")) {
                        if (codeBlocks.get(i + 2).equals("(")) {
                            FunctionTreeNode functionTreeNode = new FunctionTreeNode(i, i + 1);
                            parentNode.subNode.add(functionTreeNode);
                            i += 3;
                            int argStart = i;
                            Stack<Integer> stack = new Stack<Integer>();
                            stack.push(0);
                            for (; !stack.empty(); i++) {
                                if (codeBlocks.get(i).equals("(")) stack.push(0);
                                else if (codeBlocks.get(i).equals(")")) stack.pop();
                            }
                            ArgTreeNode argTreeNode = new ArgTreeNode(argStart, i - 1);
                            buildTree(argTreeNode);
                            functionTreeNode.subNode.add(argTreeNode);
                            stack.push(0);
                            int blockStart = i;
                            i++;
                            for (; !stack.empty(); i++) {
                                if (codeBlocks.get(i).equals("{")) stack.push(0);
                                else if (codeBlocks.get(i).equals("}")) stack.pop();
                            }
                            BlockTreeNode blockTreeNode = new BlockTreeNode(blockStart, i - 1);
                            buildTree(blockTreeNode);
                            functionTreeNode.subNode.add(blockTreeNode);
                        } else {
                            int varStart = i;
                            for (; !codeBlocks.get(i).matches("[;,]"); i++) {
                            }
                            int varEnd = i;
                            VarTreeNode varTreeNode = new VarTreeNode(varStart, varEnd);
                            parentNode.subNode.add(new VarTreeNode(varStart, i));
                            if (i - varStart > 1) {
                                i = varStart + 1;
                                StatementTreeNode statementTreeNode = new StatementTreeNode(i, varEnd);
                                buildTree(statementTreeNode);
                                parentNode.subNode.add(statementTreeNode);
                            }
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
                    FunctionCallTreeNode functionCallTreeNode = new FunctionCallTreeNode(funcCallStart, i);
                    ArgTreeNode argTreeNode = new ArgTreeNode(funcCallStart + 2, i - 2);
                    buildTree(argTreeNode);
                    functionCallTreeNode.subNode.add(argTreeNode);
                    parentNode.subNode.add(functionCallTreeNode);
                }
                case TypeLookup.SPLITPOINT -> {
                    if (viewedR > lastSplitPoint) break;
                    parentNode.subNode.add(new StatementTreeNode(lastSplitPoint + 1, i - 1));
                    lastSplitPoint = i;
                    i++;
                }
                default -> {
                    /*
                    if(parentNode.type().equals("funcCall") && i == r) {
                        parentNode.subNode.add(new StatementTreeNode(lastSplitPoint + 1, i - 1));
                        lastSplitPoint = i;
                    }
                    */
                }
            }
            viewedR = i - 1;
        }
    }

    private int preProcess(String codes) {
        //先把代码分割一遍
        codeBlocks = splitCodes(codes);
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
        Stack<Integer> tmp = new Stack<>();
        boolean bInQua = false;
        for (int i = 0; i < codeBlocks.size(); i++) {
            if (codeBlocks.get(i).equals("{") || codeBlocks.get(i).equals("(") || codeBlocks.get(i).equals("[")) {
                tmp.add(0);
                continue;
            }
            if (codeBlocks.get(i).equals("}") || codeBlocks.get(i).equals(")") || codeBlocks.get(i).equals("]")) {
                tmp.pop();
                continue;
            }
            if (codeBlocks.get(i).equals("\"")) {
                bInQua = !bInQua;
                continue;
            }
            if (bInQua || !tmp.empty()) continue;
            if (codeBlocks.get(i).equals("main")) {
                if (codeBlocks.get(i - 1).equals("int") && codeBlocks.get(i + 3).equals("{")) {
                    if (Functions.funcList.getOrDefault("main", null) == null
                            && Functions.codesIndex.getOrDefault("main", null) == null) {
                        Functions.funcList.put("main", Keywords.Int);
                        Functions.codesIndex.put("main", i + 3);
                        break;
                    }
                }
            }
        }
        if (!tmp.empty()) return -1;
        if (Functions.codesIndex.get("main") == -1) return -1;
        expendHeader("");
        System.out.println("Main function index: " + Functions.codesIndex.get("main"));
        return 0;
    }

    private int expendHeader(String headerName) {
        return 0;
    }

    private List<String> splitCodes(String codes) {
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
            }
            sb.append(c);
            if (i == codes.length() - 1) statements.add(sb.toString());
        }
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).equals("")) statements.remove(i);
        }
        return statements;
    }
}
