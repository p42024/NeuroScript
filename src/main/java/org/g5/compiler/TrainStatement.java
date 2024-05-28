package org.g5.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.g5.typecheck.ScopedTypeTable;

import java.util.HashSet;
import java.util.stream.Collectors;

public class TrainStatement extends Statement {
    private HashSet<String> variables;
    private HashSet<String> models;
    protected Boolean importBool = false;

    private String modelFormat =
            "class %s(nn.Module):\n" +
                    "\tdef __init__(self):\n" +
                    "\t\tsuper().__init__()\n" +
                    "\t\tself.flatten = nn.Flatten()\n" +
                    "\t\tself.stack = %s\n" +
                    "\n" +
                    "\tdef forward(self, x):\n" +
                    "\t\tif len(x.size()) > 1:\n" +
                    "\t\t\tx = self.flatten(x)\n" +
                    "\t\tlogits = self.stack(x)\n" +
                    "\t\treturn logits";

    public TrainStatement(ParserRuleContext context, int id, ScopedTypeTable scopedTypeTable) {
        super(context);
        TrainStatementBuilder trainStatementBuilder = new TrainStatementBuilder(scopedTypeTable);
        String code = trainStatementBuilder.build(context);
        this.importBool = trainStatementBuilder.importBool;

        this.createVariables(trainStatementBuilder.getIdsOutOfScope());
        this.createModels(trainStatementBuilder.getIdsOutOfScope());
        this.createLine(code, id, trainStatementBuilder.getIdsOutOfScope());
    }

    private void createLine(String code, int id, HashSet<String> idsOutOfScope) {
        String args = this.variables.stream().map(s -> s + "().to(\"cpu\")").collect(Collectors.joining(", "));
        String variablesString = String.join(", ", idsOutOfScope);
        String modelsString = String.join("\n\n\n", this.models);
        this.line = "\ndef train_" + id + "(" + variablesString + "):\n"
                + code + "\n"
                + modelsString + "\n"
                + "train_" + id + "(" + args + ")";
    }

    private void createVariables(HashSet<String> idsOutOfScope) {
        this.variables = new HashSet<>();
        int i = 0;
        for (String variable : idsOutOfScope) {
            this.variables.add(variable + "_" + i++);
        }
    }

    private void createModels(HashSet<String> idsOutOfScope) {
        this.models = new HashSet<>();
        Object[] variablesArray = this.variables.toArray();
        Object[] idsOutOfScopeArray = idsOutOfScope.toArray();
        for (int i = 0; i < idsOutOfScope.size(); i++) {
            String modelDefine = String.format(modelFormat, variablesArray[i], idsOutOfScopeArray[i]);
            models.add(modelDefine);
        }
    }
}
