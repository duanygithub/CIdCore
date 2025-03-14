package dev.duanyper.cidcore.wrapper;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.grammar.BlockTreeNode;
import dev.duanyper.cidcore.grammar.GrammarProc;
import dev.duanyper.cidcore.grammar.RootTreeNode;
import dev.duanyper.cidcore.grammar.TreeNode;
import dev.duanyper.cidcore.runtime.Environment;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.variable.Variable;

public class CIdWrapper {
    Variable returnValue;

    public CIdWrapper() {
    }

    public Variable getReturnValue() {
        return returnValue;
    }

    public CInterpreter executeCode(String code, Environment env, CInterpreter cInterpreter) throws CIdGrammarException, CIdRuntimeException {
        if (env == null)
            env = new Environment(null, null, null);
        if (cInterpreter == null)
            cInterpreter = CInterpreter.create(null, code, null);
        cInterpreter.setFunctions(env.functions);
        GrammarProc gp = new GrammarProc(env.functions);
        cInterpreter.setGrammarProc(gp);
        gp.preProcess(code);
        gp.root = new RootTreeNode(0, gp.codeBlocks.size(), null, gp.codeBlocks);
        BlockTreeNode block = new BlockTreeNode(gp.root.lIndex, gp.root.rIndex, gp.root);
        gp.buildTree(block);
        gp.root.children.add(block);
        block.vars.putAll(env.variables);
        returnValue = cInterpreter.execBlock(block);
        return cInterpreter;
    }

    public int executeProgram(String code, Functions functions, CInterpreter cInterpreter) throws CIdGrammarException {
        if (functions == null)
            functions = new Functions();
        if (cInterpreter == null)
            cInterpreter = CInterpreter.create(null, code, null);
        cInterpreter.setFunctions(functions);
        return cInterpreter.start();
    }

    public CInterpreter executeFunction(String function, String code, Functions functions, ValuedArgTreeNode arg, CInterpreter cInterpreter) throws CIdGrammarException, CIdRuntimeException {
        if (functions == null)
            functions = new Functions();
        if (cInterpreter == null)
            cInterpreter = CInterpreter.create(null, code, null);
        if (arg == null)
            arg = new ValuedArgTreeNode();
        GrammarProc gp = new GrammarProc(functions);
        gp.analyze(code);
        returnValue = cInterpreter.callFunction(function, arg);
        return cInterpreter;
    }

    public CInterpreter executeTree(TreeNode treeNode, Environment env, CInterpreter cInterpreter) throws CIdRuntimeException, CIdGrammarException {
        if (env == null)
            env = new Environment(null, null, null);
        if (cInterpreter == null)
            cInterpreter = CInterpreter.create(null, null, null);
        GrammarProc gp = new GrammarProc(env.functions);
        cInterpreter.setFunctions(env.functions);
        cInterpreter.setGrammarProc(gp);
        returnValue = cInterpreter.calcExpression(treeNode);
        return cInterpreter;
    }
}
