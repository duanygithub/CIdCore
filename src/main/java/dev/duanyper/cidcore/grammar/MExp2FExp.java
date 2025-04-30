package dev.duanyper.cidcore.grammar;

import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.runtime.Environment;
import dev.duanyper.cidcore.symbols.CIdType;
import dev.duanyper.cidcore.symbols.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
                }
            }
        }
        return parseSuffixExpression(tmp);
    }

    private static List<String> parseSuffixExpression(List<String> tokens) throws CIdGrammarException {
        InfixToPostfixParser parser = new InfixToPostfixParser(tokens);
        return parser.parse();
    }

    public static class InfixToPostfixParser {
        private final List<String> tokens;
        private int pos;
        private final List<String> output = new ArrayList<>();

        public InfixToPostfixParser(List<String> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        public List<String> parse() {
            expression();
            if (!eof())
                throw new RuntimeException("Unexpected token: " + peek());
            return output;
        }

        private String peek() {
            return pos < tokens.size() ? tokens.get(pos) : null;
        }

        private String next() {
            return pos < tokens.size() ? tokens.get(pos++) : null;
        }

        private boolean eof() {
            return pos >= tokens.size();
        }

        private boolean match(String token) {
            if (pos < tokens.size() && tokens.get(pos).equals(token)) {
                pos++;
                return true;
            }
            return false;
        }

        private boolean isOperator(String t, String... ops) {
            if (t == null) return false;
            for (String op : ops) if (t.equals(op)) return true;
            return false;
        }

        private boolean isUnaryOperator(String t) {
            return isOperator(t, "+", "-", "!", "~", "A*", "A&", "sizeof", "++", "--");
        }

        private void expression() {
            assignment();
        }

        private void assignment() {
            logicalOr();
            while (isOperator(peek(), "=", "+=", "-=", "*=", "/=", "%=", "<<=", ">>=", "&=", "^=", "|=")) {
                String op = next();
                logicalOr();
                output.add(op);
            }
        }

        private void logicalOr() {
            logicalAnd();
            while (isOperator(peek(), "||")) {
                String op = next();
                logicalAnd();
                output.add(op);
            }
        }

        private void logicalAnd() {
            bitwiseOr();
            while (isOperator(peek(), "&&")) {
                String op = next();
                bitwiseOr();
                output.add(op);
            }
        }

        private void bitwiseOr() {
            bitwiseXor();
            while (isOperator(peek(), "|")) {
                String op = next();
                bitwiseXor();
                output.add(op);
            }
        }

        private void bitwiseXor() {
            bitwiseAnd();
            while (isOperator(peek(), "^")) {
                String op = next();
                bitwiseAnd();
                output.add(op);
            }
        }

        private void bitwiseAnd() {
            equality();
            while (isOperator(peek(), "&")) {
                String op = next();
                equality();
                output.add(op);
            }
        }

        private void equality() {
            relational();
            while (isOperator(peek(), "==", "!=")) {
                String op = next();
                relational();
                output.add(op);
            }
        }

        private void relational() {
            shift();
            while (isOperator(peek(), "<", ">", "<=", ">=")) {
                String op = next();
                shift();
                output.add(op);
            }
        }

        private void shift() {
            additive();
            while (isOperator(peek(), "<<", ">>")) {
                String op = next();
                additive();
                output.add(op);
            }
        }

        private void additive() {
            multiplicative();
            while (isOperator(peek(), "+", "-")) {
                String op = next();
                multiplicative();
                output.add(op);
            }
        }

        private void multiplicative() {
            cast();
            while (isOperator(peek(), "*", "/", "%")) {
                String op = next();
                cast();
                output.add(op);
            }
        }

        private void cast() {
            if (peek() != null && isType(peek()) && nextIs("(", 1)) {
                String type = next();
                next();
                expression();
                expect(")");
                output.add(type);
            } else if (peek() != null && peek().equals("(")) {
                int saved = pos;
                next();
                if (peek() != null && isType(peek()) && nextIs(")", 1)) {
                    String type = next();
                    next();
                    unary();
                    output.add(type);
                } else {
                    pos = saved;
                    unary();
                }
            } else {
                unary();
            }
        }

        private void unary() {
            String t = peek();
            if (isUnaryOperator(t)) {
                String op = next();
                if (op.equals("sizeof") && peek().equals("(")) {
                    next();
                    expression();
                    expect(")");
                    output.add("sizeof");
                }
                if (op.equals("++") || op.equals("--")) {
                    op = tokens.get(pos - 1);
                    unary();
                    String var = output.remove(output.size() - 1);

                    output.add(var);
                    output.add("1");
                    output.add(op.equals("++") ? "+" : "-");
                    output.add(var);
                    output.add("=");

                    output.add(var);
                } else {
                    unary();
                    output.add(op + "_unary");
                }
            } else {
                postfix();
            }
        }

        private void postfix() {
            primary();
            while (true) {
                if (peek() != null && peek().equals("(")) {
                    String funcName = tokens.get(pos - 1);
                    output.remove(output.size() - 1);
                    next();
                    List<String> args = new ArrayList<>();
                    int argc = 0;
                    if (!peek().equals(")")) {
                        do {
                            expression();
                            argc++;
                        } while (isOperator(peek(), ",") && next() != null);
                    }
                    expect(")");
                    output.add(funcName);
                    output.add("(" + argc);
                } else if (peek() != null && peek().equals("[")) {
                    next();
                    expression();
                    expect("]");
                    output.add("[");
                } else if (match("++") || match("--")) {
                    String op = tokens.get(pos - 1);
                    String var = output.remove(output.size() - 1);
                    output.add(var);  // 原值先留着用

                    // 副作用表达式追加
                    output.add(var);
                    output.add("1");
                    output.add(op.equals("++") ? "+" : "-");
                    output.add(var);
                    output.add("=");
                } else {
                    break;
                }
            }
        }

        private void primary() {
            String t = peek();
            if (t == null) throw new RuntimeException("Unexpected end of input");
            if (t.equals("(")) {
                next();
                expression();
                expect(")");
            } else {
                output.add(next());
            }
        }

        private void expect(String s) {
            if (!s.equals(next()))
                throw new RuntimeException("Expected '" + s + "'");
        }

        private boolean nextIs(String val, int offset) {
            return (pos + offset < tokens.size()) && tokens.get(pos + offset).equals(val);
        }

        private boolean isType(String t) {
            return isOperator(t, "int", "float", "char", "double", "short", "long", "signed", "unsigned", "void");
        }
    }

    public static class Operation {
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
                case "[", "]":
                    result = 15; break;
                default:
//                    System.out.println("不存在该运算符");
                    break;
            }
            return result;
        }
    }
}