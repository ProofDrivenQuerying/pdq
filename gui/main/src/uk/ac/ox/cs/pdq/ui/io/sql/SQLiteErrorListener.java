package uk.ac.ox.cs.pdq.ui.io.sql;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.log4j.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.antlr.v4.runtime.Parser;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser;
import org.antlr.v4.runtime.ANTLRErrorListener;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving SQLiteSimple events.
 * The class that is interested in processing a SQLiteSimple
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSQLiteSimpleListener<code> method. When
 * the SQLiteSimple event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SQLiteSimpleEvent
 */
public class SQLiteErrorListener implements ANTLRErrorListener {
	
	/** The log. */
	private static Logger log = Logger.getLogger(SQLiteErrorListener.class);
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		// TODO Auto-generated method stub
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Information Dialog");
    	alert.setHeaderText(null);
    	alert.setContentText("Line: " + line + ":" + charPositionInLine + " " + msg);
    	alert.showAndWait();
    	throw e;
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
			BitSet ambigAlts, ATNConfigSet configs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
			BitSet conflictingAlts, ATNConfigSet configs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
			ATNConfigSet configs) {
		// TODO Auto-generated method stub
		
	}
	
}
