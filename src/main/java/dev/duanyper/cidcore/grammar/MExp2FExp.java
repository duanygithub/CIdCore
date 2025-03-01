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
        }
        for (int i = 0; i < tmp.size(); i++) {
            //修改函数调用方便解析
            //printf(a+b) -> (a+b)printf
            String n = tmp.get(i);
            try {
                if (n.equals("(")) {
                    if (functions.funcList.get(tmp.get(i - 1)) != null) {
                        int stack = 1;
                        while (stack > 0) {
                            n = tmp.get(i);
                            if (n.equals("(")) {
                                func.push(tmp.get(i - 1));
                                tmp.remove(i - 1);
                                i--;
                                tmp.remove(i);
                                i--;
                                stack++;
                            }
                            if (n.equals(")")) {
                                String peekStr = func.pop();
                                if (functions.funcList.get(peekStr) != null) {
                                    tmp.remove(i);
                                    i--;
                                    tmp.add(i + 1, peekStr);
                                }
                                stack--;
                            }
                            if (n.equals(",")) {
                                tmp.remove(i);
                                i--;
                            }
                            i++;
                        }
                        i--;
                    } else func.push("");
                }
            }catch(IndexOutOfBoundsException ignore){}
        }
        return parseSuffixExpression(tmp);
    }

    private static List<String> parseSuffixExpression(List<String> tokens) throws CIdGrammarException {
        Stack<String> stack = new Stack<>();
        List<String> postfix = new ArrayList<>();

        String prevToken = null;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.isBlank()) continue;

            // 处理操作数
            if (!Operation.isOperator(token)) {
                postfix.add(token);
                // 处理栈中的单目运算符（例如 A* A* p -> p A* A*）
                while (!stack.isEmpty() && Operation.isPrefixOperator(stack.peek(), prevToken)) {
                    postfix.add(stack.pop());
                }
            }
            // 处理括号
            else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    postfix.add(stack.pop());
                }
                stack.pop(); // 弹出 '('
            }
            // 处理运算符
            else {
                // 单目运算符直接入栈（高优先级）
                if (Operation.isPrefixOperator(token, prevToken)) {
                    stack.push(token);
                } else {
                    // 普通运算符处理
                    while (!stack.isEmpty() && comparePrecedence(stack.peek(), token) >= 0) {
                        postfix.add(stack.pop());
                    }
                    stack.push(token);
                }
            }
            prevToken = token;
        }

        // 弹出剩余运算符
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    // 比较运算符优先级和结合性
    private static int comparePrecedence(String stackOp, String currentOp) {
        int stackPrec = Operation.getValue(stackOp);
        int currentPrec = Operation.getValue(currentOp);

        // 右结合运算符（如 =）仅在栈顶优先级严格大于当前优先级时弹出
        if (Operation.isRightAssociative(currentOp)) {
            return (stackPrec > currentPrec) ? 1 : -1;
        }
        // 左结合运算符（如 +, A*）在栈顶优先级 >= 当前优先级时弹出
        else {
            return Integer.compare(stackPrec, currentPrec);
        }
    }

    public static class Operation {
        //判断是否为运算符
        public static boolean isOperator(String str) {
            return getValue(str) != 0;
        }

        //判断右结合性
        static final Pattern pattern = Pattern.compile("(\\+\\+)|(--)|!|(A&)|~|(A\\*)|(sizeof)");
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
