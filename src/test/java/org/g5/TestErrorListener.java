package org.g5;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.BitSet;

public class TestErrorListener implements ANTLRErrorListener {
    private boolean hasFailed = false;

    public boolean HasFailed() {
		return hasFailed;
	}

	@Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        hasFailed = true;
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
            BitSet ambigAlts, ATNConfigSet configs) {
        hasFailed = true;
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
            BitSet conflictingAlts, ATNConfigSet configs) {
        hasFailed = true;
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
            ATNConfigSet configs) {
        hasFailed = true;
    }
}
