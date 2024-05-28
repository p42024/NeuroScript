package org.g5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.g5.compiler.StatementBuilder;
import org.g5.parser.GrammarLexer;
import org.g5.parser.GrammarParser;
import org.g5.semantics.Semantics;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.TypeChecker;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar NeuroScript.jar <filename>");
            return;
        }
        try {
            parseAndCompileFile(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
        }
    }

    public static void parseAndCompileFile(String filename) throws Exception {
        parseAndCompileFile(filename, false);
    }

    public static void parseAndCompileFile(String filename, boolean debug) throws Exception {
        CharStream charStream = CharStreams.fromFileName(filename);

        String generatedCode = parseAndCompileString(charStream);

        // Get original file name without extension
        final String regex = "([ \\w-]+?(?=\\.))|([ \\w-]+$)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(filename);

        String outputName = "output";
        if (matcher.find()) outputName = matcher.group(0);
        writeStringToFile(generatedCode, outputName + ".py");
    }

    public static String parseAndCompileString(String code) {
        CharStream charStream = CharStreams.fromString(code);
        return parseAndCompileString(charStream);
    }

    private static String parseAndCompileString(CharStream charStream) {
        // Parse the input code
        GrammarLexer lexer = new GrammarLexer(charStream);
        GrammarParser parser = new GrammarParser(new CommonTokenStream(lexer));
        ParseTree topTree = parser.program();

        // Type check the parsed code
        TypeChecker typeChecker = new TypeChecker(topTree);
        typeChecker.typeCheck();
        ScopedTypeTable scopedTypeTable = typeChecker.getScopedTypeTable();
        //if (debug) System.out.println("Type checker result: " + expressionTable.toString());

        // Check semantics

        Semantics semantics = new Semantics();
        semantics.check(topTree, scopedTypeTable);

        // Generate code
        StatementBuilder statementBuilder = new StatementBuilder(scopedTypeTable);
        String generatedCode = "import torch" + "\n"
                + "import pandas as pd" + "\n"
                + "from torch import nn" + "\n"
                + "from torch.utils.data import DataLoader, Dataset" + "\n"
                + "from torchvision import datasets" + "\n"
                + "from torchvision.transforms import ToTensor" + "\n"
                + "import os" + "\n\n"
                + statementBuilder.build(topTree);
        return generatedCode;
    }

    public static void writeStringToFile(String string, String filename) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(string);
            myWriter.close();
            System.out.printf("Successfully wrote to %s\n", filename);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
