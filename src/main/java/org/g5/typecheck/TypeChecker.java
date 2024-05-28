package org.g5.typecheck;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.g5.exceptions.NoTypeException;
import org.g5.exceptions.WrongArgumentException;
import org.g5.exceptions.WrongTypeException;
import org.g5.optimiser.BooleanOptimiser;
import org.g5.optimiser.NumericOptimiser;
import org.g5.parser.GrammarParser.*;
import org.g5.typecheck.types.*;
import org.g5.typecheck.types.model.*;
import org.g5.typecheck.types.training.*;


public class TypeChecker extends TypeContext {
    public TypeChecker(ParseTree tree) {
        super(tree);
    }

    public void typeCheck() {
        this.visit(tree);
    }

    /*==================================================================*
     *                        Scoped statements                         *
     *==================================================================*/
    public void visitScopedStatement(ParserRuleContext context) {
        TypeTable table = this.types.copyParentOrCreateTypeTable(context);
        types.put(context, table);
        visitChildren(context);
    }

    @Override
    public Object visitScopeTrain(ScopeTrainContext context) {
        this.visitScopedStatement(context);
        return null;
    }

    @Override
    public Object visitScopeDefine(ScopeDefineContext context) {
        this.visitScopedStatement(context);
        return null;
    }

    /*==================================================================*
     *                                 Id                               *
     *==================================================================*/

    @Override
    public Object visitExpressionId(ExpressionIdContext context) {
        return getVariableFromContext(context, context.id().getText());
    }

    @Override
    public Object visitExpressionTrainId(ExpressionTrainIdContext context) {
        return getVariableFromContext(context, context.id().getText());
    }

    @Override
    public Object visitNumberConstantId(NumberConstantIdContext context) {
        return getVariableOfTypeFromContext(context, context.id().getText(), NumberExpression.class);
    }

    @Override
    public Object visitBoolConstId(BoolConstIdContext context) {
        return getVariableOfTypeFromContext(context, context.id().getText(), BooleanExpression.class);
    }

    @Override
    public Object visitSequentialConstId(SequentialConstIdContext context) {
        return getVariableOfTypeFromContext(context, context.id().getText(), Layer.class);
    }

    /*==================================================================*
     *                             Assignment                           *
     *==================================================================*/

    private void assign(ParserRuleContext context, String id) {
        Object type = visit(context);
        if (type == null) throw new NoTypeException(context.getStart().getLine(), context.getText());
        this.types.insertValue(context, id, type);
    }

    @Override
    public Object visitStatementDefineAssignment(StatementDefineAssignmentContext context) {
        assign(context.expressionDefine(), context.id().getText());
        return null;
    }

    @Override
    public Object visitStatementTrainAssignment(StatementTrainAssignmentContext context) {
        assign(context.expressionTrain(), context.id().getText());
        return null;
    }

    /*==================================================================*
     *                                 If                               *
     *==================================================================*/
    @Override
    public Object visitStatementTrainIf(StatementTrainIfContext context) {
        ParserRuleContext boolContext = context.bool();
        evaluateBoolContext(context, boolContext);
        return super.visitStatementTrainIf(context);
    }

    @Override
    public Object visitStatementDefineIf(StatementDefineIfContext context) {
        ParserRuleContext boolContext = context.bool();
        evaluateBoolContext(context, boolContext);
        return super.visitStatementDefineIf(context);
    }

    /*==================================================================*
     *                              Import                              *
     *==================================================================*/

    @Override
    public Object visitStatementTrainImportModel(StatementTrainImportModelContext context) {
        evaluateStringOrVariableString(context, context.stringOrId());
        this.types.insertValue(context, context.id().getText(), new ImportModel());
        return super.visitStatementTrainImportModel(context);
    }

    @Override
    public Object visitStatementTrainImportData(StatementTrainImportDataContext context) {
        evaluateStringOrVariableString(context, context.stringOrId());
        this.types.insertValue(context, context.id().getText(), new Data(context.id().getText(), true));
        return super.visitStatementTrainImportData(context);
    }

    /*==================================================================*
     *                              Export                              *
     *==================================================================*/

    private void export(ParserRuleContext context, StringOrIdContext stringOrIdContext, ParserRuleContext expressionTrainContext) {
        evaluateModelContext(context, expressionTrainContext);
        evaluateStringOrVariableString(context, stringOrIdContext);
    }

    @Override
    public Object visitStatementTrainExport(StatementTrainExportContext context) {
        export(context, context.stringOrId(), context.expressionTrain());
        return super.visitStatementTrainExport(context);
    }

    @Override
    public Object visitStatementDefineExport(StatementDefineExportContext context) {
        export(context, context.stringOrId(), context.expressionDefine());
        return super.visitStatementDefineExport(context);
    }

    /*==================================================================*
     *                       Numeric statements                         *
     *==================================================================*/

    @Override
    public Object visitNumber(NumberContext context) {
        //@TODO - Ensure type of ids
        context.children.stream().filter(parseTree -> parseTree instanceof IdContext).forEach(this::visit);

        NumericOptimiser optimiser = new NumericOptimiser();
        return optimiser.visit(context);
    }

    /*==================================================================*
     *                          Number array                            *
     *==================================================================*/

    @Override
    public NumberArray visitNumberArray(NumberArrayContext context) {
        NumberArray numberArray = new NumberArray();
        int index = 0;
        for (NumberContext numberContext : context.number()) {
            NumberExpression number = getAndCheckContextType(context, numberContext, NumberExpression.class);
            if (number.isInt())
                throw new WrongTypeException(context.getStart().getLine(),
                        Integer.class,
                        Float.class,
                        String.format("The number %s at index %d, can be written as %s.0", number, index, number)
                );
            numberArray.addNumber(number);
            index++;
        }
        return numberArray;
    }

