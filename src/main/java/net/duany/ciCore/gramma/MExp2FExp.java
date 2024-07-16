package net.duany.ciCore.gramma;

import net.duany.ciCore.symbols.Functions;
import net.duany.ciCore.symbols.Keywords;
import net.duany.ciCore.symbols.TypeLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MExp2FExp {
    public static List<String> convert(String s) {
        if (s.charAt(s.length() - 1) == ';') {
            s = s.substring(0, s.length() - 1);
        }
        List<String> tmp = GrammarProc.splitCodes(s), result;
        Stack<String> func = new Stack<>();
        for (int i = 0; i < tmp.size(); i++) {
            //替换*和&使其更方便索引
            String n = tmp.get(i);
            if (n.equals("*") || n.equals("&")) {
                if (i == 0 || Operation.getValue(tmp.get(i - 1)) != 0 || tmp.get(i - 1).equals("(") || Keywords.keywords.contains(tmp.get(i - 1))) {
                    n = 'A' + n;
                    tmp.set(i, n);
                    continue;
                }
            }

            //修改函数调用方便解析
            //printf(a+b) -> (a+b)printf
            try {
                if (n.equals("(")) {
                    if(Functions.funcList.get(tmp.get(i - 1)) != null) {
                        func.push(tmp.get(i - 1));
                        tmp.remove(i - 1);
                        i--;
                    }
                    else func.push("");
                } else if(n.equals(")")) {
                    String peekStr = func.pop();
                    if(Functions.funcList.get(peekStr) != null) {
                        tmp.add(i + 1, peekStr);
                    }
                }
            }catch(IndexOutOfBoundsException ignore){}
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
            if (s.charAt(index) == ' ') {
                index++;
                continue;
            }
            //引号跳过
            if (s.charAt(index) == '\'') {
                try {
                    if (s.charAt(index + 2) == '\'') {
                        String tmp = "";
                        tmp += "'" + s.charAt(index + 1) + "'";
                        ls.add(tmp);
                        index += 3;
                        continue;
                    }
                } catch (StringIndexOutOfBoundsException ignore) {
                }
            }
            if (s.charAt(index) == '\"') {
                boolean flag = false;
                try {
                    flag = s.charAt(index - 1) != '\\';
                } catch (StringIndexOutOfBoundsException ignore) {
                }
                if (flag) {
                    String tmp = "";
                    while (true) {
                        tmp += s.charAt(index);
                        index++;
                        try {
                            if(s.charAt(index) == '\"' && s.charAt(index - 1) != '\\') {
                                break;
                            }
                        }catch (StringIndexOutOfBoundsException ignore) {}
                    }
                    tmp += s.charAt(index);
                    ls.add(tmp);
                    index++;
                    continue;
                }
            }
            //如果是一个非数字，就需要加入ls
            if (((c = s.charAt(index)) < 48 || (c = s.charAt(index)) > 57) && !String.valueOf(s.charAt(index)).matches("\\w")) {
                //转化为字符串
                String tmp = "" + c;
                try {
                    if (s.charAt(index + 1) == '=') {
                        tmp += '=';
                        index++;
                    }
                    if (s.charAt(index + 1) == '>') {
                        tmp += '>';
                        index++;
                    }
                    if (s.charAt(index + 1) == '<') {
                        tmp += '<';
                        index++;
                    }
                }catch(StringIndexOutOfBoundsException ignore){}
                ls.add("" + tmp);
                index++;
            } else {
                //如果是数，需要考虑多位数的问题
                //先将str置成空串
                str = "";
                while ((index < s.length())
                        && ((((c = s.charAt(index)) >= 48) && ((c = s.charAt(index)) <= 57))
                        || String.valueOf(s.charAt(index)).matches("\\w"))) {
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
            if (Operation.getValue(item) == 0 && !item.matches("[()]")) {
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
    public class Operation {
        //返回对应优先级的数字
        public static int getValue(String operation) {
            int result = 0;
            if (operation.matches("(<<=)|(>>=)")) return 2;
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
