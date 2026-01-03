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
                List.of(),
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

    private Stack<Type> typeStack = new Stack<>();

    @Getter
    private final Symbol methodSymbol;

    public BytecodeStaticMethodGenerator(String name, List<Stmt.Param> params, TokenType returnType,
                                         Integer stackLimit, Integer localsLimit, List<Symbol> definedMethods) {
        this.stackLimit = stackLimit;
        this.localsLimit = localsLimit;

        if (name.equals("main")) {
            programBuilder.append(".method public static main([Ljava/lang/String;)V\n");
            programBuilder.append(String.format("\t.limit stack %d\n", stackLimit));
            programBuilder.append(String.format("\t.limit locals %d\n", localsLimit));

            this.methodSymbol = new Symbol(
                    name,
                    null,
                    Type.of(returnType)
            );

            for (var s : definedMethods)
                scope.define(s);

            localsCounter = 1;

            return;
        }

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

    public void declareVariable(String name, TokenType type, Integer arrayLength) {
        Symbol s = scope.define(new Symbol(name, type, localsCounter++));

        switch (type) {
            case TokenType.INT:
                programBuilder.append(String.format("\tldc %d\n", 0));
                programBuilder.append(String.format("\tistore %d\n", s.localId));
                break;
            case TokenType.STRING:
                programBuilder.append("\tldc \"\"\n");
                programBuilder.append(String.format("\tistore %d\n", s.localId));
                break;
            case TokenType.DOUBLE:
                programBuilder.append("\tldc2_w 0.0\n");
                programBuilder.append(String.format("\tdstore %d\n", s.localId));
                localsCounter++;
                break;
            case TokenType.BOOL:
                programBuilder.append(String.format("\tldc %d\n", 0));
                programBuilder.append(String.format("\tistore %d\n", s.localId));
                break;
            case TokenType.ARRAY:
                programBuilder.append(String.format("\tbipush %d\n", arrayLength));
                programBuilder.append("\tnewarray int\n");
                programBuilder.append(String.format("\tastore %d\n", s.localId));
                break;
        }
    }

    public void varAssign(String name, Token arrayIndex) {
        Symbol s = scope.resolve(name);

        switch (s.type) {
            case TokenType.INT:
                programBuilder.append(String.format("\tistore %d\n", s.localId));
                break;
            case TokenType.STRING:
                programBuilder.append(String.format("\tastore %d\n", s.localId));
                break;
            case TokenType.DOUBLE:
                programBuilder.append(String.format("\tdstore %d\n", s.localId));
                break;
            case TokenType.BOOL:
                programBuilder.append(String.format("\tistore %d\n", s.localId));
                break;
            case TokenType.ARRAY:
                programBuilder.append(String.format("\taload %d\n", s.localId));
                programBuilder.append("\tswap\n");
                if (arrayIndex.type() == TokenType.INT_LIT)
                    programBuilder.append(String.format("\tldc %d\n", (Integer) arrayIndex.literal()));
                else if (arrayIndex.type() == TokenType.IDENT) {
                    Symbol idx = scope.resolve(arrayIndex.lexeme());
                    programBuilder.append(String.format("\tiload %d\n", idx.localId));
                }
                programBuilder.append("\tswap\n");
                programBuilder.append("\tiastore\n");
                break;
        }
        typeStack.pop();
    }

    public void initVariable(String name, Token arrayIndex) {
        this.varAssign(name, arrayIndex);
    }

    public void prepareFunctionCall(String name) {
        if (name.equals("print")) {
            programBuilder.append("\tgetstatic java/lang/System/out Ljava/io/PrintStream;\n");
            return;
        }
    }

    public void callFunction(String name) {
        if (name.equals("print")) {
            if (typeStack.peek() == Type.INT)
                programBuilder.append("\tinvokevirtual java/io/PrintStream/println(I)V\n");
            else if (typeStack.peek() == Type.DOUBLE)
                programBuilder.append("\tinvokevirtual java/io/PrintStream/println(D)V\n");
            else if (typeStack.peek() == Type.STRING)
                programBuilder.append("\tinvokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
            typeStack.pop();
            typeStack.push(Type.VOID);
            return;
        }

        Symbol s = scope.resolve(name);

        for (int i = 0; i < s.paramTypes.size(); i++)
            typeStack.pop();

        programBuilder.append(String.format("\tinvokestatic Main/%s(%s)%s\n", s.nameS, s.getSerializedParams(), s.getSerializedReturnType()));
        typeStack.push(s.returnType);
    }

    public void stackPop() {

        if (typeStack.peek() == Type.VOID) {
            typeStack.pop();
            return;
        }

        if (typeStack.peek() == Type.DOUBLE)
            programBuilder.append("\tpop2\n");
        else
            programBuilder.append("\tpop\n");

        typeStack.pop();
    }

    public void stackPush(Token term) {
        if (term.type() == TokenType.INT_LIT) {
            programBuilder.append(String.format("\tldc %s\n", term.lexeme()));
            typeStack.push(Type.INT);
        }
        if (term.type() == TokenType.STRING_LIT) {
            programBuilder.append(String.format("\tldc \"%s\"\n", term.literal()));
            typeStack.push(Type.STRING);
        }
        if (term.type() == TokenType.DOUBLE_LIT) {
            programBuilder.append(String.format("\tldc2_w %s\n", term.lexeme()));
            typeStack.push(Type.DOUBLE);
        }
        if (term.type() == TokenType.BOOL_FALSE_LIT) {
            programBuilder.append("\tldc 0\n");
            typeStack.push(Type.INT);
        }
        if (term.type() == TokenType.BOOL_TRUE_LIT) {
            programBuilder.append("\tldc 1\n");
            typeStack.push(Type.INT);
        }
        if (term.type() == TokenType.IDENT) {
            Symbol s = scope.resolve(term.lexeme());
            if (s.type == TokenType.INT) {
                programBuilder.append(String.format("\tiload %d\n", s.localId));
                typeStack.push(Type.INT);
            }
            if (s.type == TokenType.STRING) {
                programBuilder.append(String.format("\taload %d\n", s.localId));
                typeStack.push(Type.STRING);
            }
            if (s.type == TokenType.DOUBLE) {
                programBuilder.append(String.format("\tdload %d\n", s.localId));
                typeStack.push(Type.DOUBLE);
            }
            if (s.type == TokenType.BOOL) {
                programBuilder.append(String.format("\tiload %d\n", s.localId));
                typeStack.push(Type.INT);
            }
            if (s.type == TokenType.ARRAY) {
                programBuilder.append(String.format("\taload %d\n", s.localId));
                typeStack.push(Type.ARRAY);
            }
        }
    }

    public void stackPushArrayEl(Token term, Token arrayIndex) {
        this.stackPush(arrayIndex);
        Symbol s = scope.resolve(term.lexeme());
        programBuilder.append(String.format("\taload %d\n", s.localId));
        programBuilder.append("\tswap\n");
        programBuilder.append("\tiaload\n");
        typeStack.push(Type.INT);
    }

    public void stackPeekMultiplyByMinusOne() {
        if (typeStack.peek() == Type.INT) {
            programBuilder.append("\tbipush -1\n");
            programBuilder.append("\timul\n");
            typeStack.pop();
            typeStack.push(Type.INT);
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tldc2_w -1.0\n");
            programBuilder.append("\tdmul\n");
            typeStack.pop();
            typeStack.push(Type.DOUBLE);
        }
    }

    public void stackPeekNegate() {
        programBuilder.append("\tbipush 1\n");
        programBuilder.append("\tixor\n");
        typeStack.pop();
        typeStack.push(Type.INT);
    }

    public void multiply() {
        if (typeStack.peek() == Type.INT) {
            programBuilder.append("\timul\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.INT);
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdmul\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.DOUBLE);
        }
    }

    public void mod() {
        if (typeStack.peek() == Type.INT) {
            programBuilder.append("\tirem\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.INT);
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdrem\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.DOUBLE);
        }
    }

    public void divide() {
        if (typeStack.peek() == Type.INT) {
            programBuilder.append("\tidiv\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.INT);
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tddiv\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.DOUBLE);
        }
    }

    public void addition() {
        if (typeStack.peek() == Type.INT) {
            programBuilder.append("\tiadd\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.INT);
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdadd\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.DOUBLE);
        }
    }

    public void subtraction() {
        if (typeStack.peek() == Type.INT) {
            programBuilder.append("\tisub\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.INT);
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdsub\n");
            typeStack.pop();
            typeStack.pop();
            typeStack.push(Type.DOUBLE);
        }
    }

    public void compareEQ() {
        var labelId = labelCount++;

        if (typeStack.peek() == Type.INT) {
            programBuilder.append(String.format("\tif_icmpne FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdcmpl\n");
            programBuilder.append(String.format("\tifne FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        }


        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));

        typeStack.push(Type.INT);
    }

    public void compareNEQ() {
        var labelId = labelCount++;

        if (typeStack.peek() == Type.INT) {
            programBuilder.append(String.format("\tif_icmpeq FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdcmpl\n");
            programBuilder.append(String.format("\tifeq FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        }

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));

        typeStack.push(Type.INT);
    }

    public void compareGT() {
        var labelId = labelCount++;

        if (typeStack.peek() == Type.INT) {
            programBuilder.append(String.format("\tif_icmple FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdcmpl\n");
            programBuilder.append(String.format("\tifle FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        }

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));

        typeStack.push(Type.INT);
    }

    public void compareLT() {
        var labelId = labelCount++;

        if (typeStack.peek() == Type.INT) {
            programBuilder.append(String.format("\tif_icmpge FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdcmpl\n");
            programBuilder.append(String.format("\tifge FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        }

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));

        typeStack.push(Type.INT);
    }

    public void compareGE() {
        var labelId = labelCount++;

        if (typeStack.peek() == Type.INT) {
            programBuilder.append(String.format("\tif_icmplt FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdcmpl\n");
            programBuilder.append(String.format("\tiflt FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        }

        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));

        typeStack.push(Type.INT);
    }

    public void compareLE() {
        var labelId = labelCount++;

        if (typeStack.peek() == Type.INT) {
            programBuilder.append(String.format("\tif_icmpgt FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        } else if (typeStack.peek() == Type.DOUBLE) {
            programBuilder.append("\tdcmpl\n");
            programBuilder.append(String.format("\tifgt FALSE_BRANCH_%d\n", labelId));
            typeStack.pop();
            typeStack.pop();
        }
        programBuilder.append("\ticonst_1\n");
        programBuilder.append(String.format("\tgoto CMP_END_%d\n",  labelId));

        programBuilder.append(String.format("FALSE_BRANCH_%d:\n", labelId));
        programBuilder.append("\ticonst_0\n");

        programBuilder.append(String.format("CMP_END_%d:\n",  labelId));

        typeStack.push(Type.INT);
    }

    public void and() {
        programBuilder.append("\tiand\n");
        typeStack.pop();
        typeStack.pop();
        typeStack.push(Type.INT);
    }

    public void or() {
        programBuilder.append("\tior\n");
        typeStack.pop();
        typeStack.pop();
        typeStack.push(Type.INT);
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
        if (methodSymbol.returnType == Type.VOID) {
            programBuilder.append("\treturn\n");
            return;
        }

        if (methodSymbol.returnType == Type.INT || methodSymbol.returnType == Type.BOOL)
            programBuilder.append("\tireturn\n");
        if (methodSymbol.returnType == Type.STRING)
            programBuilder.append("\tareturn\n");
        if (methodSymbol.returnType == Type.DOUBLE)
            programBuilder.append("\tdreturn\n");
        if (methodSymbol.returnType == Type.ARRAY)
            programBuilder.append("\tareturn\n");
    }

    public void endFunc() {
    }
}
