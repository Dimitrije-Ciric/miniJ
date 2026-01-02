package org.example.intermediate;

import lombok.Getter;
import org.example.ast.Stmt;
import org.example.lexer.Token;
import org.example.lexer.TokenType;

import java.util.List;
import java.util.Stack;

public class BytecodeStaticMethodGenerator {

    public static BytecodeStaticMethodGenerator createMain() {
        return new BytecodeStaticMethodGenerator(
                "main",
                List.of(new Stmt.Param(
                        new Token(TokenType.STRING, "", 0, 0, 0),
                        new Token(TokenType.IDENT, "args", 0, 0, 0)
                )),
                null,
                64, 64,
                List.of());
    }

    private final StringBuilder programBuilder = new StringBuilder();

    private final Integer stackLimit;
    private final Integer localsLimit;

    private Integer localsCounter = 0;
    private Integer labelCount = 0;
    private Stack<Integer> activeLabels = new Stack<>();

    private Scope scope = new Scope(null);

    @Getter
    private final Symbol methodSymbol;

    public BytecodeStaticMethodGenerator(String name, List<Stmt.Param> params, TokenType returnType,
                                         Integer stackLimit, Integer localsLimit, List<Symbol> definedMethods) {
        this.stackLimit = stackLimit;
        this.localsLimit = localsLimit;

        StringBuilder argsSerialized = new StringBuilder();

        for (var p : params) {
            var s = scope.define(new Symbol(
                    p.name.lexeme(), p.type.type(), localsCounter++));
            argsSerialized.append(s.getSerializedType());
        }

        programBuilder.append(String.format(".method public static %s(%s)%s\n", name, argsSerialized, Type.of(returnType).jasminSerialize()));
        programBuilder.append(String.format("\t.limit stack %d\n", stackLimit));
        programBuilder.append(String.format("\t.limit locals %d\n", localsLimit));

        this.methodSymbol = new Symbol(
                name,
                params.stream().map(pp -> Type.of(pp.type.type())).toList(),
                Type.of(returnType)
        );

        for (var s : definedMethods)
            scope.define(s);
    }

    public void registerNewStaticMethod(Symbol s) {
        scope.define(s);
    }

    public String generate() {
        programBuilder.append(".end method\n");

        return programBuilder.toString();
    }

    public void declareVariable(String name, TokenType type) {
        Symbol s = scope.define(new Symbol(name, type, localsCounter++));

        switch (type) {
            case TokenType.INT:
                programBuilder.append(String.format("\tldc %d\n", 0));
                programBuilder.append(String.format("\tistore %d\n", s.localId));
                break;
        }
    }

    public void varAssign(String name) {
        Symbol s = scope.resolve(name);

        switch (s.type) {
            case TokenType.INT:
                programBuilder.append(String.format("\tistore %d\n", s.localId));
        }
    }

    public void initVariable(String name) {
        this.varAssign(name);
    }

    public void prepareFunctionCall(String name) {
        if (name.equals("print")) {
            programBuilder.append("\tgetstatic java/lang/System/out Ljava/io/PrintStream;\n");
            return;
        }
    }

    public void callFunction(String name) {
        if (name.equals("print")) {
            programBuilder.append("\tinvokevirtual java/io/PrintStream/println(I)V\n");
            programBuilder.append("\tbipush 0\n");
            return;
        }

        Symbol s = scope.resolve(name);

        programBuilder.append(String.format("\tinvokestatic Main/%s(%s)%s\n", s.nameS, s.getSerializedParams(), s.getSerializedReturnType()));
    }

    public void stackPop() {
        programBuilder.append("\tpop\n");
    }

    public void stackPush(Token term) {
        if (term.type() == TokenType.INT_LIT)
            programBuilder.append(String.format("\tldc %s\n", term.lexeme()));
        if (term.type() == TokenType.IDENT) {
            Symbol s = scope.resolve(term.lexeme());
            if (s.type == TokenType.INT)
                programBuilder.append(String.format("\tiload %d\n", s.localId));
        }
    }

