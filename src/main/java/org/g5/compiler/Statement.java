package org.g5.compiler;

import org.antlr.v4.runtime.ParserRuleContext;

public class Statement {
    private static final Integer spacesPerTab = 4;
    protected final Integer indentation;
    protected String line;
    protected final ParserRuleContext context;

    public Statement(int indentation, String line, ParserRuleContext context) {
        if (indentation < 0) throw new IllegalArgumentException("Indentation must be a non-negative integer: " + line);
        this.indentation = indentation;
        this.line = line;
        this.context = context;
    }

    public Statement(ParserRuleContext context) {
        this.indentation = 0;
        this.line = "";
        this.context = context;
    }

    public Statement(int indentation, String line) {
        this.indentation = indentation;
        this.line = line;
        this.context = null;
    }

    public ParserRuleContext getContext() {
        return this.context;
    }

    private String getIndentation() {
        return " ".repeat((this.indentation) * spacesPerTab);
    }

    @Override
    public String toString() {
        return this.getIndentation() + this.line;
    }
}
