package org.example.semantic;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final Scope parent;
    private final Boolean isFunction;

    public Scope(Scope parent) {
        this.parent = parent;
        isFunction = false;
    }

    public Scope(Scope currentScope, boolean isFunction) {
        this.parent = currentScope;
        this.isFunction = isFunction;
    }

    public boolean define(Symbol s) {
        if(symbols.containsKey(s.nameS)) return false;
        symbols.put(s.nameS, s);
        return true;
    }

    public Symbol resolve(String name) {
        if(symbols.containsKey(name)) return symbols.get(name);
        if(parent != null && !isFunction) return parent.resolve(name);
        return null;
    }

    public Scope parent() {return parent;}
}
