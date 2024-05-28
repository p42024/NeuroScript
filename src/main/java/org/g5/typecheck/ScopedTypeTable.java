package org.g5.typecheck;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.g5.exceptions.NoSuchVariableException;
import org.g5.typecheck.types.model.Model;

import java.util.Map;

public class ScopedTypeTable extends ParseTreeProperty<TypeTable> {
    private final ParseTree baseReference;

    public ScopedTypeTable(ParseTree tree) {
        annotations.put(tree, new TypeTable(0));
        baseReference = tree;
    }

    public void put(ParserRuleContext context, TypeTable table) {
        if (annotations.containsKey(context)) return;
        annotations.put(context, table);
    }


    public TypeTable copyParentOrCreateTypeTable(ParserRuleContext context) {
        TypeTable table = getTypeTableFromContext(context);
        if (table != null) return new TypeTable(table, context.getStart().getLine());
        return new TypeTable(context.getStart().getLine());
    }

    private TypeTable getTypeTableFromContext(ParserRuleContext context) {
        ParserRuleContext next = context;
        while (!context.parent.isEmpty()) {
            next = next.getParent();

            TypeTable table = annotations.get(next);
            if (table == null) continue;

            return table;
        }
        return null;
    }

    public TypeTable getTypeTableFromContextOrDefault(ParserRuleContext context) {
        TypeTable table = getTypeTableFromContext(context);
        if (table != null) return table;
        System.out.println("Returning default table");
        return annotations.get(this.baseReference);
    }

    public Object getTypeFromContext(ParserRuleContext ctx, String id) {
        TypeTable table = getTypeTableFromContextOrDefault(ctx);
        if (table.hasId(id)) {
            return table.getValue(id);
        }
        throw new NoSuchVariableException(ctx.start.getLine(), id);
    }

    public <T> T getTypeFromContext(ParserRuleContext ctx, String id, Class<T> type) {
        TypeTable table = getTypeTableFromContextOrDefault(ctx);
        if (table.hasId(id) && table.isType(id, type)) {
            return (T) table.getValue(id);
        }
        throw new NoSuchVariableException(ctx.start.getLine(), id);
    }

    public boolean isModelInBaseReference(String id) {
        TypeTable table = annotations.get(this.baseReference);
        return table.hasId(id) && table.getValue(id) instanceof Model;
    }

    public Boolean isIdInBaseReference(String id) {
        TypeTable table = annotations.get(this.baseReference);
        return table.hasId(id);
    }

    public TypeTable[] getScopes() {
        return annotations.values().toArray(new TypeTable[0]);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ScopedTypeTable { \n");
        for (Map.Entry<ParseTree, TypeTable> entry : annotations.entrySet()) {
            builder.append("\t[Line ")
                    .append(entry.getValue().getStartLine())
                    .append("] ")
                    .append(entry.getKey().toString())
                    .append(": [ \n\t\t")
                    .append(entry.getValue().toString().replace("\n", "\n\t\t"))
                    .append("\n\t],\n");
        }
        builder.append("}");
        return builder.toString();
    }

    public void insertValue(ParserRuleContext context, String id, Object type) {
        this.getTypeTableFromContextOrDefault(context).insertValue(id, type);
    }
}