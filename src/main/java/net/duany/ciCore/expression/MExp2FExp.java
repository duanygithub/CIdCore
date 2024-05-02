package net.duany.ciCore.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MExp2FExp {
    public static List<String> convert(String s) {
        List<String> tmp = toInfixExpressionList(s), result;
        for(int i = 0; i < tmp.size(); i++) {
            String n = tmp.get(i);
            if(n.equals("*") || n.equals("&")) {
                if(Operation.getValue(tmp.get(i - 1)) != 0 || tmp.get(i - 1).equals("(")) {
                    n = 'A' + n;
                    tmp.set(i, n);
                    continue;
                }
            }
        }
        result = parseSuffixExpression(tmp);
        return result;
    }
    private static List<String> toInfixExpressionList(String s) {
        //定义List存放中缀表达对应的内容
        List<String> ls = new ArrayList<String>();
        //用于遍历中缀表达式字符串的指针
        int index = 0;
        //多位数的拼接
        String str;
        //遍历到的字符
        char c;
        do {
            //空格跳过
            if(s.charAt(index) == ' ') {
                index++;
                continue;
            }
            //如果是一个非数字，就需要加入ls
            if (((c = s.charAt(index)) < 48 || (c = s.charAt(index)) > 57)) {
                //转化为字符串
                String tmp = "" + c;
                if(s.charAt(index + 1) == '=') {
                    tmp += '=';
                    index++;
                }
                ls.add("" + tmp);
                index++;
            } else {
                //如果是数，需要考虑多位数的问题
                //先将str置成空串
                str = "";
                while ((index < s.length()) && ((c = s.charAt(index)) >= 48) && ((c = s.charAt(index)) <= 57)) {
                    //'0'-> [48] ;'9'->[57]
                    //拼接字符串
                    str += c;
                    index++;
                }
                ls.add(str);
            }
        } while (index < s.length());
        return ls;
    }
    private static List<String> parseSuffixExpression(List<String> ls) {
        //定义两个栈
        //符号栈
        Stack<String> s1 = new Stack<String>();
        //因为S2这个栈，转换过程中没有pop操作，且还需逆序输出，可以不用Stack<String>
        //直接使用List<String> s2
        //Stack<String> s2 =new Stack<String> ();
        //储存中间结果的List2
        List<String> s2 = new ArrayList<String>();

        for (String item : ls) {
            if (item.matches("\\w+")) {
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
                while (s1.size() != 0 && Operation.getValue(s1.peek()) >= Operation.getValue(item)) {
                    s2.add(s1.pop());
                }
                //需要将item压入栈
                s1.push(item);
            }
        }
        //将s1中剩余的运算符依次弹出并压入s2
        while (s1.size() != 0) {
            s2.add(s1.pop());
        }
        //因为是存放到List,因此按顺序输出就是对应的后缀表达式对应的List
        return s2;
    }
    class Operation {
        //返回对应优先级的数字
        public static int getValue(String operation) {
            int result = 0;
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
                    System.out.println("不存在该运算符");
                    break;
            }
            return result;
        }
    }
}
