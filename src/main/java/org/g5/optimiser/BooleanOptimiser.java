package org.g5.optimiser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.g5.BaseVisitor;
import org.g5.parser.GrammarParser.*;
import org.g5.typecheck.types.BooleanExpression;

public class BooleanOptimiser extends BaseVisitor {
    private static final String booleanFormat = "(%s %s %s)";

    @Override
    public Object visitBoolConstTrue(BoolConstTrueContext context) {
        return "True";
    }

    @Override
    public Object visitBoolConstFalse(BoolConstFalseContext context) {
        return "False";
    }

    @Override
    public Object visitBool(BoolContext context) {
        return expressionToBoolean(context.andor());
    }

    @Override
    public Object visitAndor(AndorContext context) {
        String ordering = (String) visit(context.ordering(0));

        for (int i = 1; i < context.ordering().size(); i++) {
            ordering = String.format(booleanFormat, ordering, context.andorOp(i - 1).getText(), (String) visit(context.ordering(i)));
        }

        return ordering;
    }

    @Override
    public Object visitOrderingOrdering(OrderingOrderingContext context) {
        return String.format(booleanFormat, visit(context.boolConst(0)), context.orderingOp().getText(), visit(context.boolConst(1)));
    }

    @Override
    public Object visitBoolConstBool(BoolConstBoolContext context) {
        return String.format("(%s)", visit(context.bool()));
    }

    @Override
    public Object visitBoolConstNumber(BoolConstNumberContext context) {
        NumericOptimiser optimiser = new NumericOptimiser();
        return optimiser.visit(context.number());
    }

    private BooleanExpression expressionToBoolean(ParserRuleContext context) {
        Object object = visit(context);
        if (object.getClass().isAssignableFrom(Boolean.class)) return new BooleanExpression((Boolean) object);
        if (object.getClass().isAssignableFrom(String.class)) return new BooleanExpression((String) object);
        return (BooleanExpression) object;
    }
}
