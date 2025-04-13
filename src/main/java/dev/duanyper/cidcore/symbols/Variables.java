package dev.duanyper.cidcore.symbols;

import dev.duanyper.cidcore.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Variables extends HashMap<String, Variable> {
    private final List<Variables> inheritors = new ArrayList<>();
    private final Variables parent;

    public Variables(Variables vars) {
        super();
        this.putAll(vars);
        vars.inheritors.add(this);
        parent = vars;
    }

    public Variables() {
        super();
        parent = null;
    }

    @Override
    public Variable put(String key, Variable value) {
        if (parent != null && size() < parent.size()) {
            for (var i : parent.entrySet()) {
                putIfAbsent(i.getKey(), i.getValue());
            }
        }
        for (var inheritors : inheritors) {
            inheritors.put(key, value);
        }
        return super.put(key, value);
    }
}
