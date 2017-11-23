package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * @author Gabor
 *
 */
public class Command implements DerbyStatement, MySqlStatement, PostgresStatement {
	public static final String DATABASENAME = "{DATABASENAME}";
	protected Map<String,String> replaceTagsMySql = new HashMap<>();
	protected Map<String,String> replaceTagsDerby = new HashMap<>();
	protected Map<String,String> replaceTagsPostgres = new HashMap<>();
	protected List<String> statements = null;
	
	public Command() {
		this.statements = new ArrayList<>(); 
	}
	
	public Command(String command) {
		this.statements = new ArrayList<>(); 
		this.statements.add(command);
	}

	@Override
	public String toString() {
		return "" + statements;
	}
	
	@Override
	public List<String> toPostgresStatement(String databaseName) {
		List<String> newStatements = replaceTags(statements,DATABASENAME,databaseName);
		for (String key:replaceTagsPostgres.keySet()) {
			newStatements = replaceTags(newStatements,key,replaceTagsPostgres.get(key));
		}
		return newStatements;
	}

	@Override
	public List<String> toMySqlStatement(String databaseName) {
		List<String> newStatements = replaceTags(statements,DATABASENAME,databaseName);
		for (String key:replaceTagsMySql.keySet()) {
			newStatements = replaceTags(newStatements,key,replaceTagsMySql.get(key));
		}
		return newStatements;
	}

	@Override
	public List<String> toDerbyStatement(String databaseName) {
		List<String> newStatements = replaceTags(statements,DATABASENAME,databaseName);
		for (String key:replaceTagsDerby.keySet()) {
			newStatements = replaceTags(newStatements,key,replaceTagsDerby.get(key));
		}
		return newStatements;
	}

	protected List<String> replaceTags(List<String> commands, String tagName, String newValue) {
		List<String> results = new ArrayList<>();
		for (String command:commands) {
			results.add(command.replace(tagName, newValue));
		}
		return results;
	}
	
	protected static String convertTermToSQLString(Attribute a, Term term) {
		String termInSqlString = ""; 
		if (!term.isVariable()) {
			
			if (a.getType() == String.class && term instanceof TypedConstant && !"DatabaseInstanceID".equals(a.getName()) && !"FactId".equals(a.getName()))
				termInSqlString += "'" +  ((TypedConstant)term).serializeToString() + "'";
			else if (String.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += "'" +  term + "'";
			else if (Integer.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Double.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Float.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
		} else
				throw new RuntimeException("Unsupported type");
		return termInSqlString;
	}

}
