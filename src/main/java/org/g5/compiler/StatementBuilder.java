package org.g5.compiler;

import org.antlr.v4.misc.OrderedHashMap;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.g5.BaseVisitor;
import org.g5.optimiser.BooleanOptimiser;
import org.g5.optimiser.NumericOptimiser;
import org.g5.parser.GrammarParser.*;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.types.NumberExpression;
import org.g5.typecheck.types.model.Activation;
import org.g5.typecheck.types.model.ActivationTypes;
import org.g5.typecheck.types.model.LinearLayer;
import org.g5.typecheck.types.model.SequentialModel;

import java.util.ArrayList;
import java.util.List;

public class StatementBuilder extends BaseVisitor {
    protected Integer beginningStatementsNextIndex;
    private final OrderedHashMap<Integer, Statement> statements;
    protected final ScopedTypeTable scopedTypeTable;

    private int trainSectionCount;
    private int lastIndex;
    protected int currentDepth = 0;
    protected Boolean importBool = false;

    public StatementBuilder(ScopedTypeTable scopedTypeTable) {
        this.statements = new OrderedHashMap<>();
        this.trainSectionCount = 0;
        this.scopedTypeTable = scopedTypeTable;
        this.beginningStatementsNextIndex = Integer.MIN_VALUE;
    }

    public String build(ParseTree tree) {
        this.visit(tree);
        StringBuilder builder = new StringBuilder();
        this.statements.keySet().stream().sorted().forEach(key -> builder.append(this.statements.get(key).toString()).append("\n"));
        return builder.toString();
    }

    protected void addToBeginningStatements(int depth, String line, ParserRuleContext context) {
        Statement statement = new Statement(depth, line, context);
        this.addToStatements(this.beginningStatementsNextIndex, statement, false);
        this.beginningStatementsNextIndex++;
    }

    private void addToStatements(int index, Statement statement) {
        this.addToStatements(index, statement, true);
    }

    private void addToStatements(int index, Statement statement, boolean changeIndex) {
        while (this.statements.containsKey(index)) index++;
        this.statements.put(index, statement);
        if (changeIndex) lastIndex = index;
    }


    protected void addToStatements(String line, ParserRuleContext context) {
        Statement statement = new Statement(currentDepth, line, context);
        this.addToStatements(context.start.getStartIndex(), statement);
    }

    protected void addToStatementsNextLine(String line, ParserRuleContext context) {
        Statement statement = new Statement(currentDepth, line, context);
        lastIndex = lastIndex + 1;
        this.addToStatements(lastIndex, statement);
    }

    protected void addToStatementsNextLine(Statement statement) {
        lastIndex = lastIndex + 1;
        this.addToStatements(lastIndex, statement);
    }

    @Override
    public String visitString(StringContext ctx) {
        return '"' + super.visitString(ctx) + '"';
    }

    /*==================================================================*
     *                             Assignment                           *
     *==================================================================*/

    protected void addAssignmentStatement(ParserRuleContext expression, String id, ParserRuleContext context) {
        Object object = this.visit(expression);
        if (object == null) return;
        String line = id + " = " + object;
        this.addToStatements(line, context);
    }

    @Override
    public Object visitStatementDefineAssignment(StatementDefineAssignmentContext context) {
        this.addAssignmentStatement(context.expressionDefine(), context.id().getText(), context);
        return null;
    }

    /*==================================================================*
     *                                 If                               *
     *==================================================================*/

    /* ========================= If statement  =========================*/
    protected void addIfStatement(BoolContext bool, ParserRuleContext context) {
        BooleanOptimiser booleanOptimiser = new BooleanOptimiser();
        String line = "if " + booleanOptimiser.visitBool(bool) + ":";
        this.addToStatements(line, context);
    }

    @Override
    public Object visitStatementDefineIf(StatementDefineIfContext context) {
        this.addIfStatement(context.bool(), context);
        increaseDepth();
        super.visitScopeDefine(context.scopeDefine());
        decreaseDepth();
        if (context.elseDefine() != null) {
            this.visitElseDefine(context.elseDefine());
        }
        return null;
    }

    /* ======================= Else statement =========================*/

    protected void addElseStatement(ParserRuleContext context) {
        // Decrease the depth of the previous if statement
        String line = "else:";
        this.addToStatements(line, context);
    }

    @Override
    public Object visitElseDefine(ElseDefineContext context) {
        addElseStatement(context);
        increaseDepth();
        this.visitScopeDefine(context.scopeDefine());
        decreaseDepth();
        return null;
    }

    /* ======================= Break statement ========================*/

    protected void addBreakStatement(ParserRuleContext context) {
        String line = "break";
        this.addToStatements(line, context);
    }

    @Override
    public Object visitStatementDefineBreak(StatementDefineBreakContext context) {
        this.addBreakStatement(context);
        return null;
    }

    /*==================================================================*
     *                               Loop                               *
     *==================================================================*/

    protected void addLoopStatement(ParserRuleContext context) {
        addEmptyLine(context);
        String line = "while True:";
        this.addToStatementsNextLine(line, context);
    }