    public void stackPeekMultiplyByMinusOne() {
        programBuilder.append("\tbipush -1\n");
        programBuilder.append("\timul\n");
    }

    public void stackPeekNegate() {
        programBuilder.append("\tbipush 1\n");
        programBuilder.append("\tixor\n");
    }

    public void multiply() {
        programBuilder.append("\timul\n");
    }

    public void divide() {
        programBuilder.append("\tidiv\n");
    }

    public void addition() {
        programBuilder.append("\tiadd\n");
    }

    public void subtraction() {
        programBuilder.append("\tisub\n");
    }

    public void compareEQ() {
        var labelId = labelCount++;
        programBuilder.append(String.format("\tif_icmpne FALSE_BRANCH_%d\n", labelId));

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));
    }

    public void compareNEQ() {
        var labelId = labelCount++;
        programBuilder.append(String.format("\tif_icmpeq FALSE_BRANCH_%d\n", labelId));

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));
    }

    public void compareGT() {
        var labelId = labelCount++;
        programBuilder.append(String.format("\tif_icmple FALSE_BRANCH_%d\n", labelId));

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));
    }

    public void compareLT() {
        var labelId = labelCount++;
        programBuilder.append(String.format("\tif_icmpge FALSE_BRANCH_%d\n", labelId));

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));
    }

    public void compareGE() {
        var labelId = labelCount++;
        programBuilder.append(String.format("\tif_icmplt FALSE_BRANCH_%d\n", labelId));

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));
    }

    public void compareLE() {
        var labelId = labelCount++;
        programBuilder.append(String.format("\tif_icmpgt FALSE_BRANCH_%d\n", labelId));

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));
    }

    public void and() {
        programBuilder.append("\tiand\n");
    }

    public void or() {
        programBuilder.append("\tior\n");
    }

    public void ifBranch() {
        scope = new Scope(scope);
        var labelId = labelCount++;
        activeLabels.push(labelId);
        programBuilder.append(String.format("\tifeq ELSE_%d\n", labelId));
    }

    public void elseBranch() {
        scope = scope.parent();
        scope = new Scope(scope);
        programBuilder.append(String.format("\tgoto END_IF_%d\n", activeLabels.peek()));
        programBuilder.append(String.format("ELSE_%d:\n", activeLabels.peek()));
    }

    public void endIf(int i) {
        scope = scope.parent();
        while (i > 0) {
            programBuilder.append(String.format("END_IF_%d:\n", activeLabels.pop()));
            --i;
        }
    }

    public void whileCondition() {
        var labelId = labelCount++;
        activeLabels.push(labelId);
        programBuilder.append(String.format("WHILE_CONDITION_%d:\n", labelId));
    }

    public void whileLoop() {
        scope = new Scope(scope);
        programBuilder.append(String.format("\tifeq WHILE_END_%d\n", activeLabels.peek()));
    }

    public void endWhile() {
        scope = scope.parent();
        var labelId = activeLabels.pop();
        programBuilder.append(String.format("\tgoto WHILE_CONDITION_%d\n", labelId));
        programBuilder.append(String.format("WHILE_END_%d:\n", labelId));
    }

    public void forInit() {
        scope = new Scope(scope);
    }

    public void forCondition() {
        var labelId = labelCount++;
        activeLabels.push(labelId);
        programBuilder.append(String.format("FOR_CONDITION_%d:\n", labelId));
    }

    public void forLoop() {
        programBuilder.append(String.format("\tifeq FOR_END_%d\n", activeLabels.peek()));
    }

    public void endFor() {
        scope = scope.parent();
        var labelId = activeLabels.pop();
        programBuilder.append(String.format("\tgoto FOR_CONDITION_%d\n", labelId));
        programBuilder.append(String.format("FOR_END_%d:\n", labelId));
    }

    public void returnStmt() {
        if (methodSymbol.returnType == Type.INT)
            programBuilder.append("\tireturn\n");
        if (methodSymbol.returnType == Type.VOID)
            programBuilder.append("\treturn\n");
    }

    public void endFunc() {
    }
}
