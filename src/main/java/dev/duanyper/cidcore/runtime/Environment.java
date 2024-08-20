package dev.duanyper.cidcore.runtime;

import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.symbols.Variables;

import java.util.List;

public class Environment {
    public List<String> codeBlocks;
    public Functions functions;
    public Variables variables;

    public Environment(Functions functions, Variables vars) {
        this.functions = functions == null ? new Functions() : functions;
        this.variables = vars == null ? new Variables() : vars;
    }
}
