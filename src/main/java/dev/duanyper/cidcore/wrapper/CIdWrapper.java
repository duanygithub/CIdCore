package dev.duanyper.cidcore.wrapper;

import dev.duanyper.cidcore.CInterpreter;
import dev.duanyper.cidcore.exception.CIdGrammarException;
import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.grammar.BlockTreeNode;
import dev.duanyper.cidcore.grammar.GrammarProc;
import dev.duanyper.cidcore.grammar.RootTreeNode;
import dev.duanyper.cidcore.runtime.ValuedArgTreeNode;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.variable.Variable;

import java.lang.reflect.InvocationTargetException;

public class CIdWrapper {
    Variable returnValue;

    public CIdWrapper() {
    }

    public Variable getReturnValue() {
        return returnValue;
    }

    public CInterpreter executeCode(String code, Functions functions, CInterpreter cInterpreter) throws CIdGrammarException, CIdRuntimeException {
        if (functions == null)
            functions = new Functions();
        if (cInterpreter == null)
            cInterpreter = new CInterpreter(code, false);
        cInterpreter.setFunctions(functions);
        GrammarProc gp = new GrammarProc(functions);
        cInterpreter.setGrammarProc(gp);
        gp.preProcess(code);
        gp.root = new RootTreeNode(0, gp.codeBlocks.size(), null);
        BlockTreeNode block = new BlockTreeNode(gp.root.lIndex, gp.root.rIndex, gp.root);
        gp.buildTree(block);
        gp.root.subNode.add(block);
        returnValue = cInterpreter.execBlock(block);
        return cInterpreter;
    }

    public CInterpreter executeProgram(String code, Functions functions, CInterpreter cInterpreter) {
        if (functions == null)
            functions = new Functions();
        if (cInterpreter == null)
            cInterpreter = new CInterpreter(code, false);
        cInterpreter.setFunctions(functions);
        cInterpreter.start();
        return cInterpreter;
    }

    public CInterpreter executeFunction(String function, String code, Functions functions, ValuedArgTreeNode arg, CInterpreter cInterpreter) throws CIdGrammarException, CIdRuntimeException {
        if (functions == null)
            functions = new Functions();
        if (cInterpreter == null)
            cInterpreter = new CInterpreter(code, false);
        if (arg == null)
            arg = new ValuedArgTreeNode();
        GrammarProc gp = new GrammarProc(functions);
        gp.analyze(code);
        returnValue = cInterpreter.callFunction(function, arg);
        return cInterpreter;
    }
}
