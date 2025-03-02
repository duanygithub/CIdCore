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
    final CInterpreter ci;
    final CIdWrapper wrapper = new CIdWrapper();
    Environment env;
    boolean printPrompt;
    static boolean exitLoop = false;

    public CIdShell() {
        ci = CInterpreter.create(null, null, null);
    }

    public CIdShell(Environment env, boolean printPrompt) {
        this.env = env;
        this.printPrompt = printPrompt;
        if (env == null || env.functions == null) {
            ci = CInterpreter.createEmpty();
            assert ci != null;
            this.env = new Environment(new Functions(), new Variables(), new HashMap<>());
            ci.setFunctions(this.env.functions);
        } else ci = CInterpreter.create(null, null, env.functions);
        if (this.printPrompt) {
            System.out.print(">>> ");
        }
    }

    public void exec(String s) throws CIdRuntimeException, CIdGrammarException {
        GrammarProc gp = new GrammarProc(env.functions);
        ci.setGrammarProc(gp);
        Variable value = null;
        gp.analyze(s);
        gp.root.vars.putAll(env.variables);
        env.functions = gp.functions;
        if (gp.isComplex(gp.root)) {
            wrapper.executeTree(gp.root, env, ci);
        } else {
            for (TreeNode treeNode : gp.root.children) {
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
    }

    public static void exit(CInterpreter cInterpreter, ValuedArgTreeNode args) {
        exitLoop = true;
    }

    public static void __typeof(CInterpreter cInterpreter, ValuedArgTreeNode args) {
        for(Variable var : args.argMap.values()) {
            System.out.println(var.getType().toString());
        }
    }

    public static void loop() throws IOException {
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("CIdShell -- CIdCore - made by duanyB");
        Functions functions = new Functions();
        functions.funcList.put("exit", CIdType.Void);
        functions.nativeFunctions.put("exit", CIdShell::exit);
        functions.funcList.put("printf", CIdType.Void);
        functions.nativeFunctions.put("printf", dev.duanyper.cidcore.libraries.CStdIO::printf);
        functions.funcList.put("__typeof", CIdType.Void);
        functions.nativeFunctions.put("__typeof", CIdShell::__typeof);
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