    /*==================================================================*
     *                       Boolean statements                         *
     *==================================================================*/

    @Override
    public Object visitBool(BoolContext context) {
        context.children.stream().filter(parseTree -> parseTree instanceof IdContext).forEach(this::visit);

        BooleanOptimiser optimiser = new BooleanOptimiser();
        return optimiser.visit(context);
    }

    /*==================================================================*
     *                              Model                               *
     *==================================================================*/

    @Override
    public Data visitExpressionModelCall(ExpressionModelCallContext context) {
        evaluateNumberArrayContext(context, context.expressionTrain());
        getVariableOfTypeFromContext(context, context.id().getText(), Model.class);
        return new Data(context.id().getText());
    }

    @Override
    public Object visitSequentialContainerModel(SequentialContainerModelContext context) {
        SequentialModel model = new SequentialModel();
        int argCount = 0;
        for (ParseTree child : context.children) {
            // Skip "->"
            if (child.getClass().isAssignableFrom(TerminalNodeImpl.class)) continue;
            String id = null;
            if (child.getClass().isAssignableFrom(SequentialConstIdContext.class)) id = child.getText();
            Object object = visit(child);
            if (object instanceof Model layer) {
                if (child.getClass().isAssignableFrom(SequentialConstIdContext.class)) {
                    layer.setExpression(((SequentialConstIdContext) child).id().getText());
                }
                model.addLayer(layer);
            } else if (object instanceof Activation activation) {
                Activation copy = activation.deepCopy();
                if (id != null) {
                    copy.setId(id);
                }
                model.addLayer(copy);
            } else {
                throw new WrongArgumentException(context.getStart().getLine(), "sequential", argCount, "Model, Layer or Activation", object.toString());
            }
            argCount++;
        }
        this.types.insertValue(context, context.getText(), model);
        return model;
    }

    /*==================================================================*
     *                              Layer                               *
     *==================================================================*/

    @Override
    public LinearLayer visitLinearLayer(LinearLayerContext context) {
        NumberExpression input = evaluateNumericContext(context, context.linearLayerConst(0));
        NumberExpression output = evaluateNumericContext(context, context.linearLayerConst(1));
        if (input.isNumeric() && !input.isInt())
            throw new WrongTypeException(context.getStart().getLine(), input.getClass(), Integer.class, input.toString());
        if (output.isNumeric() && !output.isInt())
            throw new WrongTypeException(context.getStart().getLine(), input.getClass(), Integer.class, input.toString());
        if (input.isNumeric() && input.getInt() < 0)
            throw new WrongArgumentException(context.getStart().getLine(), "linear", 1, "positive integer", input.toString());
        if (output.isNumeric() && output.getInt() < 0)
            throw new WrongArgumentException(context.getStart().getLine(), "linear", 0, "positive integer", input.toString());

        return new LinearLayer(input, output);
    }

    /*==================================================================*
     *                            Activation                            *
     *==================================================================*/

    @Override
    public Activation visitActivationReLU(ActivationReLUContext context) {
        return new Activation(ActivationTypes.ReLU);
    }

    @Override
    public Activation visitActivationSigmoid(ActivationSigmoidContext context) {
        return new Activation(ActivationTypes.Sigmoid);
    }

    @Override
    public Activation visitActivationTanh(ActivationTanhContext context) {
        return new Activation(ActivationTypes.Tanh);
    }

    /*==================================================================*
     *                            Optimiser                             *
     *==================================================================*/

    private Optimiser getOptimiser(ParserRuleContext context, ExpressionTrainContext modelExp, ExpressionTrainContext lossExp, ExpressionTrainContext lrExp, OptimiserTypes type) {
        Model model = evaluateModelContext(context, modelExp);
        Loss loss = evaluateLossContext(context, lossExp);
        NumberExpression learningRate = evaluateNumericContext(context, lrExp);
        return new Optimiser(type, model, loss, learningRate);
    }


    @Override
    public Object visitStatementTrainSGD(StatementTrainSGDContext context) {
        Optimiser optimiser = getOptimiser(context, context.expressionTrain(0), context.expressionTrain(1), context.expressionTrain(2), OptimiserTypes.SGD);
        return null;
    }

    /*==================================================================*
     *                              Loss                                *
     *==================================================================*/

    @Override
    public Object visitExpressionMSE(ExpressionMSEContext context) {
        Data prediction = evaluatePredictionContext(context, context.expressionTrain(0));
        NumberArray data = evaluateNumberArrayContext(context, context.expressionTrain(1));
        return new Loss(LossTypes.MSE, prediction, data);
    }

    @Override
    public Object visitExpressionCrossEntropy(ExpressionCrossEntropyContext context) {
        Data prediction = evaluatePredictionContext(context, context.expressionTrain(0));
        NumberArray data = evaluateNumberArrayContext(context, context.expressionTrain(1));
        return new Loss(LossTypes.CrossEntropy, prediction, data);
    }

    /*==================================================================*
     *                       Calculate Accuracy                         *
     *==================================================================*/

    @Override
    public Object visitExpressionAccuracy(ExpressionAccuracyContext context) {
        Data prediction = evaluatePredictionContext(context, context.expressionTrain(0));
        NumberArray data = evaluateNumberArrayContext(context, context.expressionTrain(1));
        return new CalculateAccuracy(context.getText(), prediction, data);
    }
}
