package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.runtime.Environment;
import dev.duanyper.cidcore.symbols.CIdType;
import dev.duanyper.cidcore.symbols.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import static dev.duanyper.cidcore.Patterns.LEFTEQUAL_OR_RIGHTEQUAL;
import static dev.duanyper.cidcore.Patterns.isMatch;

public class MExp2FExp {
    public static List<String> convert(int l, int r, Environment env) throws CIdGrammarException {
        Functions functions = env.functions;
        if (env.codeBlocks.get(r - 1).equals(";")) {
            r--;
        }
        List<String> tmp = new ArrayList<>(env.codeBlocks.subList(l, r));
        Stack<String> func = new Stack<>();
        for (int i = 0; i < tmp.size(); i++) {
            //替换*和&使其更方便索引
            String n = tmp.get(i);
            if (n.equals("*") || n.equals("&")) {
                if (i == 0 || Operation.getValue(tmp.get(i - 1)) != 0 || tmp.get(i - 1).equals("(") || CIdType.keywords.contains(tmp.get(i - 1))) {
                    n = 'A' + n;
                    tmp.set(i, n);
                    continue;
                }
            }

            //修改函数调用方便解析
            //printf(a+b) -> (a+b)printf
            try {
                if (n.equals("(")) {
                    if (functions.funcList.get(tmp.get(i - 1)) != null) {
                        func.push(tmp.get(i - 1));
                        tmp.remove(i - 1);
                        i--;
                    } else func.push("");
                } else if(n.equals(")")) {
                    String peekStr = func.pop();
                    if (functions.funcList.get(peekStr) != null) {
                        tmp.add(i + 1, peekStr);
                    }
                }
            }catch(IndexOutOfBoundsException ignore){}
        }
        return parseSuffixExpression(tmp);
    }

    private static List<String> parseSuffixExpression(List<String> tokens) throws CIdGrammarException {
        /*
        //定义两个栈
        //符号栈
        Stack<String> s1 = new Stack<String>();
        //因为S2这个栈，转换过程中没有pop操作，且还需逆序输出，可以不用Stack<String>
        //直接使用List<String> s2
        //Stack<String> s2 =new Stack<String> ();
        //储存中间结果的List2
        List<String> s2 = new ArrayList<String>();

        for (String item : ls) {
            if (Operation.getValue(item) == 0 && !isMatch(item, BRACKET)) {
                //如果是一个数，直接加入S2
                s2.add(item);
            } else if (item.equals("(")) {
                //左括号也直接加入
                s1.push(item);
            } else if (item.equals(")")) {
                //如果为有括号，则需要弹出s1中的运算符，直到遇到左括号
                while (!s1.peek().equals("(")) {
                    s2.add(s1.pop());
                }
                //将左括号弹出
                s1.pop();
            } else {
                //当item的优先级小于等于s1栈顶的运算符时，将s1栈顶的运算符弹出并加入到s2中
                while (!s1.isEmpty() && Operation.getValue(s1.peek()) > Operation.getValue(item)) {
                    s2.add(s1.pop());
                }
                //需要将item压入栈
                s1.push(item);
            }
        }
        //将s1中剩余的运算符依次弹出并压入s2
        while (!s1.isEmpty()) {
            s2.add(s1.pop());
        }
        //因为是存放到List,因此按顺序输出就是对应的后缀表达式对应的List
        return s2;

         */
        Stack<String> stack = new Stack<>();
        List<String> postfix = new ArrayList<>();

        String prevToken = null;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String nextToken = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;

            if (token.isBlank()) {
                continue; // 忽略空白
            }

            if (!Operation.isOperator(token)) {
                // 如果是操作数，直接添加到后缀表达式
                postfix.add(token);
            } else if (Operation.isOperator(token)) {
                if (token.equals("(")) {
                    stack.push(token); // 左括号直接入栈
                } else if (token.equals(")")) {
                    // 遇到右括号，弹出所有运算符直到左括号
                    while (!stack.isEmpty() && !stack.peek().equals("(")) {
                        postfix.add(stack.pop());
                    }
                    stack.pop(); // 弹出左括号
                } else if (Operation.isPostfixOperator(token, nextToken)) {
                    // 后缀自增或自减
                    postfix.add(prevToken);
                    postfix.add(token);
                } else if (Operation.isPrefixOperator(token, prevToken)) {
                    // 前缀自增或自减
                    stack.push(token);
                } else {
                    // 普通运算符
                    while (!stack.isEmpty() && Operation.getValue(stack.peek()) >= Operation.getValue(token) &&
                            !Operation.isRightAssociative(token)) {
                        postfix.add(stack.pop());
                    }
                    stack.push(token);
                }
            } else {
                throw new CIdGrammarException("无效的字符: " + token);
            }
            prevToken = token;
        }

        // 弹出剩余运算符
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    public static class Operation {
        //判断是否为运算符
        public static boolean isOperator(String str) {
            return getValue(str) != 0;
        }

        //判断右结合性
        static final Pattern pattern = Pattern.compile("(\\+\\+)|(--)|!|(A&)|~|(A*)|(sizeof)");
        private static boolean isRightAssociative(String operator) {
            return isMatch(operator, pattern);
        }

        // 判断是否是后缀运算符
        private static boolean isPostfixOperator(String token, String nextToken) {
            return ("++".equals(token) || "--".equals(token)) && (nextToken == null || isOperator(nextToken));
        }

        // 判断是否是前缀运算符
        private static boolean isPrefixOperator(String token, String prevToken) {
            return isMatch(token, pattern) && (prevToken == null || isOperator(prevToken));
        }

        //返回对应优先级的数字
        public static int getValue(String operation) {
            int result = 0;
            if (isMatch(operation, LEFTEQUAL_OR_RIGHTEQUAL)) return 2;
            switch (operation) {
                case ",":
                    result = 1; break;
                case "=":
                case "/=":
                case "*=":
                case "%=":
                case "+=":
                case "-=":
                case "<<=":
                case ">>=":
                case "&=":
                case "^=":
                case "|=":
                    result = 2; break;
                case "?:": result = 3; break;
                case "||": result = 4; break;
                case "&&": result = 5; break;
                case "|": result = 6; break;
                case "^": result = 7; break;
                case "&": result = 8; break;
                case "==":
                case "!=":
                    result = 9; break;
                case ">":
                case "<":
                case ">=":
                case "<=":
                    result = 10; break;
                case "<<":
                case ">>":
                    result = 11; break;
                case "+":
                case "-":
                    result = 12; break;
                case "/":
                case "*":
                case "%":
                    result = 13; break;
                case "~":
                case "++":
                case "--":
                case "!":
                case "A*":
                case "A&":
                case "sizeof":
                    result = 14; break;
                case ".":
                case "->":
                    result = 15; break;
                default:
//                    System.out.println("不存在该运算符");
                    break;
            }
            return result;
        }
    }
}
