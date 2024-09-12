package dev.duanyper.cidcore.runtime;

import dev.duanyper.cidcore.grammar.StructureDescriptor;
import dev.duanyper.cidcore.symbols.Functions;
import dev.duanyper.cidcore.symbols.Variables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    public List<String> codeBlocks;
    public Functions functions;
    public Variables variables;
    public Map<String, StructureDescriptor> descriptors;

    public Environment(Functions functions, Variables vars, Map<String, StructureDescriptor> descriptorMap) {
        this.functions = functions == null ? new Functions() : functions;
        this.variables = vars == null ? new Variables() : vars;
        this.descriptors = descriptorMap == null ? new HashMap<>() : descriptorMap;
    }
}
