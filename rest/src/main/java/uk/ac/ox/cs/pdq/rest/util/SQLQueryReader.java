package uk.ac.ox.cs.pdq.rest.util;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.ConjunctiveQueryBodyBuilder;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLQueryReader {

    /** The schema. */
    private Schema schema;

    /** The alias to tables. */
    private Map<String, String> aliasToTables;

    /**
     * Instantiates a new SQL like query reader.
     *
     * @param schema the schema
     */
    public SQLQueryReader(Schema schema) {
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

        // Initialise variables
        CharStream stream = new ANTLRInputStream(str);
        SQLiteLexer lexer = new SQLiteLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SQLQueryParser parser = new SQLQueryParser(tokens); //wrapper class

        ConjunctiveQuery query;
        try
        {
            parser.parse();

            // Parse the variables from the SELECT ... FROM statement
            ArrayList<Variable> list = new ArrayList<>();
            for(String columnName : parser.getColumnNames()) {
                list.add(new Variable(columnName));
            }

            Term[] terms = new Term[list.size()];
            int i = 0;
            for(Variable var : list) terms[i++] = var;

            // Collect relation names and aliases:
            this.aliasToTables = new HashMap<String, String>();
            for(String raw : parser.getTableAliases()) {
                if(raw.contains("AS"))
                {
                    String[] elements = raw.split("AS");
                    String tableName = elements[0].replaceAll("\\s",""); // Consume whitespace
                    String aliasName = elements[1].replaceAll("\\s",""); // Consume whitespace
                    this.aliasToTables.put(aliasName, tableName);
                }
            }

            // Initialise the Arnold Schwarzenegger Conjunctive Query Body Builder
            ConjunctiveQueryBodyBuilder qBuilder = new ConjunctiveQueryBodyBuilder(this.schema, this.aliasToTables, terms);

            List<String> joinConstraints = new ArrayList<String>();
            for(String s : parser.getJoinConstraints() ) {
                String[] individualConstraints = s.split("AND");

                for( String constraint : individualConstraints ) {
                    joinConstraints.add(constraint);
                }
            }

            // Begin joining on join constraints
            for( String constraint : joinConstraints ) {
                String[] elements = constraint.split("=");
                String left = elements[0];
                String right = elements[1];

                String leftAlias = left.split("\\.")[0].replaceAll("\\s", "");        // Consume whitespace
                String leftColumnName = left.split("\\.")[1].replaceAll("\\s", "");   // Consume whitespace
                String rightAlias = right.split("\\.")[0].replaceAll("\\s", "");      // Consume whitespace
                String rightColumnName = right.split("\\.")[1].replaceAll("\\s", ""); // Consume whitespace

                ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm leftAliasAttr =
                        new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(leftAlias, leftColumnName);
                ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm rightAliasAttr =
                        new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(rightAlias, rightColumnName);

                qBuilder.addConstraint(leftAliasAttr, rightAliasAttr);
            }


            // Begin adding where constraints:
            String rawExpression = parser.getLastExpr();
            if ((rawExpression != null) && (!rawExpression.equals(""))) {
                String[] rawConstraints = rawExpression.split("AND");

                for( String rawConstraint : rawConstraints ) {
                    if(rawConstraint.split("=").length >= 2)
                    {
                        String left = rawConstraint.split("=")[0].replaceAll("\\s", "");  // Consume whitespace
                        String right = rawConstraint.split("=")[1].replaceAll("\\s", ""); // Consume whitespace

                        ConjunctiveQueryBodyBuilder.ConstraintTerm leftConstraint = null;
                        ConjunctiveQueryBodyBuilder.ConstraintTerm rightConstraint = null;

                        // Process left:
                        if(left.contains("'")) {
                            String constant = left.replaceAll("'", "");

                            leftConstraint = new ConjunctiveQueryBodyBuilder.ConstantConstraintTerm(constant);
                        } else if( left.contains(".") ) {
                            String aliasName = left.split("\\.")[0];
                            String attrName = left.split("\\.")[1];

                            leftConstraint = new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(aliasName, attrName);
                        }
                        else
                        {
                            leftConstraint = new ConjunctiveQueryBodyBuilder.ConstantConstraintTerm(left);
                        }

                        // Process right:
                        if(right.contains("'")) {
                            String constant = right.replaceAll("'", "");

                            rightConstraint = new ConjunctiveQueryBodyBuilder.ConstantConstraintTerm(constant);
                        } else if( right.contains(".") ) {
                            String aliasName = right.split("\\.")[0];
                            String attrName = right.split("\\.")[1];

                            rightConstraint = new ConjunctiveQueryBodyBuilder.AliasAttrConstraintTerm(aliasName, attrName);
                        }
                        else
                        {
                            rightConstraint = new ConjunctiveQueryBodyBuilder.ConstantConstraintTerm(right);
                        }
                        qBuilder.addConstraint(leftConstraint, rightConstraint);
                    }
                }
            }

            // Build the query head:
            for (String rawResultColumn : parser.getResultColumns() ) {

                if( rawResultColumn.equals("*") ) {
                    qBuilder.returnAllVars();
                } else {
                    if(rawResultColumn.contains("."))
                    {
                        String aliasName = rawResultColumn.split("\\.")[0];
                        String attrName = rawResultColumn.split("\\.")[1];

                        qBuilder.addResultColumn(aliasName, attrName);
                    }
                }
            }

            // Call toConjunctiveQuery() as the last step
            query = qBuilder.toConjunctiveQuery();

        }
        catch(Exception e)
        {
            throw e;
        }

        return query;
    }
}
