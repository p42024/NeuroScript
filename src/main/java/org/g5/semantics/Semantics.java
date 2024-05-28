package org.g5.semantics;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.g5.BaseVisitor;
import org.g5.exceptions.ScopeNotBreakableException;
import org.g5.optimiser.ModelOptimiser;
import org.g5.parser.GrammarParser.*;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.types.model.SequentialModel;

public class Semantics extends BaseVisitor {
    private ScopedTypeTable scopedTypeTable;

    public void check(ParseTree tree, ScopedTypeTable scopedTypeTable) {
        this.scopedTypeTable = scopedTypeTable;
        this.visit(tree);
    }

    /*==================================================================*
     *                              Model                               *
     *==================================================================*/

    @Override
    public Object visitSequentialContainerModel(SequentialContainerModelContext context) {
        matchSequentialModelLayers(context);
        return null;
    }

    /**
     * Checks if the models layers input and output sizes are compatible
     */
    private void matchSequentialModelLayers(SequentialContainerModelContext context) {
        SequentialModel model = this.scopedTypeTable.getTypeFromContext(context, context.getText(), SequentialModel.class);
        SequentialModel optimisedModel = new ModelOptimiser().sequentialModel(model, this.scopedTypeTable, context);

        this.scopedTypeTable.insertValue(context, String.valueOf(context.hashCode()), optimisedModel);
    }

    /*==================================================================*
     *                              Break                               *
     *==================================================================*/

    protected void isContextBreakable(ParserRuleContext context) {
        while (!context.parent.isEmpty()) {
            context = context.getParent();
            if (context instanceof StatementDefineLoopContext || context instanceof StatementTrainLoopContext) {
                return;
            }
        }
        throw new ScopeNotBreakableException(context.getStart().getLine());
    }

    @Override
    public Object visitStatementDefineBreak(StatementDefineBreakContext ctx) {
        isContextBreakable(ctx);
        return null;
    }

    @Override
    public Object visitStatementTrainBreak(StatementTrainBreakContext ctx) {
        isContextBreakable(ctx);
        return null;
    }
}
