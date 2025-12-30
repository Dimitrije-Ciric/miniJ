package org.example.semantic;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final Scope parent;

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public boolean define(Symbol s) {
        if(symbols.containsKey(s.nameS)) return false;
        symbols.put(s.nameS, s);
        return true;
    }

    public Symbol resolve(String name) {
        if(symbols.containsKey(name)) return symbols.get(name);
        if(parent != null) return parent.resolve(name);
        return null;
    }

    public Scope parent() {return parent;}
}