    @Override
    public Object visitStatementDefineLoop(StatementDefineLoopContext context) {
        this.addLoopStatement(context);
        increaseDepth();
        this.visitScopeDefine(context.scopeDefine());
        decreaseDepth();
        return null;
    }

    /*==================================================================*
     *                         Import / export                          *
     *==================================================================*/

    @Override
    public String visitStringOrId(StringOrIdContext context) {
        if (context.string() != null) return context.string().getText();
        return this.scopedTypeTable.getTypeFromContext(context, context.id().getText()).toString();
    }

    protected void addModelImportStatement(StringOrIdContext context, ParserRuleContext idContext) {
        String name = visitStringOrId(context).replaceAll("\"", "");

        this.addToStatements(idContext.getText() + " = torch.load('" + name + "')", context);
        this.addToStatementsNextLine(idContext.getText() + ".eval()", context);
    }

    protected void addExportStatement(ParserRuleContext context, StringOrIdContext idContext) {
        String name = visitStringOrId(idContext).replaceAll("\"", "");
        String line = "torch.save(" + context.getText() + ", os.getcwd() + " + "'\\\\" + name + ".pt'" + ")";
        this.addToStatements(line, context);
    }

    @Override
    public Object visitStatementDefineExport(StatementDefineExportContext context) {
        this.addExportStatement(context.expressionDefine(), context.stringOrId());
        return null;
    }

    @Override
    public Object visitStatementDefineImportModel(StatementDefineImportModelContext ctx) {
        addModelImportStatement(ctx.stringOrId(), ctx.id());
        return null;
    }

    /*==================================================================*
     *                              Train                               *
     *==================================================================*/

    @Override
    public Object visitTrain(TrainContext context) {
        TrainStatement trainStatement = new TrainStatement(context, this.trainSectionCount, this.scopedTypeTable);
        if (!this.importBool && trainStatement.importBool) {
            this.importBool = true;
            addToBeginningStatements(0, new CustomDatasetStatement().toString(), null);
        }
        this.statements.put(context.start.getStartIndex(), trainStatement);
        this.trainSectionCount++;
        return null;
    }

    /*==================================================================*
     *                            Activation                            *
     *==================================================================*/

    @Override
    public String visitActivationReLU(ActivationReLUContext context) {
        return ActivationTypes.ReLU.toString();
    }

    @Override
    public String visitActivationSigmoid(ActivationSigmoidContext context) {
        return ActivationTypes.Sigmoid.toString();
    }

    @Override
    public String visitActivationTanh(ActivationTanhContext context) {
        return ActivationTypes.Tanh.toString();
    }

    /*==================================================================*
     *                              Layer                               *
     *==================================================================*/

    @Override
    public String visitLinearLayer(LinearLayerContext context) {
        NumberExpression input = (NumberExpression) visit(context.linearLayerConst(0));
        NumberExpression output = (NumberExpression) visit(context.linearLayerConst(1));

        return new LinearLayer(input, output).toString();
    }

    /*==================================================================*
     *                              Model                               *
     *==================================================================*/

    @Override
    public String visitSequentialContainerModel(SequentialContainerModelContext context) {
        SequentialModel model = scopedTypeTable.getTypeFromContext(context, String.valueOf(context.hashCode()), SequentialModel.class);
        return model.toString();
    }

    /*==================================================================*
     *                       Numeric statements                         *
     *==================================================================*/

    @Override
    public Object visitNumber(NumberContext context) {
        NumericOptimiser optimiser = new NumericOptimiser();
        return optimiser.visit(context);
    }

    /*==================================================================*
     *                       Boolean statements                         *
     *==================================================================*/

    @Override
    public Object visitBool(BoolContext ctx) {
        BooleanOptimiser optimiser = new BooleanOptimiser();
        return optimiser.visitBool(ctx);
    }

    /*==================================================================*
     *                          Number array                            *
     *==================================================================*/

    @Override
    public String visitNumberArray(NumberArrayContext context) {
        List<String> array = new ArrayList<>();
        for (NumberContext numberContext : context.number()) {
            Object number = visit(numberContext);
            array.add(number.toString());
        }
        String arrayString = array.stream().reduce((a, b) -> a + ", " + b).orElse("");
        return "torch.tensor([" + arrayString + "])";
    }

    /*==================================================================*
     *                              Print                               *
     *==================================================================*/

    protected void addPrintStatement(ParserRuleContext context) {
        Object arg = visit(context);
        this.addToStatementsNextLine(String.format("print(%s)", arg), context);
    }

    @Override
    public Object visitStatementDefinePrint(StatementDefinePrintContext ctx) {
        addPrintStatement(ctx.expressionDefine());
        return null;
    }

    /*==================================================================*
     *                          Depth Handler                           *
     *==================================================================*/

    protected void increaseDepth() {
        currentDepth++;
    }

    protected void decreaseDepth() {
        currentDepth--;
        if (currentDepth < 0) throw new IllegalArgumentException("Depth cannot be negative");
    }

    /*==================================================================*
     *                             Utility                              *
     *==================================================================*/

    protected void addEmptyLine(ParserRuleContext context) {
        this.addToStatements("", context);
    }

    protected void addEmptyNextLine(ParserRuleContext context) {
        this.addToStatementsNextLine("", context);
    }
}
