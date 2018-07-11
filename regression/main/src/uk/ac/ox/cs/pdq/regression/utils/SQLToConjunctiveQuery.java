package uk.ac.ox.cs.pdq.regression.utils;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Converts an SQL query string given as an input text file to a Conjunctive query object. 
 *
 */
public class SQLToConjunctiveQuery {
 
	private static String READ_COLUMN_ALIAS = "((\\w+).(\\w+)(\\s+)AS(\\s+)(\\w+))";
	private static String READ_COLUMN = "((\\w+).(\\w+)(\\s*))";
	private static String READ_TABLE_ALIAS = "((\\w+)(\\s+)AS(\\s+)(\\w+))";
	private static String READ_JOIN_PREDICATE = "((\\w+).(\\w+)(\\s*)=(\\s*)(\\w+).(\\w+))";
	private static String READ_EQUALITY_FILTER_PREDICATE = "((\\w+).(\\w+)(\\s*)=(\\s*)(\\'.*\\'))";

	public static ConjunctiveQuery parse(File input, Schema schema) {
		
		Map<String,Relation> aliases = Maps.newHashMap();
		
		Set<Pair<Pair<String, Attribute>, ConstantEqualityCondition>> filterPredicates = new LinkedHashSet<>();
		Set<Pair<Pair<String, String>, AttributeEqualityCondition>> joinPredicates = new LinkedHashSet<>();
		
		Set<Pair<String,Attribute>> columns = new LinkedHashSet<>();		
		
		String line = null;
		try {
			FileReader fileReader = new FileReader(input);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String selectStatement = null;
			try {
				while((line = bufferedReader.readLine()) != null) {
					if(line.startsWith("SELECT DISTINCT")) 
						selectStatement = line.substring("SELECT DISTINCT".length() + 1);
					else if(line.startsWith("SELECT")) 
						selectStatement = line.substring("SELECT".length() + 1);
					
					else if(line.startsWith("FROM")) {
						String fromStatement = line.substring("FROM".length() + 1);
						String[] expressions = fromStatement.split(",");
						for(String expression:expressions) {
							Pair<String,Relation> aliasTable = parseTable(expression,schema);
							if(aliases.containsKey(aliasTable.getLeft())) 
								throw new java.lang.IllegalStateException("The " + aliasTable.getLeft() + " has been already used for another table");
							else 
								aliases.put(aliasTable.getLeft(), aliasTable.getRight());
						}
					}
					else {
						String whereStatement = null;
						if(line.startsWith("WHERE")) 
							whereStatement = line.substring("WHERE".length() + 1);
						else 
							whereStatement = line;
						
						if(line.endsWith(";")) 
							whereStatement = whereStatement.substring(0, whereStatement.length() - 1);
						
						if(StringUtils.countMatches(whereStatement, "AND") > 1) 
							throw new java.lang.IllegalStateException("Cannot parse the input where statement. Only one AND per row is allowed");
						
						if(!whereStatement.isEmpty()) {
							String[] predicates = whereStatement.split("AND");
							Pair<Pair<String, String>, AttributeEqualityCondition> joinPredicate = parseJoinPredicate(predicates[0], schema, aliases);
							if(joinPredicate == null && predicates.length > 1) {
								joinPredicate = parseJoinPredicate(predicates[1], schema, aliases);
							}
							if(joinPredicate != null) 
								joinPredicates.add(joinPredicate);
							else {
								Pair<Pair<String, Attribute>, ConstantEqualityCondition> filterPredicate = parseFilterPredicate(predicates[0], aliases);
								if(filterPredicate == null && predicates.length > 1) {
									filterPredicate = parseFilterPredicate(predicates[1], aliases);
									filterPredicates.add(filterPredicate);
								}
								Preconditions.checkNotNull(filterPredicate, "Error parsing the line " + line);
							}
						}
					}
				}
			} finally {
				bufferedReader.close();
			}

			String[] expressions = selectStatement.split(",");
			for(String expression:expressions) {
				Pair<String, Attribute> pair = parseColumn(expression, aliases);
				if(pair == null) 
					throw new java.lang.IllegalStateException("The expression " + expression + " does not parse");
				else 
					columns.add(pair);
			}

			Map<String,Term[]> atomToTerms = Maps.newLinkedHashMap();
			Atom[] bodyAtoms = computeQueryAtoms(schema, aliases, filterPredicates, joinPredicates, atomToTerms);
			Variable[] freeVariables = computeFreeVariables(aliases, columns, atomToTerms);
			
			return ConjunctiveQuery.create(freeVariables, bodyAtoms);		
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return null;
	}

	protected static Pair<String,Attribute> parseColumn(String line, Map<String,Relation> aliases) {
		Pattern p = Pattern.compile(READ_COLUMN_ALIAS);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String alias = m.group(2);
			String column = m.group(3);
			if(aliases.get(alias) == null) {
				throw new java.lang.IllegalStateException("Table " + alias + " does not exist in the input schema");
			}
			else {
				Relation table = aliases.get(alias);
				return Pair.of(alias, table.getAttribute(column));
			}
		}
		else {
			p = Pattern.compile(READ_COLUMN);
			m = p.matcher(line);
			if (m.find()) {
				String alias = m.group(2);
				String column = m.group(3);
				if(aliases.get(alias) == null) {
					throw new java.lang.IllegalStateException("Table " + alias + " does not exist in the input schema");
				}
				else {
					Relation table = aliases.get(alias);
					return Pair.of(alias, table.getAttribute(column));
				}
			}
		}
		return null;
	}

