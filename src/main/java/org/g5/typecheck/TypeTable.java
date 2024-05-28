package org.g5.typecheck;

import org.g5.exceptions.AssignmentTypeException;
import org.g5.typecheck.types.NumberExpression;

import java.util.HashMap;
import java.util.stream.Collectors;

public class TypeTable {
    HashMap<String, Object> table;
    int startLine;

    {
        startLine = 0;
    }

    public TypeTable() {
        table = new HashMap<>();
    }

    public TypeTable(int startLine) {
        this();
        this.startLine = startLine;
    }

    public TypeTable(TypeTable table, int startLine) {
        this(startLine);
        this.deepCopy(table);
    }

    private void deepCopy(TypeTable table) {
        HashMap<String, Object> tableMap = table.getTable();
        for (String s : tableMap.keySet()) {
            this.table.put(s, tableMap.get(s));
        }
    }

    public HashMap<String, Object> getTable() {
        return table;
    }

    public boolean isType(String id, Object object) {
        Object value = table.get(id);
        if (!object.getClass().isAssignableFrom(value.getClass())) return false;
        if (!(object instanceof NumberExpression)) return true;
        if (!((NumberExpression) object).isNumeric() || !((NumberExpression) value).isNumeric()) return true;
        return ((NumberExpression) object).isInt() == ((NumberExpression) value).isInt();
    }

    public boolean isType(String id, Class<?> object) {
        return object.isAssignableFrom(table.get(id).getClass());
    }

    public Class<?> getType(String id) {
        if (!hasId(id)) return null;
        return this.table.get(id).getClass();
    }

    public void insertValue(String id, Object object) {
        if (hasId(id)) {
            if (isType(id, object)) return;
            throw new AssignmentTypeException(0, id, object, this.getType(id));
        }
        this.table.put(id, object);
    }

    public Object getValue(String id) {
        return this.table.get(id);
    }

    public boolean hasId(String id) {
        return this.table.containsKey(id);
    }

    @Override
    public String toString() {
        int maxKeyLength = table.keySet().stream().mapToInt(String::length).max().orElse(0);
        int maxClassNameLength = table.values().stream().mapToInt(value -> value.getClass().getSimpleName().length()).max().orElse(0);
        return table.keySet().stream()
                .map(key -> String.format("%-" + maxKeyLength + "s (%s)%s : %s",
                        key,
                        table.get(key).getClass().getSimpleName(),
                        " ".repeat((maxClassNameLength - table.get(key).getClass().getSimpleName().length())),
                        table.get(key)))
                .collect(Collectors.joining("\n"));
    }

    public int getStartLine() {
        return startLine;
    }
}
