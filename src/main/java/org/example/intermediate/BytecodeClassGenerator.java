package org.example.intermediate;

import org.example.ast.Stmt;
import org.example.lexer.Token;
import org.example.lexer.TokenType;

import java.util.LinkedList;
import java.util.List;

public class BytecodeClassGenerator {

    private final List<BytecodeStaticMethodGenerator> helperStaticMethods = new LinkedList<>();
    
    private BytecodeStaticMethodGenerator mainMethod;
    
    private BytecodeStaticMethodGenerator currentMethod;

    public String generate() {
        StringBuilder program  = new StringBuilder();

        program.append(".class public Main\n");
        program.append(".super java/lang/Object\n\n");
        program.append(".method public <init>()V\n");
        program.append("\taload_0\n");
        program.append("\tinvokespecial java/lang/Object/<init>()V\n");
        program.append("\treturn\n");
        program.append(".end method\n\n");

        for (var m : helperStaticMethods)
            program.append(m.generate()).append("\n");

        program.append(mainMethod.generate());

        return program.toString();
    }

    public void startMain() {
        currentMethod = mainMethod = BytecodeStaticMethodGenerator.createMain();
    }
    
    public void endMain() {
        mainMethod.returnStmt();
        mainMethod.endFunc();
    }

    public void declareVariable(String name, TokenType type) {
        currentMethod.declareVariable(name, type);
    }

    public void varAssign(String name) {
        currentMethod.varAssign(name);
    }

    public void initVariable(String name) {
        currentMethod.initVariable(name);
    }

    public void prepareFunctionCall(String name) {
        currentMethod.prepareFunctionCall(name);
    }

    public void callFunction(String name) {
        currentMethod.callFunction(name);
    }

    public void stackPop() {
        currentMethod.stackPop();
    }

    public void stackPush(Token term) {
        currentMethod.stackPush(term);
    }

    public void stackPeekMultiplyByMinusOne() {
        currentMethod.stackPeekMultiplyByMinusOne();
    }

    public void stackPeekNegate() {
        currentMethod.stackPeekNegate();
    }

    public void multiply() {
        currentMethod.multiply();
    }

    public void divide() {
        currentMethod.divide();
    }

    public void mod() {
        currentMethod.mod();
    }

    public void addition() {
        currentMethod.addition();
    }

    public void subtraction() {
        currentMethod.subtraction();
    }

    public void compareEQ() {
        currentMethod.compareEQ();
    }

    public void compareNEQ() {
        currentMethod.compareNEQ();
    }

    public void compareGT() {
        currentMethod.compareGT();
    }

    public void compareLT() {
        currentMethod.compareLT();
    }

    public void compareGE() {
        currentMethod.compareGE();
    }

    public void compareLE() {
        currentMethod.compareLE();
    }

    public void and() {
        currentMethod.and();
    }

    public void or() {
        currentMethod.or();
    }

    public void ifBranch() {
        currentMethod.ifBranch();
    }

    public void elseBranch() {
        currentMethod.elseBranch();
    }

    public void endIf(int i) {
        currentMethod.endIf(i);
    }

    public void whileCondition() {
        currentMethod.whileCondition();
    }

    public void whileLoop() {
        currentMethod.whileLoop();
    }

    public void endWhile() {
        currentMethod.endWhile();
    }

    public void forInit() {
        currentMethod.forInit();
    }

    public void forCondition() {
        currentMethod.forCondition();
    }

    public void forLoop() {
        currentMethod.forLoop();
    }

    public void endFor() {
        currentMethod.endFor();
    }

    public void returnStmt() {
        currentMethod.returnStmt();
    }

    public void declareFunc(String name, List<Stmt.Param> params, TokenType returnType) {
        currentMethod = new BytecodeStaticMethodGenerator(
                name, params, returnType,
                64, 64,
                helperStaticMethods.stream().map(BytecodeStaticMethodGenerator::getMethodSymbol).toList());
    }

    public void endFunc() {
        currentMethod.endFunc();
        helperStaticMethods.add(currentMethod);
        mainMethod.registerNewStaticMethod(currentMethod.getMethodSymbol());
        currentMethod = mainMethod;
    }
}
