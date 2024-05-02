package net.duany.ciCore;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.runtime.RtContext;
import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CInterpreter {
    String codes;
    int codesAddr = -1;
    RtContext context;
    List<String> codeBlocks;
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
    public int start()
    {
        preProcess();
        Stack<Integer> callDepth = new Stack<>();
        int i = Functions.codesIndex.get("main");
        do {
            if(codeBlocks.get(i).equals("{")) {
                callDepth.add(0);
                i++;
                continue;
            }
            if(codeBlocks.get(i).equals("}")) {
                callDepth.pop();
                i++;
                continue;
            }
            switch(codeBlocks.get(i)) {
                case "printf": {
                    String content = codeBlocks.get(i + 2);
                    System.out.println(content);
                    i++;
                }
            }
        } while(!callDepth.empty());
        return 0;
    }

    private int preProcess()
    {
        //先把代码分割一遍
        codeBlocks = splitCodes();
        //计算代码块总数
        int codeSize = 0;
        for(String s : codeBlocks) {
            codeSize += s.length();
        }
        if(codeSize == 0) {
            return -1;
        }
        //目前没用
        codesAddr = MemOperator.allocateMemory(codeSize);
        if(codesAddr == -1) {
            return -1;
        }
        context = new RtContext();
        Stack<Integer> tmp = new Stack<>();
        boolean bInQua = false;
        for(int i = 0; i < codeBlocks.size(); i++) {
            if(codeBlocks.get(i).equals("{") || codeBlocks.get(i).equals("(") || codeBlocks.get(i).equals("[")) {
                tmp.add(0);
                continue;
            }
            if(codeBlocks.get(i).equals("}") || codeBlocks.get(i).equals(")") || codeBlocks.get(i).equals("]")) {
                tmp.pop();
                continue;
            }
            if(codeBlocks.get(i).equals("\"")) {
                bInQua = !bInQua;
                continue;
            }
            if(bInQua || !tmp.empty())continue;
            if(codeBlocks.get(i).equals("main")) {
                if(codeBlocks.get(i - 1).equals("int") && codeBlocks.get(i + 3).equals("{")) {
                    if(Functions.funcList.getOrDefault("main", null) == null
                    && Functions.codesIndex.getOrDefault("main", null) == null) {
                        Functions.funcList.put("main", Keywords.Int);
                        Functions.codesIndex.put("main", i + 4);
                        break;
                    }
                }
            }
        }
        if(!tmp.empty())return -1;
        if(Functions.codesIndex.get("main") == -1)return -1;
        expendHeader("");
        System.out.println("Main function index: " + Functions.codesIndex.get("main"));
        return 0;
    }
    private int expendHeader(String headerName)
    {
        return 0;
    }
    private List<String> splitCodes() {
        try {
            codes = codes.replaceAll("\r\n", "\n");
        }catch(NullPointerException ignore){}
        Stack<Integer> parStack = new Stack<Integer>();//用于识别圆括号
        Stack<Integer> xpnStack = new Stack<Integer>();//用于识别注释
        boolean bInQua = false,//在双引号内
                bInPar = false;//在圆括号内
        List<String> statements = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean disableSpace = false,
                disableEnter = false,
                disableUntilEnter = false;
        for(int i = 0; i < codes.length(); i++) {
            char c = codes.charAt(i), pre, nxt;//当前字符和之前的字符
            //防止访问codes[-1]而引发异常
            try{pre = codes.charAt(i - 1);}catch(Exception ignore){pre = '\0';}
            //防止访问codes[length + 1]而引发异常
            try{nxt = codes.charAt(i + 1);}catch(Exception ignore){nxt = '\0';}
            //检查由/**/括起来的注释
            if(c == '/' && nxt == '*' && !bInQua) {
                xpnStack.add(0);
            }
            if(c == '*' && nxt == '/' && !bInQua) {
                xpnStack.pop();
                i = i + 1;
                continue;
            }
            if(!xpnStack.empty()) {
                continue;
            }
            //检查标志
            if(disableUntilEnter) {
                //取消添加字符，直到遇到换行
                if(c == '\n' && !bInQua) disableUntilEnter = false;
                continue;
            }
            //判断字符，大多数都要分字符串内外来确保分割准确率
            if(c == '\n') {
                if(!bInQua) {
                    disableSpace = false;
                    if(sb.length() != 0) {
                        statements.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    continue;
                }
            } else if(c == '\t') {
                //将字符串中的制表符替换为空格，其它不用管，直接跳过
                if(bInQua) sb.append(' ');
                continue;
            } else if(c == '/' && nxt == '/' && !bInQua) {
                //注释没用，跳过这行
                disableUntilEnter = true;
                continue;
            } else if(c == ';' && !bInQua) {
                //遇到分号提交一次
                sb.append(c);
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                continue;
            } else if(c == ' ' && !bInQua && !bInPar) {
                if(sb.length() != 0 && !disableSpace) {
                    //遇到非字符串中的空格就提交一次
                    statements.add(sb.toString());
                    sb.delete(0, sb.length());
                }
                if(disableSpace)//没啥用处
                {
                    sb.append(' ');
                }
                continue;
            } else if(c == '\"' && pre != '\\') {
                //记录字符串的开始与终止
                bInQua = !bInQua;
            } else if((c == '}' || c == '{' || c == ',') && !bInQua && !bInPar) {
                //大括号和逗号分开来放好像更方便
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                statements.add(String.valueOf(c));
                continue;
            } else if(c == '(' && !bInQua) {
                //识别括号的开始
                parStack.add(1);
                bInPar = !parStack.empty();
                statements.add(sb.toString());
                sb.delete(0, sb.length());
                statements.add("(");
                continue;
            } else if(c == ')' && !bInQua) {
                //识别括号的终止
                parStack.pop();
                bInPar = !parStack.empty();
                statements.add(sb.toString());
                sb.delete(0, sb.length());
            } else if(c == '#' && !bInQua) {
                //这里原来检查预处理指令的，检查到了这行就不用换行了
                //但现在感觉还是换行比较好
//                disableSpace = true;
            }
            sb.append(c);
            if(i == codes.length() - 1) statements.add(sb.toString());
        }
        for(int i = 0; i < statements.size(); i++) {
            if(statements.get(i).equals(""))statements.remove(i);
        }
        return statements;
    }
}