	protected static Pair<String,Relation> parseTable(String line, Schema schema) {
		Pattern p = Pattern.compile(READ_TABLE_ALIAS);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String  table = m.group(2);
			String alias = m.group(5);
			if(getTable(schema, table) == null) {
				throw new java.lang.IllegalStateException("Table " + table + " does not exist in the input schema");
			}
			Relation prototype = getTable(schema, table);
			return Pair.of(alias, prototype);
		}
		else {
			String  table = line.trim();
			Relation prototype = getTable(schema, table);
			return Pair.of(prototype.getName(), prototype);
		}
	}

	protected static Relation getTable(Schema schema, String table) {
		Relation prototype = schema.getRelation(table);
		return prototype;
	}

	protected static Pair<Pair<String,String>,AttributeEqualityCondition> parseJoinPredicate(String line, Schema schema, Map<String,Relation> aliases) {
		Pattern p = Pattern.compile(READ_JOIN_PREDICATE);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String alias1 = m.group(2);
			String column1 = m.group(3);
			if(aliases.get(alias1) == null) {
				throw new java.lang.IllegalStateException("Table " + alias1 + " does not exist in the input schema");
			}
			Relation aliasTable1 = aliases.get(alias1);
			Relation sourceTable1 = schema.getRelation(aliasTable1.getName());
			Attribute attribute1 = aliasTable1.getAttribute(column1);
			if(attribute1 == null) {
				throw new java.lang.IllegalStateException("Attribute " + attribute1 + " does not exist in table " + aliasTable1);
			}
			String alias2 = m.group(6);
			String column2 = m.group(7);
			if(aliases.get(alias2) == null) {
				throw new java.lang.IllegalStateException("Table " + alias2 + " does not exist in the input schema");
			}
			Relation aliasTable2 = aliases.get(alias2);
			Relation sourceTable2 = schema.getRelation(aliasTable2.getName());
			Attribute attribute2 = aliasTable2.getAttribute(column2);
			if(attribute2 == null) {
				throw new java.lang.IllegalStateException("Attribute " + attribute2 + " does not exist in table " + aliasTable2);
			}
			AttributeEqualityCondition condition = AttributeEqualityCondition.create(sourceTable1.getAttributePosition(attribute1.getName()), 
					sourceTable2.getAttributePosition(attribute2.getName()));
			
			return Pair.of(Pair.of(alias1,alias2), condition);
		}
		return null;
	}

	protected static Pair<Pair<String, Attribute>,ConstantEqualityCondition> parseFilterPredicate(String line, Map<String,Relation> aliases) {
		Pattern p = Pattern.compile(READ_EQUALITY_FILTER_PREDICATE);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String  alias1 = m.group(2);
			String column1 = m.group(3);
			if(aliases.get(alias1) == null) {
				throw new java.lang.IllegalStateException("Table " + alias1 + " does not exist in the input schema");
			}
			Relation table1 = aliases.get(alias1);
			Attribute attribute1 = table1.getAttribute(column1);
			if(attribute1 == null) {
				throw new java.lang.IllegalStateException("Attribute " + attribute1 + " does not exist in table " + table1);
			}
			Object value = m.group(6);
			Object value_cast = Utility.cast(attribute1.getType(), value);
			if(attribute1.getType().equals(String.class)) {
				value_cast = ((String)value_cast).replaceAll("'", "");
			}
			
			ConstantEqualityCondition condition = ConstantEqualityCondition.create(table1.getAttributePosition(attribute1.getName()), TypedConstant.create(value_cast));
			
			return Pair.of(Pair.of(alias1,attribute1), condition);
		}
		return null;
	}

	private static Variable[] computeFreeVariables(
			Map<String,Relation> aliases,
			Set<Pair<String,Attribute>> columns, 
			Map<String,Term[]> atomToTerms) {
		//Create the Atom of the query
		List<Variable> freeVariables = new ArrayList<>();
		for(Pair<String, Attribute> column:columns) {
			String alias = column.getLeft();
			Attribute attribute = column.getRight();
			int indexOf = aliases.get(alias).getAttributePosition(attribute.getName());
			Term[] terms = atomToTerms.get(alias);
			freeVariables.add((Variable) terms[indexOf]);
		}
		return freeVariables.toArray(new Variable[freeVariables.size()]);
	}

	private static Atom[] computeQueryAtoms(
			Schema schema, 
			Map<String,Relation> aliases,
			Set<Pair<Pair<String, Attribute>, ConstantEqualityCondition>> filterPredicates,
			Set<Pair<Pair<String, String>, AttributeEqualityCondition>> joinPredicates,
			Map<String,Term[]> atomToTerms) {
		
		Map<Term,Term> joinTermsMap = Maps.newHashMap();
		for(Entry<String,Relation> table:aliases.entrySet()) {
			//Generate one predicate per table
			Term[] terms = new Term[table.getValue().getArity()];
			for(int attributeIndex = 0; attributeIndex < table.getValue().getArity(); ++attributeIndex) {
				Attribute attribute = table.getValue().getAttribute(attributeIndex);
				Variable variable = Variable.create(table.getKey() == null ? table.getValue().getName() : table.getKey() + "_" + attribute.getName());
				terms[attributeIndex] = variable;
			}
			atomToTerms.put(table.getKey(), terms);
		}
		
		//Create equijoin predicates
		for(Pair<Pair<String, String>, AttributeEqualityCondition> joinPredicate:joinPredicates) {
			String leftAlias = joinPredicate.getLeft().getLeft();
			String rightAlias = joinPredicate.getLeft().getRight();
			
			int leftIndex = joinPredicate.getRight().getPosition();
			int rightIndex = joinPredicate.getRight().getOther();
			
			Preconditions.checkArgument(leftIndex >= 0 && rightIndex >= 0);
			Term leftTerm = atomToTerms.get(leftAlias)[leftIndex];
			Term rightTerm = atomToTerms.get(rightAlias)[rightIndex];
			if(joinTermsMap.get(leftTerm) != null) 
				joinTermsMap.put(rightTerm, joinTermsMap.get(leftTerm));
			else if(joinTermsMap.get(rightTerm) != null) 
				joinTermsMap.put(leftTerm, joinTermsMap.get(rightTerm));
			else {
				joinTermsMap.put(leftTerm, rightTerm);
				joinTermsMap.put(rightTerm, rightTerm);
			}
		}
		
		//Update the terms of the atoms based on the equijoin conditions
		for(Entry<String, Term[]> entry:atomToTerms.entrySet()) {
			Term[] terms = entry.getValue();
			for(int i = 0; i < terms.length; ++i) {
				if(joinTermsMap.get(terms[i]) != null) 
					terms[i] = joinTermsMap.get(terms[i]);
			}
		}
		
		//Create filter predicates
		for(Pair<Pair<String, Attribute>, ConstantEqualityCondition> filterPredicate:filterPredicates) {
			Attribute left = filterPredicate.getLeft().getRight();
			Object value = filterPredicate.getValue().getConstant();
			String leftTable = filterPredicate.getLeft().getLeft();
			int leftIndex = aliases.get(leftTable).getAttributePosition(left.getName());
			Preconditions.checkArgument(leftIndex >=0);
			atomToTerms.get(leftTable)[leftIndex] = TypedConstant.create(value);
		}
		
		//Create the body of the query
		Atom[] body = new Atom[atomToTerms.entrySet().size()];
		int index = 0;
		for(Entry<String, Term[]> entry:atomToTerms.entrySet()) {
			String alias = entry.getKey();
			Term[] terms = entry.getValue();
			Predicate predicate = Predicate.create(aliases.get(alias).getName(), terms.length);
			if(schema != null) 
				Preconditions.checkArgument(schema.getRelation(aliases.get(alias).getName()) != null, "The corresponding relation cannot be found in the input schema");
			body[index++] = Atom.create(predicate, terms);
		}
		return body;
	}
	
	public static void main(String[] args) {
		String directory = "test/planner/linear/fast/tpch/nhs";
		String schemaFileName = "schema.xml";
		String queryFile = "query.sql";
		
		// Import the schema
		Schema schema;
		try {
			schema = DbIOManager.importSchema(new File(directory, schemaFileName));
			ConjunctiveQuery cq = parse(new File(directory, queryFile), schema);
			System.out.println(cq);
			String sql = ConjunctiveQueryToSQL.translateQueryToSQL(cq, schema, "nhs");
			System.out.println(sql);
		} catch (FileNotFoundException | JAXBException e) {
			e.printStackTrace();
		}
	}

}