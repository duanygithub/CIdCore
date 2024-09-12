package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.variable.Variable;

import java.util.HashMap;

public class Variables extends HashMap<String, Variable> {
    public Variables(Variables vars) {
        this.putAll(vars);
    }

    public Variables() {
        super();
    }
}
