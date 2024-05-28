package org.g5;

import org.g5.parser.GrammarBaseVisitor;
import org.g5.parser.GrammarParser.*;

public class BaseVisitor extends GrammarBaseVisitor<Object> {
    @Override
    public String visitId(IdContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitString(StringContext ctx) {
        String txt = ctx.getText();

        txt = txt.substring(1, txt.length() - 1);

        return txt;
    }

    @Override
    public Integer visitInt(IntContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public Float visitFloat(FloatContext ctx) {
        return Float.parseFloat(ctx.getText());
    }

    @Override
    public Object visitNumberConstantParenthesis(NumberConstantParenthesisContext ctx) {
        return this.visit(ctx.number());
    }
}
