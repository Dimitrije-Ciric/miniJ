package org.example.intermediate;

import org.example.lexer.TokenType;

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

    public Symbol(String nameS, List<Type> paramTypes, Type returnType) {
        this.nameS = nameS;
        this.type = null;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.localId = null;
    }

    public String getSerializedReturnType() {
        return returnType.jasminSerialize();
    }

    public String getSerializedParams() {
        return paramTypes.stream().map(Type::jasminSerialize).reduce("", String::concat);
    }

    public String getSerializedType() {
        return Type.of(type).jasminSerialize();
    }
}
