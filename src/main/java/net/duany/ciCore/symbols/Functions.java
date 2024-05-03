package net.duany.ciCore.symbols;

import java.util.HashMap;
import java.util.Map;

public class Functions {
    public static Map<String, Keywords> funcList = new HashMap<>();
    public static Map<String, Integer> codesIndex = new HashMap<>();

    public class NativeFunction {
        public static Object runNativeFunction_String1(String funcName, String arg) {
            switch (funcName) {
                case "printf": return printf(arg);
                default: return null;
            }
        }
        static int printf(String str) {
            System.out.print(str);
            return 0;
        }
    }
}
