package org.example.intermediate;

import org.example.lexer.TokenType;
import org.example.semantic.Type;

import java.util.List;

public class Symbol {
    public final String nameS;
    public final TokenType type;
    public final Integer localId;


    public Type returnType;
    public List<Type> paramTypes;
    public Symbol(String nameS, TokenType type, Integer localId) {
        this.nameS = nameS;
        this.type = type;
        this.localId = localId;
    }
}
