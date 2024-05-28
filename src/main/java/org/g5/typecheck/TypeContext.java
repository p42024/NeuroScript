package org.g5.typecheck;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.g5.BaseVisitor;
import org.g5.exceptions.*;
import org.g5.parser.GrammarParser;
import org.g5.typecheck.types.BooleanExpression;
import org.g5.typecheck.types.NumberExpression;
import org.g5.typecheck.types.model.Model;
import org.g5.typecheck.types.training.Data;
import org.g5.typecheck.types.training.Loss;
import org.g5.typecheck.types.training.NumberArray;

public class TypeContext extends BaseVisitor {
    protected ScopedTypeTable types;
    protected ParseTree tree;

    public TypeContext(ParseTree tree) {
        this.tree = tree;
        types = new ScopedTypeTable(tree);
    }

    protected <T> void ensureContextType(ParserRuleContext context, ParserRuleContext specificContext, Class<T> expectedTypeClass) {
        Object result = visit(specificContext);
        if (result == null) throw new NoTypeException(context.getStart().getLine(), specificContext.getText());
        if (!expectedTypeClass.isInstance(result) && !expectedTypeClass.isAssignableFrom(result.getClass()))
            throw new AssignmentTypeException(context.getStart().getLine(), specificContext.getText(), result, expectedTypeClass);
    }

    protected <T> T getAndCheckContextType(ParserRuleContext context, ParserRuleContext specificContext, Class<T> expectedTypeClass) {
        ensureContextType(context, specificContext, expectedTypeClass);
        return (T) visit(specificContext);
    }

    protected <T> T evaluateContext(ParserRuleContext context, ParserRuleContext specificContext, Class<T> expectedTypeClass) {
        T result = getAndCheckContextType(context, specificContext, expectedTypeClass);
        return result;
    }

    protected String evaluateStringContext(ParserRuleContext context, ParserRuleContext stringContext) {
        return evaluateContext(context, stringContext, String.class);
    }

    protected BooleanExpression evaluateBoolContext(ParserRuleContext context, ParserRuleContext boolContext) {
        return evaluateContext(context, boolContext, BooleanExpression.class);
    }

    protected NumberExpression evaluateNumericContext(ParserRuleContext context, ParserRuleContext numericContext) {
        return evaluateContext(context, numericContext, NumberExpression.class);
    }

    protected Model evaluateModelContext(ParserRuleContext context, ParserRuleContext modelContext) {
        return evaluateContext(context, modelContext, Model.class);
    }

    protected NumberArray evaluateNumberArrayContext(ParserRuleContext context, ParserRuleContext numberArrayContext) {
        return evaluateContext(context, numberArrayContext, NumberArray.class);
    }

    public Loss evaluateLossContext(ParserRuleContext context, ParserRuleContext lossContext) {
        return evaluateContext(context, lossContext, Loss.class);
    }

    public Data evaluatePredictionContext(ParserRuleContext context, ParserRuleContext predictionContext) {
        return evaluateContext(context, predictionContext, Data.class);
    }
    
    protected void evaluateStringOrVariableString(ParserRuleContext context, GrammarParser.StringOrIdContext stringOrIdContext) {
        if (stringOrIdContext.string() != null) evaluateStringContext(context, stringOrIdContext.string());
        else if (stringOrIdContext.id() != null) evaluateStringContext(context, stringOrIdContext.id());
        else throw new ImpossibleException(String.format(
                    "Statement at line %s has no string or id",
                    context.getStart().getLine()
            ));
    }

    protected <T> T getVariableOfTypeFromContext(ParserRuleContext context, String id, Class<T> expectedTypeClass) {
        Object type = getVariableFromContext(context, id);
        if (!expectedTypeClass.isInstance(type.getClass())) return (T) type;
        throw new AssignmentTypeException(context.getStart().getLine(), id, type, expectedTypeClass);
    }

    protected Object getVariableFromContext(ParserRuleContext context, String id) {
        Object type = this.types.getTypeFromContext(context, id);
        if (type == null) throw new NoSuchVariableException(context.getStart().getLine(), id);
        return type;
    }

    public ScopedTypeTable getScopedTypeTable() {
        return types;
    }
}
