package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Variables extends HashMap<String, Variable> {
    private final List<Variables> inheritors = new ArrayList<>();

    public Variables(Variables vars) {
        this.putAll(vars);
        vars.inheritors.add(this);
    }

    public Variables() {
        super();
    }

    @Override
    public Variable put(String key, Variable value) {
        for (var inheritors : inheritors) {
            inheritors.put(key, value);
        }
        return super.put(key, value);
    }
}
