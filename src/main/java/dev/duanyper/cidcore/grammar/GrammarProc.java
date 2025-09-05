package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.DbgStart;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.symbols.CIdType;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.symbols.TypeLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static dev.duanyper.cidcore.Patterns.*;

public class GrammarProc {
    // 存储分割后的代码块（token序列）
    public List<String> codeBlocks = new ArrayList<>();
    // AST的根节点
    public RootTreeNode root;
    // 函数符号表
    public final Functions functions;

    public GrammarProc(Functions functions) {
        this.functions = functions;
    }

    /**
     * 判断节点是否包含复杂结构（循环、函数、结构体等）
     * @param treeNode 要检查的AST节点
     * @return 如果包含复杂结构返回true，否则返回false
     */
    public boolean isComplex(TreeNode treeNode) {
        for (TreeNode subNode : treeNode.children) {
            if (subNode instanceof ForTreeNode || subNode instanceof WhileTreeNode
                    || subNode instanceof StructureTreeNode
                    || subNode instanceof FunctionTreeNode) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分析代码字符串并构建AST
     * @param codes 源代码字符串
     * @return 总是返回0
     * @throws CIdGrammarException 语法分析异常
     */
    public int analyze(String codes) throws CIdGrammarException {
        preProcess(codes);
        root = new RootTreeNode(0, codeBlocks.size(), null, codeBlocks);
        buildTree(root);
        return 0;
    }

    public RootTreeNode getRoot() {
        return root;
    }

    /**
     * 将类型字符串转换为内部格式表示
     * @param type 类型字符串（如"int", "float*", "MyStruct"）
     * @return 内部格式字符串
     */
    public static String typeStringToFormat(String type) {
        switch (type) {
            case "int" -> {
                return "I";
            }
            case "float" -> {
                return "F";
            }
            case "char" -> {
                return "C";
            }
            case "bool" -> {
                return "B";
            }
            case "void" -> {
                return "V";
            }
            default -> {
                if (isMatch(type, IDENTIFIER)) {
                    return "S" + type + ";";
                }
                if (isMatch(type, DECLARE_POINTER)) {
                    String format = "P" + typeStringToFormat(type.substring(0, type.indexOf('*')));
                    format += type.lastIndexOf('*') - type.indexOf('*') + 1;
                    return format;
                } else throw new IllegalArgumentException("不合法的类型: " + type);
            }
        }
    }

    /**
     * 跳过配对的括号/大括号
     * @param i 起始索引
     * @param currentPair 起始括号类型（"(" 或 "{"）
     * @return 匹配括号结束后的索引
     */
    private int skipPairs(int i, String currentPair) {
        String endPair = currentPair.equals("(") ? ")" : "}";
        int tmp = 1;
        i++;

        while (i < codeBlocks.size() && tmp > 0) {
            String block = codeBlocks.get(i);
            if (block.equals(currentPair)) {
                tmp++;
            } else if (block.equals(endPair)) {
                tmp--;
            } else if (block.equals("(") || block.equals("{")) {
                i = skipPairs(i, block);
                continue;
            }
            i++;
        }
        return i;
    }

    /**
     * 递归构建抽象语法树（AST）
     * 核心方法：根据token类型创建相应的AST节点
     * @param parentNode 父节点
     * @throws CIdGrammarException 语法错误异常
     */
    public void buildTree(TreeNode parentNode) throws CIdGrammarException {
        int l = parentNode.lIndex; // 左边界索引
        int r = parentNode.rIndex; // 右边界索引
        if (r <= l) return; // 空范围，直接返回
        
        // 处理特殊节点类型
        if (handleSpecialNodeTypes(parentNode, l, r)) {
            return;
        }
        
        // 遍历当前范围内的所有token，根据类型创建相应的AST节点
        for (int i = l; i < r; i++) {
            String str = codeBlocks.get(i);
            // 根据token类型进行不同的处理
            switch (TypeLookup.lookup(str, parentNode.vars, functions)) {
                case TypeLookup.BASICTYPE -> i = handleBasicTypeDeclaration(parentNode, i, r);
                case TypeLookup.FUNCTION -> i = handleFunctionCall(parentNode, i);
                case TypeLookup.PROC_CONTROL -> i = handleControlFlow(parentNode, i, str);
                case TypeLookup.RETURN -> i = handleReturnStatement(parentNode, i, r);
                case TypeLookup.STRUCT -> i = handleStructDeclaration(parentNode, i);
                case TypeLookup.BLOCK_START -> i = handleBlockStart(parentNode, i);
                default -> i = handleDefaultCase(parentNode, i, r);
            }
        }
    }
    
    /**
     * 处理特殊节点类型（StatementTreeNode和ArgTreeNode）
     */
    private boolean handleSpecialNodeTypes(TreeNode parentNode, int l, int r) throws CIdGrammarException {
        if (parentNode instanceof StatementTreeNode) {
            if (parentNode instanceof VarTreeNode || parentNode instanceof FunctionCallTreeNode) {
                return true;
            }
        }
        
        if (parentNode instanceof ArgTreeNode) {
            handleArgTreeNode((ArgTreeNode) parentNode, l, r);
            return true;
        }
        return false;
    }
    
    /**
     * 处理参数列表节点
     */
    private void handleArgTreeNode(ArgTreeNode parentNode, int l, int r) throws CIdGrammarException {
        int last = l;
        int parCount = 0;
        for (int i = l; i < r; i++) {
            String current = codeBlocks.get(i);
            if (current.equals("(")) {
                parCount++;
            } else if (current.equals(")")) {
                parCount--;
            }
            if ((TypeLookup.lookup(current, parentNode.vars, functions) == TypeLookup.SPLITPOINT && parCount == 0) || (i == r - 1 && parCount == 0)) {
                int end = i;
                if (i == r - 1) {
                    end++;
                }
                StatementTreeNode node = new StatementTreeNode(last, end, parentNode);
                buildTree(node);
                parentNode.children.add(node);
                last = end + 1;
            }
        }
    }
    
    /**
     * 处理基本类型声明（变量或函数）
     */
    private int handleBasicTypeDeclaration(TreeNode parentNode, int i, int r) throws CIdGrammarException {
        if (i + 1 >= codeBlocks.size()) {
            throw new CIdGrammarException("未预料到的代码终止");
        }
        
        int ptrIndex = i + 1;
        while (ptrIndex < codeBlocks.size() && codeBlocks.get(ptrIndex).matches("\\*+")) {
            ptrIndex++;
        }
        
        if (ptrIndex >= codeBlocks.size()) {
            throw new CIdGrammarException("无效的声明");
        }
        
        if (isMatch(codeBlocks.get(i + 1), IDENTIFIER) || codeBlocks.get(i + 1).matches("\\*+")) {
            if (ptrIndex + 1 < codeBlocks.size() && codeBlocks.get(ptrIndex + 1).equals("(")) {
                return handleFunctionDeclaration(parentNode, i, ptrIndex);
            } else {
                return handleVariableDeclaration(parentNode, i, r);
            }
        }
        return i;
    }
    
    /**
     * 处理函数声明
     */
    private int handleFunctionDeclaration(TreeNode parentNode, int i, int ptrIndex) throws CIdGrammarException {
        int funcBegin = i;
        FunctionTreeNode functionTreeNode = new FunctionTreeNode(i, i + 2, parentNode);
        parentNode.children.add(functionTreeNode);
        i = ptrIndex + 2;
        int argStart = i;
        i = skipPairs(i - 1, "(");
        
        // 处理参数列表
        ArgTreeNode argTreeNode = new ArgTreeNode(argStart, i - 1, functionTreeNode);
        buildTree(argTreeNode);
        functionTreeNode.children.add(argTreeNode);
        
        // 构建函数格式字符串
        StringBuilder formatString = new StringBuilder();
        formatString.append(typeStringToFormat(codeBlocks.get(funcBegin)));
        formatString.append(codeBlocks.get(funcBegin + 1)).append('(');
        for (var treeNode : argTreeNode.children) {
            formatString.append(codeBlocks.get(treeNode.lIndex));
        }
        formatString.append(')');
        functionTreeNode.format = formatString.toString();
        
        // 处理函数体
        int blockStart = i;
        i++;
        i = skipPairs(i - 1, "{");
        BlockTreeNode blockTreeNode = new BlockTreeNode(blockStart + 1, i - 1, functionTreeNode);
        
        // 注册函数到符号表
        CIdType keywordType = CIdType.string2Type(codeBlocks.get(funcBegin));
        String name = codeBlocks.get(funcBegin + 1);
        functions.funcList.put(name, keywordType);
        functions.codeIndex.put(name, blockTreeNode);
        functions.argIndex.put(name, argTreeNode);
        
        buildTree(blockTreeNode);
        functionTreeNode.children.add(blockTreeNode);
        i--;
        
        return i;
    }
    
    /**
     * 处理变量声明
     */
    private int handleVariableDeclaration(TreeNode parentNode, int i, int r) throws CIdGrammarException {
        int varStart = i;
        int a = ++i, pointerLevel = 0;
        
        // 处理指针声明
        if (codeBlocks.get(i).equals("*")) {
            pointerLevel++;
            i++;
        }
        
        // 找到声明结束位置
        while (!codeBlocks.get(i).equals(";") && i < r) {
            i++;
        }
        
        VarTreeNode varTreeNode = new VarTreeNode(varStart, i, parentNode, codeBlocks.get(a + pointerLevel));
        i = a;
        
        // 处理多个变量声明（逗号分隔）
        while (i < parentNode.rIndex && !codeBlocks.get(i).equals(";")) {
            if (codeBlocks.get(i).equals("{") || codeBlocks.get(i).equals("(")) {
                i = skipPairs(i, codeBlocks.get(i)) - 1;
            }
            if (codeBlocks.get(i).equals(",")) {
                StatementTreeNode statementTreeNode = new StatementTreeNode(a, i, varTreeNode);
                buildTree(statementTreeNode);
                varTreeNode.children.add(statementTreeNode);
                a = i + 1;
            }
            i++;
        }
        
        // 处理最后一个变量声明
        StatementTreeNode statementTreeNode = new StatementTreeNode(a, i, varTreeNode);
        buildTree(statementTreeNode);
        varTreeNode.children.add(statementTreeNode);
        buildTree(varTreeNode);
        checkFunctionCall(varTreeNode);
        parentNode.children.add(varTreeNode);
        
        return i;
    }
    
    /**
     * 处理函数调用
     */
    private int handleFunctionCall(TreeNode parentNode, int i) throws CIdGrammarException {
        int funcCallStart = i;
        if (i + 1 >= codeBlocks.size() || !codeBlocks.get(i + 1).equals("(")) {
            throw new CIdGrammarException("不标准的函数调用");
        }
        
        i += 2;
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
        functionCallTreeNode.children.add(argTreeNode);
        buildTree(functionCallTreeNode);
        parentNode.children.add(functionCallTreeNode);
        
        return i;
    }
    
    /**
     * 处理控制流语句（if, while, for）
     */
    private int handleControlFlow(TreeNode parentNode, int i, String controlType) throws CIdGrammarException {
        switch (controlType) {
            case "if" -> { return handleIfStatement(parentNode, i); }
            case "while" -> { return handleWhileStatement(parentNode, i); }
            case "for" -> { return handleForStatement(parentNode, i); }
            default -> { return i; }
        }
    }
    
    /**
     * 处理if语句
     */
    private int handleIfStatement(TreeNode parentNode, int i) throws CIdGrammarException {
        int ifBegin = i;
        i = skipPairs(i + 1, "(");
        
        IfStatementTreeNode ifStatementTreeNode = new IfStatementTreeNode(ifBegin, i, parentNode);
        ArgTreeNode argTreeNode = new ArgTreeNode(ifBegin + 2, i - 1, ifStatementTreeNode);
        buildTree(argTreeNode);
        ifStatementTreeNode.children.add(argTreeNode);
        
        // 处理if语句体
        if (codeBlocks.get(i).equals("{")) {
            int blockBegin = i + 1;
            i = skipPairs(i, "{");
            BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, i - 1, ifStatementTreeNode);
            buildTree(blockTreeNode);
            ifStatementTreeNode.children.add(blockTreeNode);
            i--;
        } else {
            int blockBegin = i + 1;
            while (!codeBlocks.get(i).equals(";")) i++;
            BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, i, ifStatementTreeNode);
            buildTree(blockTreeNode);
            ifStatementTreeNode.children.add(blockTreeNode);
        }
        
        parentNode.children.add(ifStatementTreeNode);
        return i;
    }
    
