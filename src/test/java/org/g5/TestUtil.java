package org.g5;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.g5.parser.GrammarLexer;
import org.g5.parser.GrammarParser;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.TypeChecker;
import org.g5.typecheck.TypeTable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestUtil {
    public static void printArr(String[] s1, PrintStream stream) {
        boolean first = true;
        for (String string : s1) {
            if (first) {
                stream.print(string);
                first = false;
            } else stream.print(", " + string);
        }
    }

    public static boolean compareStrings(List<String> s1, List<String> s2) {
        for (int i = 0; i < s1.size(); i++) {
            if (s1.get(i).equals(s2.get(i))) continue;
            else return false;
        }
        return true;
    }

    public static void printArr(List<String> s1, PrintStream stream) {
        boolean first = true;
        for (String string : s1) {
            if (first) {
                stream.print(string);
                first = false;
            } else stream.print(", " + string);
        }
    }

    public static boolean compareStrings(String[] s1, String[] s2) {
        for (int i = 0; i < s1.length; i++) {
            if (s1[i].equals(s2[i])) continue;
            else return false;
        }
        return true;
    }

    public static void traverseAndCheck(ParseTree tree, ScopedTypeTable scopedTypeTable, Class<?> type) {
        traverseAndCheck(tree, scopedTypeTable, new String[]{"a", "b"}, type);
    }

    public static void traverseAndCheckAllScopes(ParseTree tree, ScopedTypeTable scopedTypeTable, String[] variables, Class<?> type) {
        for (TypeTable scope : scopedTypeTable.getScopes()) {
            for (String id : variables) {
                if (scope.hasId(id)) {
                    Object evaluatedExpression = scope.getValue(id);
                    System.out.printf("Testing id: %s for type: %s, expression: %s\n", id, type.getSimpleName(), evaluatedExpression);
                    assertEquals(type, evaluatedExpression.getClass());
                    variables = Arrays.stream(variables).filter(s -> !s.equals(id)).toArray(String[]::new);
                }
            }
        }
        if (variables.length > 0) {
            assertArrayEquals(new String[]{}, variables);
        }
    }

    public static void traverseAndCheck(ParseTree tree, ScopedTypeTable scopedTypeTable, String[] variables, Class<?> type) {
        for (String id : variables) {
            Object evaluatedExpression = scopedTypeTable.get(tree).getValue(id);
            System.out.printf("Testing id: %s for type: %s, expression: %s\n", id, type.getSimpleName(), evaluatedExpression);
            assertEquals(type, evaluatedExpression.getClass());
        }
    }

    public interface TestFunction<T> {
        boolean test(ScopedTypeTable scopedTypeTable, Object type, T evaluatedExpression);
    }

    public static <T> void traverseAndCheck(ParseTree tree, ScopedTypeTable scopedTypeTable, Class<T> type, TestFunction<T> testFunction) {
        traverseAndCheck(tree, scopedTypeTable, new String[]{"a", "b"}, type, testFunction);
    }

    public static <T> void traverseAndCheck(ParseTree tree, ScopedTypeTable scopedTypeTable, String[] variables, Class<T> type, TestFunction<T> testFunction) {
        for (String id : variables) {
            Object evaluatedExpression = scopedTypeTable.get(tree).getValue(id);
            System.out.printf("Testing id: %s for type: %s, expression: %s\n", id, type.getSimpleName(), evaluatedExpression);
            assertEquals(type, evaluatedExpression.getClass());
            T expression = type.cast(evaluatedExpression);
            assertTrue(testFunction.test(scopedTypeTable, type, expression));
        }
    }

    public static ParseTree getParseTreeFromString(String code) throws IOException {
        CharStream charStream = CharStreams.fromString(code);
        // Parse the input code
        GrammarLexer lexer = new GrammarLexer(charStream);
        GrammarParser parser = new GrammarParser(new CommonTokenStream(lexer));
        return parser.program();
    }

    public static ScopedTypeTable getTypeTable(ParseTree tree) {
        // Type check the parsed code
        TypeChecker typeChecker = new TypeChecker(tree);
        typeChecker.typeCheck();
        return typeChecker.getScopedTypeTable();
    }

    public static ParseTree getParseTree(String s) {
        return null;
    }
}
