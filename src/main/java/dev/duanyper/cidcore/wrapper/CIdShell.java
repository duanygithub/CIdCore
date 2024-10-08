package dev.duanyper.cidcore.wrapper;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.grammar.GrammarProc;
import dev.duanyper.cidcore.grammar.TreeNode;
import dev.duanyper.cidcore.runtime.Environment;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.*;
import dev.duanyper.cidcore.variable.CIdPOINTER;
import dev.duanyper.cidcore.variable.Variable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class CIdShell {
    CInterpreter ci;
    CIdWrapper wrapper = new CIdWrapper();
    Environment env;
    boolean printPrompt;
    static boolean exitLoop = false;

    public CIdShell() {
        ci = new CInterpreter();
    }

    public CIdShell(Environment env, boolean printPrompt) {
        this.env = env;
        this.printPrompt = printPrompt;
        if (env == null || env.functions == null) {
            ci = new CInterpreter();
            this.env = new Environment(new Functions(), new Variables(), new HashMap<>());
            ci.setFunctions(this.env.functions);
        } else ci = new CInterpreter(env.functions);
        if (this.printPrompt) {
            System.out.print(">>> ");
        }
    }

    public Variable exec(String s) throws CIdRuntimeException, CIdGrammarException {
        GrammarProc gp = new GrammarProc(env.functions);
        ci.setGrammarProc(gp);
        Variable value = null;
        gp.analyze(s);
        gp.root.vars.putAll(env.variables);
        env.functions = gp.functions;
        if (gp.isComplex(gp.root)) {
            wrapper.executeTree(gp.root, env, ci);
        } else {
            for (TreeNode treeNode : gp.root.subNode) {
                value = ci.calcExpression(treeNode);
                if (gp.codeBlocks.get(treeNode.lIndex).equals("return")) {
                    break;
                }
            }
        }
        for (int i = 0; i < gp.root.vars.size(); i++) {
            String varName = (String) gp.root.vars.keySet().toArray()[i];
            if (env.variables.containsKey(varName)) {
                env.variables.replace(varName, gp.root.vars.get(varName));
            } else {
                env.variables.put(varName, gp.root.vars.get(varName));
            }
        }
        if (printPrompt) {
            if (value != null && value.getType() != CIdType.Void && !(value.getType() instanceof CIdStructType)) {
                if (value.getType() instanceof CIdPointerType && ((CIdPOINTER) value).getTargetType() == CIdType.Char && ((CIdPOINTER) value).getLevel() == 1) {
                    System.out.println("\"" + value + "\"");
                } else System.out.println(value);
            }
            System.out.print(">>> ");
        }
        return wrapper.returnValue;
    }

    public static void exit(CInterpreter cInterpreter, ValuedArgTreeNode args) {
        exitLoop = true;
    }

    public static void printf(CInterpreter cInterpreter, ValuedArgTreeNode arg) {
        Variable var = arg.argMap.get("%0");
        System.out.println(var.toString());
    }

    public static void loop() throws IOException, NoSuchMethodException {
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("CIdShell -- CIdCore - made by duanyB");
        Functions functions = new Functions();
        functions.funcList.put("exit", CIdType.Void);
        functions.nativeFunctions.put("exit", CIdShell.class.getMethod("exit", CInterpreter.class, ValuedArgTreeNode.class));
        functions.funcList.put("printf", CIdType.Void);
        functions.nativeFunctions.put("printf", CIdShell.class.getMethod("printf", CInterpreter.class, ValuedArgTreeNode.class));
        CIdShell shell = new CIdShell(new Environment(functions, null, null), true);
        while (!exitLoop) {
            String c = cin.readLine();
            try {
                shell.exec(c);
            } catch (CIdRuntimeException | CIdGrammarException e) {
                e.printStackTrace();
            }
        }
    }
}
