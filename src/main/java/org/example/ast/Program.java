package org.example.ast;

import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class Program implements VisitorAccept {

    public List<Stmt> stmts = new LinkedList<>();

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visitProgram(this);
    }
}
