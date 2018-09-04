package uk.ac.ox.cs.pdq.ui.io.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
//import uk.ac.ox.cs.pdq.db.builder.ConjunctiveQueryBodyBuilder;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteLexer;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser;

// TODO: Auto-generated Javadoc
/**
 * The Class SQLLikeQueryReader.
 */
public class SQLLikeQueryReader {
	
	/** The log. */
	private static Logger log = Logger.getLogger(SQLLikeQueryReader.class);
	
	/** The schema. */
	private Schema schema;
	
	/** The alias to tables. */
	private Map<String, String> aliasToTables;
	
	/**
	 * Instantiates a new SQL like query reader.
	 *
	 * @param schema the schema
	 */
	public SQLLikeQueryReader(Schema schema) {
		this.schema = schema;
	}
	
	/**
	 * Gets the alias to tables.
	 *
	 * @return the alias to tables
	 */
	public Map<String, String> getAliasToTables() {
		return this.aliasToTables;
	}
	
	/**
	 * From string.
	 *
	 * @param str the str
	 * @return the conjunctive query
	 * @throws Exception the exception
	 */
	public ConjunctiveQuery fromString(String str) throws Exception {
		
		CharStream stream = new ANTLRInputStream(str);
        SQLiteLexer lexer = new SQLiteLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLiteParser parser = new SQLiteParser(tokens);
        
        SQLiteSimpleListener listener = new SQLiteSimpleListener();
        parser.addParseListener(listener);
        
        parser.parse();
        
        // Collect relation names and aliases:
        this.aliasToTables = new HashMap<String, String>();
        
        for(ParseTree p : listener.getTableAliases()) {
        	String raw = p.getText();
        	String[] elements = raw.split("AS");
        	String tableName = elements[0];
        	String aliasName = elements[1];
        	this.aliasToTables.put(aliasName, tableName);
        }
        
        log.debug("query aliases: " + this.aliasToTables);
        
 // MR       ConjunctiveQueryBodyBuilder qBuilder = new ConjunctiveQueryBodyBuilder(this.schema, this.aliasToTables);
        
        List<String> joinConstraints = new ArrayList<String>();
        for( ParseTree p : listener.getJoinConstraints() ) {
        	String[] individualConstraints = p.getText().split("AND");
        	
        	for( String constraint : individualConstraints ) {
        		joinConstraints.add(constraint);
        	}
        }
        
    	for( String constraint : joinConstraints ) {
        	String[] elements = constraint.split("=");
        	String left = elements[0];
        	String right = elements[1];
        	
        	String leftAlias = left.split("\\.")[0];
        	String leftColumnName = left.split("\\.")[1];
        	String rightAlias = right.split("\\.")[0];
        	String rightColumnName = right.split("\\.")[1];
        	
/* MR        	ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm leftAliasAttr =
        			new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(leftAlias, leftColumnName);
        	ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm rightAliasAttr =
        			new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(rightAlias, rightColumnName);
        	
        	qBuilder.addConstraint(leftAliasAttr, rightAliasAttr); */
        } // end joining on join constraints
    	
    	
    	// Begin adding where constraints:
    	
		ParseTree ctx = listener.getLastExpr();
		if (ctx != null) {
        	String rawExpression = ctx.getText();
        	log.debug("raw where expr: " + rawExpression);
        	String[] rawConstraints = rawExpression.split("AND");
        	
        	for( String rawConstraint : rawConstraints ) {
        		String left = rawConstraint.split("=")[0];
        		String right = rawConstraint.split("=")[1];
        		
/* MR        		ConjunctiveQueryBodyBuilder.ConstraintTerm leftConstraint = null;
        		ConjunctiveQueryBodyBuilder.ConstraintTerm rightConstraint = null;
        		
        		// process left:
        		if(left.contains("'")) {
         			String constant = left.replaceAll("'", "");
         			
         			leftConstraint = new ConjunctiveQueryBodyBuilder.ConstantConstraintTerm(constant);
         		} else if( left.contains(".") ) {
        			String aliasName = left.split("\\.")[0];
        			String attrName = left.split("\\.")[1];
        			
        			leftConstraint = new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(aliasName, attrName);
        		}
        		
        		// process right:
         		if(right.contains("'")) {
         			String constant = right.replaceAll("'", "");
         			
         			rightConstraint = new ConjunctiveQueryBodyBuilder.ConstantConstraintTerm(constant);
         		} else if( right.contains(".") ) {
        			String aliasName = right.split("\\.")[0];
        			String attrName = right.split("\\.")[1];
        			
        			rightConstraint = new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(aliasName, attrName);
        		}
        		
        		qBuilder.addConstraint(leftConstraint, rightConstraint); */
        	}
		}
    	
    	// Build the query head:
    	
    	for (ParseTree ctxt : listener.getResultColumns() ) {
    		String rawResultColumn = ctxt.getText();
    		
    		if( rawResultColumn.equals("*") ) {
// MR  			qBuilder.returnAllVars();
    		} else {
    			String aliasName = rawResultColumn.split("\\.")[0];
    			String attrName = rawResultColumn.split("\\.")[1];
    			
// MR   			qBuilder.addResultColumn(aliasName, attrName);
    		}
    	}
    	
// MR    	log.debug("qBuilder: " + qBuilder);
    	
/* MR    	ConjunctiveQuery query = qBuilder.toConjunctiveQuery();
    	log.debug("query: " + query);
    	
        return query; */
    	return null;
	}

}
