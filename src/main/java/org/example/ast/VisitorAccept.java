package org.example.ast;

public interface VisitorAccept {
    <R> R accept(Visitor<R> v);
}
