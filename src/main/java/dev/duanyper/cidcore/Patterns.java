package dev.duanyper.cidcore;

import java.util.regex.Pattern;

public class Patterns {
    public static Pattern IDENTIFIER = Pattern.compile("\\w+");
    public static Pattern NUMBER = Pattern.compile("[0-9]+");
    public static Pattern HEX_NUMBER = Pattern.compile("([+\\-])?^(0x|0X)[0-9a-fA-F]+$");
    public static Pattern SIGN = Pattern.compile("[+-]");
    public static Pattern BRACKET = Pattern.compile("[()]");
    public static Pattern LEFTEQUAL_OR_RIGHTEQUAL = Pattern.compile("(<<=)|(>>=)");
    public static Pattern SIGNED_NUMBER = Pattern.compile("([+\\-])?[0-9]+");
    public static Pattern FLOAT_NUMBER = Pattern.compile("^([0-9]+[.][0-9]*)$");
    public static Pattern COMMA_OR_SEMICOLON = Pattern.compile("[,;]");
    public static Pattern DECLARE_POINTER = Pattern.compile("\\w+\\*+");
    public static Pattern BASIC_TYPE = Pattern.compile("(int|char|float|void|bool)\\**");
    public static Pattern STRING = Pattern.compile("\"([^\"]*)\"");
    public static Pattern PROC_CONTROL = Pattern.compile("(for)|(while)|(if)|(do)|(goto)");
    public static Pattern BOOLEAN = Pattern.compile("(true)|(false)");
    public static Pattern ARRAY_INDEX = Pattern.compile("\\[\\+?(([0-9]+)|((0x|0X)[0-9a-fA-F]+))]");

    public static boolean isMatch(String str, Pattern p) {
        return p.matcher(str).matches();
    }
}
