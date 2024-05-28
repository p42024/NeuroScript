package org.g5.optimiser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.g5.BaseVisitor;
import org.g5.parser.GrammarParser.*;
import org.g5.typecheck.types.NumberExpression;

public class NumericOptimiser extends BaseVisitor {
    private static final String arithmeticFormat = "(%s %s %s)";

    @Override
    public NumberExpression visitNumber(NumberContext context) {
        return expressionToNumber(context.additive());
    }

    private NumberExpression processExpression(NumberExpression left, NumberExpression right, String operator) {
        if (left.isNumeric() && right.isNumeric()) {
            if (left.isInt() && right.isInt()) {
                switch (operator) {
                    case "+" -> left.setNumber(left.getInt() + right.getInt());
                    case "-" -> left.setNumber(left.getInt() - right.getInt());
                    case "*" -> left.setNumber(left.getInt() * right.getInt());
                    case "/" -> left.setNumber(left.getNumber() / right.getNumber());
                    default -> throw new IllegalArgumentException("Invalid operator");
                }
            } else {
                switch (operator) {
                    case "+" -> left.setNumber(left.getNumber() + right.getNumber());
                    case "-" -> left.setNumber(left.getNumber() - right.getNumber());
                    case "*" -> left.setNumber(left.getNumber() * right.getNumber());
                    case "/" -> left.setNumber(left.getNumber() / right.getNumber());
                    default -> throw new IllegalArgumentException("Invalid operator");
                }
            }
        } else {
            left.setExpression(String.format(arithmeticFormat, left, operator, right));
        }
        return left;
    }

    @Override
    public Object visitAdditive(AdditiveContext context) {
        NumberExpression left = (NumberExpression) visit(context.multiplicative(0));
        for (int i = 1; i < context.multiplicative().size(); i++) {
            NumberExpression right = (NumberExpression) visit(context.multiplicative(i));
            left = processExpression(left, right, context.addOp(i - 1).getText());
        }
        return left;
    }

    @Override
    public Object visitMultiplicative(MultiplicativeContext context) {
        NumberExpression left = expressionToNumber(context.numberConstant(0));
        for (int i = 1; i < context.numberConstant().size(); i++) {
            NumberExpression right = expressionToNumber(context.numberConstant(i));
            left = processExpression(left, right, context.multOp(i - 1).getText());
        }
        return left;
    }

    private NumberExpression expressionToNumber(ParserRuleContext context) {
        Object object = visit(context);
        if (object.getClass().isAssignableFrom(Float.class)) return new NumberExpression((Float) object);
        if (object.getClass().isAssignableFrom(Integer.class)) return new NumberExpression((Integer) object);
        if (object.getClass().isAssignableFrom(String.class)) return new NumberExpression((String) object);
        return (NumberExpression) object;
    }
}
