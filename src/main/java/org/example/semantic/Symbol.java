package org.example.semantic;

import org.example.lexer.Token;

import java.util.List;

public class Symbol {
    public final String nameS;
    public final Type type;

    public Type returnType;
    public List<Type> paramTypes;
    public Symbol(String nameS, Type type) {
        this.nameS = nameS;
        this.type = type;
    }
}