    /**
     * 处理while语句
     */
    private int handleWhileStatement(TreeNode parentNode, int i) throws CIdGrammarException {
        if (parentNode.type().equals("do")) {
            i += 2;
            int conditionBegin = i + 2;
            i = skipPairs(i, "(");
            ArgTreeNode argTreeNode = new ArgTreeNode(conditionBegin, i - 1, parentNode);
            buildTree(argTreeNode);
            parentNode.children.add(argTreeNode);
        } else {
            int whileBegin = i;
            i += 2;
            if (i + 1 >= codeBlocks.size()) {
                throw new CIdGrammarException("不完整的while语句");
            }
            
            int conditionBegin = i, conditionEnd = 0, blockBegin = 0, blockEnd = 0;
            i = skipPairs(i - 1, "(");
            conditionEnd = i - 1;
            
            // 处理while循环体
            if (codeBlocks.get(i).equals("{")) {
                i++;
                blockBegin = i;
                i = skipPairs(i - 1, "{");
                i--;
                blockEnd = i;
            } else {
                blockBegin = i;
                while (!codeBlocks.get(i).equals(";")) i++;
                blockEnd = i;
            }
            
            WhileTreeNode whileTreeNode = new WhileTreeNode(whileBegin, blockEnd + 1, parentNode);
            ArgTreeNode argTreeNode = new ArgTreeNode(conditionBegin, conditionEnd, whileTreeNode);
            buildTree(argTreeNode);
            whileTreeNode.children.add(argTreeNode);
            
            BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, blockEnd, whileTreeNode);
            buildTree(blockTreeNode);
            whileTreeNode.children.add(blockTreeNode);
            parentNode.children.add(whileTreeNode);
        }
        return i;
    }
    
    /**
     * 处理for语句
     */
    private int handleForStatement(TreeNode parentNode, int i) throws CIdGrammarException {
        if (i + 2 >= codeBlocks.size()) {
            throw new CIdGrammarException("不完整的for语句");
        }
        
        i += 2;
        int conditionBegin = i, conditionEnd, blockBegin, blockEnd;
        int tmp = 1;
        
        // 跳过for循环条件
        while (tmp > 0) {
            if (codeBlocks.get(i).equals("(")) tmp++;
            else if (codeBlocks.get(i).equals(")")) tmp--;
            i++;
        }
        
        conditionEnd = i - 1;
        
        // 处理for循环体
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
            i--;
        } else {
            blockBegin = i;
            while (!codeBlocks.get(i).equals(";")) i++;
            blockEnd = i;
        }
        
        ForTreeNode forTreeNode = new ForTreeNode(conditionBegin - 2, blockEnd + 1, parentNode);
        ArgTreeNode argTreeNode = new ArgTreeNode(conditionBegin, conditionEnd, forTreeNode);
        buildTree(argTreeNode);
        forTreeNode.children.add(argTreeNode);
        
        BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, blockEnd, forTreeNode);
        buildTree(blockTreeNode);
        forTreeNode.children.add(blockTreeNode);
        parentNode.children.add(forTreeNode);
        
        return i;
    }
    
    /**
     * 处理return语句
     */
    private int handleReturnStatement(TreeNode parentNode, int i, int r) throws CIdGrammarException {
        int returnBegin = i;
        for (int j = returnBegin; j < r; j++) {
            if (codeBlocks.get(j).equals(";")) {
                i = j;
                break;
            }
        }
        
        StatementTreeNode returnTreeNode = new StatementTreeNode(returnBegin, i, parentNode);
        StatementTreeNode statementTreeNode = new StatementTreeNode(returnBegin + 1, i, returnTreeNode);
        checkFunctionCall(statementTreeNode);
        returnTreeNode.children.add(statementTreeNode);
        parentNode.children.add(returnTreeNode);
        
        return i;
    }
    
    /**
     * 处理结构体声明
     */
    private int handleStructDeclaration(TreeNode parentNode, int i) throws CIdGrammarException {
        int blockBegin = i + 2, structBegin = i, blockEnd;
        if (codeBlocks.get(i + 1).equals("{")) {
            blockBegin = i + 1;
        }
        
        int tmp = 0;
        do {
            if (codeBlocks.get(i).equals("{")) tmp++;
            if (codeBlocks.get(i).equals("}")) tmp--;
            i++;
        } while (tmp != 0);
        
        blockEnd = i - 1;
        while (!codeBlocks.get(i).equals(";")) i++;
        
        StructureTreeNode structureTreeNode = new StructureTreeNode(structBegin, i, parentNode);
        BlockTreeNode blockTreeNode = new BlockTreeNode(blockBegin, blockEnd, structureTreeNode);
        buildTree(blockTreeNode);
        structureTreeNode.children.add(blockTreeNode);
        
        return i;
    }
    
    /**
     * 处理代码块开始
     */
    private int handleBlockStart(TreeNode parentNode, int i) throws CIdGrammarException {
        int tmp = 0, blockStart = i + 1;
        do {
            if (codeBlocks.get(i).equals("{")) tmp++;
            if (codeBlocks.get(i).equals("}")) tmp--;
            i++;
        } while (tmp != 0);
        
        BlockTreeNode blockTreeNode = new BlockTreeNode(blockStart, i - 1, parentNode);
        if (parentNode.parentNode instanceof VarTreeNode) {
            int statementStart = blockStart;
            for (int j = blockStart; j < i - 1; j++) {
                if (codeBlocks.get(j).equals("(") || codeBlocks.get(j).equals("{")) {
                    j = skipPairs(j, codeBlocks.get(j));
                }
                if (j == i - 2 || codeBlocks.get(j + 1).equals(",")) {
                    StatementTreeNode statementTreeNode = new StatementTreeNode(statementStart, j + 1, blockTreeNode);
                    statementStart += 2;
                    buildTree(statementTreeNode);
                    blockTreeNode.children.add(statementTreeNode);
                }
            }
        } else {
            buildTree(blockTreeNode);
        }
        parentNode.children.add(blockTreeNode);
        
        return i;
    }
    
    /**
     * 处理默认情况（普通表达式语句）
     */
    private int handleDefaultCase(TreeNode parentNode, int i, int r) throws CIdGrammarException {
        if (parentNode instanceof StatementTreeNode) {
            return i;
        }
        
        int begin = i;
        while (i < r && !codeBlocks.get(i).equals(";")) i++;
        
        StatementTreeNode statementTreeNode = new StatementTreeNode(begin, i, parentNode);
        checkFunctionCall(statementTreeNode);
        parentNode.children.add(statementTreeNode);
        
        return i;
    }

    /**
     * 检查并处理函数调用
     * 在语句节点中查找并创建函数调用AST节点
     * @param parentNode 父节点
     * @throws CIdGrammarException 函数调用语法错误
     */
    private void checkFunctionCall(TreeNode parentNode) throws CIdGrammarException {
        for (int i = parentNode.lIndex; i < parentNode.rIndex; i++) {
            if (i >= codeBlocks.size()) break;
            if (TypeLookup.lookup(codeBlocks.get(i), parentNode.vars, functions) != TypeLookup.FUNCTION) {
                continue;
            }
            int funcCallStart = i;
            if (i + 1 >= codeBlocks.size() || !codeBlocks.get(i + 1).equals("(")) {
                System.out.println("不标准的函数调用");
                break;
            }
            i += 2;
            int parCnt = 1;
            while (i < codeBlocks.size() && parCnt != 0) {
                if (codeBlocks.get(i).equals("(")) parCnt++;
                else if (codeBlocks.get(i).equals(")")) parCnt--;
                i++;
            }
            if (parCnt != 0) {
                throw new CIdGrammarException("函数调用括号不匹配");
            }
            FunctionCallTreeNode functionCallTreeNode = new FunctionCallTreeNode(funcCallStart, i, parentNode);
            ArgTreeNode argTreeNode = new ArgTreeNode(funcCallStart + 2, i - 1, parentNode);
            buildTree(argTreeNode);
            functionCallTreeNode.children.add(argTreeNode);
            parentNode.children.add(functionCallTreeNode);
        }
    }

    /**
     * 预处理阶段：代码分词和清理
     * @param codes 原始源代码字符串
     */
    public void preProcess(String codes) {
        // 第一步：将代码分割成token序列
        codeBlocks = splitCodes(codes);
        DbgStart.codeBlocks = codeBlocks; // 设置调试信息
        //计算代码块总数
        int codeSize = 0;
        for (String s : codeBlocks) {
            codeSize += s.length();
        }
        if (codeSize == 0) {
            return;
        }
        //目前没用
        /*
        long codesAddr = MemOperator.allocateMemory(codeSize);
        if (codesAddr == -1) {
            return;
        }
        */
        expendHeader("");
    }

    /**
     * 扩展头文件（预留功能，当前为空实现）
     * @param headerName 头文件名
     */
    private void expendHeader(String headerName) {
        // TODO: 实现头文件包含处理
    }

    /**
     * 将源代码字符串分割成token序列
     * 核心分词器：处理注释、字符串、运算符、括号等
     * @param codes 源代码字符串
     * @return token列表
     */
    public List<String> splitCodes(String codes) {
        if (codes == null) {
            return new ArrayList<>();
        }
        try {
            codes = codes.replaceAll("\r\n", "\n");
        } catch (NullPointerException ignore) {
        }
        Stack<Integer> parStack = new Stack<>();    // 用于识别圆括号嵌套
        Stack<Integer> xpnStack = new Stack<>();    // 用于识别注释嵌套
        boolean bInQua = false,                     // 是否在双引号内（字符串字面量）
                bInPar = false;                     // 是否在圆括号内
        List<String> statements = new ArrayList<String>(); // 存储分割后的token
        StringBuilder sb = new StringBuilder();            // 当前token的缓冲区
        boolean disableSpace = false,               // 是否禁用空格分割
                disableEnter = false,               // 是否禁用换行分割
                disableUntilEnter = false;          // 是否禁用直到换行（用于行注释）
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
                    if (!sb.isEmpty()) {
                        statements.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    continue;
                }
            } else if (c == '.' && !bInQua) {
                if (isMatch(String.valueOf(nxt), NUMBER)) {
                    if (isMatch(String.valueOf(pre), NUMBER) || pre == ' ' || MExp2FExp.Operation.getValue(String.valueOf(pre)) != 0) {
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
                statements.add(String.valueOf(c));
                continue;
            } else if (c == ' ' && !bInQua/* && !bInPar*/) {
                if (!sb.isEmpty() && !disableSpace) {
                    //遇到非字符串中的空格就提交一次
                    statements.add(sb.toString());
                    sb.delete(0, sb.length());
                }
                continue;
            } else if (c == '\"' && pre != '\\') {
                //记录字符串的开始与终止
                bInQua = !bInQua;
            } else if ((c == '}' || c == '{') && !bInQua) {
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
                statements.add(")");
                continue;
            } else if (c == '#' && !bInQua) {
                //TODO: 这里原来检查预处理指令的，检查到了这行就不用换行了
            } else if (MExp2FExp.Operation.getValue(String.valueOf(c)) != 0 && !bInQua) {
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                if (c == '*' && nxt != '=') {
                    do {
                        i++;
                        c = codes.charAt(i);
                        statements.add("*");
                    } while (c == '*');
                } else {
                    try {
                        while (MExp2FExp.Operation.getValue(String.valueOf(c)) != 0) {
                            c = codes.charAt(i);
                            if (MExp2FExp.Operation.getValue(String.valueOf(c)) != 0) sb.append(c);
                            i++;
                        }
                    } catch (StringIndexOutOfBoundsException ignore) {
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
            if (statements.get(i).isEmpty()) {
                statements.remove(i);
                i--;
            }
        }
        return statements;
    }
}
