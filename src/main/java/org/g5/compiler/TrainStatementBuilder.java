package org.g5.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.g5.parser.GrammarParser.*;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.TypeTable;
import org.g5.typecheck.types.training.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TrainStatementBuilder extends StatementBuilder {
    private final HashSet<String> idsOutOfScope;
    private int loss_count;
    private int optimizerCount;
    private int accuracyCount;
    private final int trainBaseDepth = 1;
    private boolean training = false;
    private String trainingDataId;
    private List<Statement> afterTrainingStatements;
    private static final Class<?>[] trainingExpressions = {
            ExpressionMSEContext.class,
            ExpressionCrossEntropyContext.class,
            ExpressionAccuracyContext.class
    };

    public TrainStatementBuilder(ScopedTypeTable scopedTypeTable) {
        super(scopedTypeTable);
        this.idsOutOfScope = new HashSet<>();
        this.optimizerCount = 0;
        this.loss_count = 0;
        this.currentDepth = 1;
    }

    @Override
    public String build(ParseTree tree) {
        return super.build(tree);
    }

    public HashSet<String> getIdsOutOfScope() {
        return idsOutOfScope;
    }

    @Override
    public Object visitTrain(TrainContext context) {
        visitChildren(context);
        return null;
    }

    @Override
    public String visitId(IdContext context) {
        addId(context.ID().getText());
        return context.ID().getText();
    }

    private void addId(String id) {
        if (scopedTypeTable.isModelInBaseReference(id)) {
            idsOutOfScope.add(id);
        }
    }

    /*==================================================================*
     *                             Assignment                           *
     *==================================================================*/

    @Override
    public Object visitStatementTrainAssignment(StatementTrainAssignmentContext context) {
        String id = context.id().getText();
        addId(id);
        this.addAssignmentStatement(context.expressionTrain(), id, context);
        if (this.currentDepth == 1) this.beginningStatementsNextIndex = context.start.getStartIndex() + 1;
        continueTraining(context);
        return null;
    }

    /*==================================================================*
     *                                 If                               *
     *==================================================================*/

    /* ========================= If statement  =========================*/
    @Override
    public Object visitStatementTrainIf(StatementTrainIfContext context) {
        this.addIfStatement(context.bool(), context);
        increaseDepth();
        super.visitScopeTrain(context.scopeTrain());
        decreaseDepth();
        if (context.elseTrain() != null) {
            this.visitElseTrain(context.elseTrain());
        }
        return null;
    }

    /* ======================= Else statement =========================*/

    @Override
    public Object visitElseTrain(ElseTrainContext context) {
        addElseStatement(context);
        increaseDepth();
        this.visitScopeTrain(context.scopeTrain());
        decreaseDepth();
        return null;
    }

    /* ======================= Break statement ========================*/

    @Override
    public Object visitStatementTrainBreak(StatementTrainBreakContext context) {
        this.addBreakStatement(context);
        return null;
    }

    /*==================================================================*
     *                               Loop                               *
     *==================================================================*/

    @Override
    public Object visitStatementTrainLoop(StatementTrainLoopContext context) {
        this.addLoopStatement(context);
        increaseDepth();
        this.visitScopeTrain(context.scopeTrain());
        decreaseDepth();
        this.addEmptyNextLine(context);
        return null;
    }

    /*==================================================================*
     *                         Import / export                          *
     *==================================================================*/

    protected void addImportStatement(StringOrIdContext context, ParserRuleContext idContext) {
        if (context.getText().replaceAll("\"", "").equals("MNISTDigits")) {
            this.addToStatements("MNISTDigitsDataset = datasets.MNIST(root=\"data\", train=True, download=True, transform=ToTensor())", context);
            this.addToStatementsNextLine("MNISTDigits = DataLoader(MNISTDigitsDataset, batch_size=16)", context);
            addEmptyNextLine(context);
        } else if (context.getText().replaceAll("\"", "").equals("MNISTDigitsTest")) {
            this.addToStatements("MNISTDigitsTestDataset = datasets.MNIST(root=\"data\", train=False, download=True, transform=ToTensor())", context);
            this.addToStatementsNextLine("MNISTDigitsTest = DataLoader(MNISTDigitsTestDataset, batch_size=16)", context);
            addEmptyNextLine(context);
        } else {
            String name = visitStringOrId(context).replaceAll("\"", "");
            this.importBool = true;
            this.addToStatements(idContext.getText() + "_dataset = CustomDataset(csv_file='" + name + "', target_column='target')", context);
            this.addToStatements(idContext.getText() + " = DataLoader(" + idContext.getText() + "_dataset, batch_size=16, shuffle=True)", context);
        }
    }

    @Override
    public Object visitStatementTrainImportData(StatementTrainImportDataContext context) {
        this.addImportStatement(context.stringOrId(), context.id());
        return null;
    }

    @Override
    public Object visitStatementTrainImportModel(StatementTrainImportModelContext context) {
        this.addModelImportStatement(context.stringOrId(), context.id());
        return null;
    }

    @Override
    public Object visitStatementTrainExport(StatementTrainExportContext context) {
        this.addExportStatement(context.expressionTrain(), context.stringOrId());
        return null;
    }

    /*==================================================================*
     *                            Optimiser                             *
     *==================================================================*/

    @Override
    public Object visitStatementTrainSGD(StatementTrainSGDContext context) {
        String optimiser = String.format("torch.optim.SGD(%s.parameters(), lr=%s)", visit(context.expressionTrain(0)), visit(context.expressionTrain(2)));
        String id = "optimiser_" + optimizerCount;
        this.addToBeginningStatements(trainBaseDepth, id + " = " + optimiser, context);
        addToStatementsNextLine(String.format("%s.backward()", visit(context.expressionTrain(1))), context);
        addToStatementsNextLine(String.format("%s.step()", id), context);
        addToStatementsNextLine(String.format("%s.zero_grad()", id), context);
        continueTraining(context);
        optimizerCount++;
        return null;
    }

    /*==================================================================*
     *                              Loss                                *
     *==================================================================*/

    public String loss(String name, ExpressionTrainContext arg1, ExpressionTrainContext arg2, ParserRuleContext context) {
        String id = name + "_" + loss_count;
        this.addToBeginningStatements(trainBaseDepth, String.format("%s = nn.%sLoss()", id, name), context);
        loss_count++;
        String labels = "labels";
        if (!training) labels = visit(arg2).toString();
        return String.format("%s(%s, %s)", id, visit(arg1), labels);
    }

    @Override
    public String visitExpressionCrossEntropy(ExpressionCrossEntropyContext ctx) {
        return loss("CrossEntropy", ctx.expressionTrain(0), ctx.expressionTrain(1), ctx);
    }

    @Override
    public String visitExpressionMSE(ExpressionMSEContext context) {
        return loss("MSE", context.expressionTrain(0), context.expressionTrain(1), context);
    }

    /*==================================================================*
     *                              Print                               *
     *==================================================================*/

    @Override
    public Object visitStatementTrainPrint(StatementTrainPrintContext ctx) {
        addPrintStatement(ctx.expressionTrain());
        return null;
    }

    /*==================================================================*
     *                        Calculate accuracy                        *
     *==================================================================*/

    @Override
    public Object visitExpressionAccuracy(ExpressionAccuracyContext ctx) {
        if (!(ctx.getParent() instanceof StatementTrainAssignmentContext parent)) return null;
        String id = parent.id().getText();
        String pred = visit(ctx.expressionTrain(0)).toString();

        if (!training) {
            return String.format("(%s.argmax(1) == %s).type(torch.float).sum().item()", pred, visit(ctx.expressionTrain(1)));
        }

        // Create a variable to store the accumulating accuracy
        addToBeginningStatements(trainBaseDepth, String.format("%s_%s = 0", id, accuracyCount), ctx);

        // Accumulate the accuracy each batch
        String accuracyAccumulator = String.format("%s_%s += (%s.argmax(1) == labels).type(torch.float).sum().item()", id, accuracyCount, pred);
        addToStatementsNextLine(accuracyAccumulator, ctx);

        // Calculate the accuracy and store it in the original variable
        String calcAccuracyAvg = String.format("%s = %s_%s / len(%s.dataset)", id, id, accuracyCount, trainingDataId);
        afterTrainingStatements.add(new Statement(currentDepth - 1, calcAccuracyAvg, ctx));
        accuracyCount++;
        return null;
    }

    /*==================================================================*
     *                              Model                               *
     *==================================================================*/

    @Override
    public String visitExpressionModelCall(ExpressionModelCallContext context) {
        Object arg = visit(context.expressionTrain());
        TypeTable typeTable = scopedTypeTable.getTypeTableFromContextOrDefault(context);
        Object type = typeTable.getValue(arg.toString());
        if (type instanceof Data data && data.isMultiDimensional()) {
            if (training) decreaseDepth();
            this.addToStatementsNextLine(String.format("for data, labels in %s:", arg), context);
            startTraining(arg.toString());
            return String.format("%s(data)", context.id().getText());
        }

        addId(context.id().getText());

        return String.format("%s(%s)", context.id().getText(), arg);
    }

    /*==================================================================*
     *                     Training helper methods                      *
     *==================================================================*/

    private void startTraining(String id) {
        afterTrainingStatements = new ArrayList<>();
        trainingDataId = id;
        training = true;
        increaseDepth();
    }

    private void endTraining(ParserRuleContext context) {
        addEmptyNextLine(context);
        if (afterTrainingStatements != null) {
            afterTrainingStatements.forEach(this::addToStatementsNextLine);
        }
        trainingDataId = null;
        training = false;
        decreaseDepth();
        afterTrainingStatements = null;
    }

    /**
     * Finds the parent context (ScopeTrainContext) of a context
     *
     * @param context The context to find the parent of
     * @return The parent context (ScopeTrainContext) or null if not found
     */
    private ParserRuleContext findParent(ParserRuleContext context) {
        while (context != null && !(context instanceof ScopeTrainContext)) {
            context = context.getParent();
        }
        return context;
    }

    /**
     * Searches for a context in a child context
     *
     * @param toSearch The context to search (It's an object to allow both parserRuleContext and parseTree)
     * @param toFind   The context to find
     * @return The index of the child context if found, -1 otherwise
     */
    public int findContextInContext(Object toSearch, ParserRuleContext toFind) {
        if (!(toSearch instanceof ParserRuleContext context)) return -1;

        // Search in the children of the context for the toFind context
        for (int i = 0; i < context.getChildCount(); i++) {
            ParseTree childContext = context.getChild(i);
            if (childContext.equals(toFind)) {
                return i;
            }
            // Check if the children of the child has it in it
            if (childContext instanceof ParserRuleContext && findContextInContext(childContext, toFind) != -1) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Stops training if no more training statements are found in context
     *
     * @param context The context to check
     */
    public void continueTraining(ParserRuleContext context) {
        if (!training) return;
        // Find the parent context and the index of the current context
        ParserRuleContext parent = findParent(context);
        int index = parent != null ? findContextInContext(parent, context) : -1;
        if (index == -1) {
            endTraining(context);
            return;
        }

        // Check if there are more training statements in the parent context
        for (int i = index + 1; i < parent.getChildCount(); i++) {
            if (isTrainingStatement(parent.getChild(i))) return;
        }

        endTraining(context);
    }

    /**
     * Checks if a context is a training statement (MSE or SGD)
     *
     * @param context The context to check
     * @return True if training statement, false otherwise
     */
    private static boolean isTrainingStatement(ParseTree context) {
        if (context instanceof StatementTrainSGDContext) return true;
        if (context instanceof StatementTrainAssignmentContext assignment) {
            return Arrays.stream(trainingExpressions).anyMatch(expression -> expression.isInstance(assignment.expressionTrain()));
        }
        return false;
    }
}
