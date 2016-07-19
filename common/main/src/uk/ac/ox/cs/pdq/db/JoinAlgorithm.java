package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

public class JoinAlgorithm {
	
	/**
	 * Joins two or more tables according to the equalities stored in joins. 
	 * Current implementation uses a hash join across a single attribute pair for each join, then applies additional equality constraints.
	 * @param tablesMapping: a String-MainMemoryRelation mapping with all of the relevant tables, which should already have been appropriately aliased for the search.
	 * @param joins: an AttributeEqualities object (essentially a glorified multimap) representing all join equalities to be applied
	 * @return the resulting joined table.
	 * If the table is a join of tables A0 and A1, with attributes x0 and x1 each, 
	 * 		then the attributes for the resulting table will be A0.x0, A0.x1, A1.x0, and A1.x1
	 */
	public MainMemoryRelation join( Map<String, MainMemoryRelation> tablesMapping, AttributeEqualities joins){
		MainMemoryRelation hashTable = null;
		List<String> theTables = Lists.newArrayList(tablesMapping.keySet());
		// The hashtable is initially formed from the first table on the list of relevant tables
		// We locate a relevant equality for this first table and the other table in the equality becomes our second table
		String leftTable = theTables.get(0);
		for( Pair<String, String> left : Lists.newArrayList(joins.getLeftEqualities())){
			if( left.getLeft().equals(leftTable) ){
				Pair<String, String> right = joins.get(left).get(0);
				int index1 = tablesMapping.get(left.getLeft()).getAttributeIndex(left.getRight());
				int index2 = tablesMapping.get(right.getLeft()).getAttributeIndex(right.getRight());
				hashTable = hashJoin( tablesMapping.get(left.getLeft()), index1, tablesMapping.get(right.getLeft()), index2);
				joins.dropEquality(Pair.of(left, right));
				break;
			}
		}
		// The subsequent hash joins occur between the existing hash table and a table that exists in an equality but has not yet been incorporated into the table
		for( Pair<String, String> left : Lists.newArrayList(joins.getLeftEqualities())){
			Pair<String, String> right = joins.get(left).get(0);
			if( inAlready(left.getLeft(), hashTable) && !inAlready(right.getLeft(), hashTable )){
				int index1 = tablesMapping.get(right.getLeft()).getAttributeIndex(right.getRight());
				int index2 = whichIndex( left, hashTable );
				hashTable = hashJoin( tablesMapping.get(right.getLeft()), index1, hashTable, index2);
				joins.dropEquality(Pair.of(left, right));
			}
		}

		//The joined table has been computed by joining single attributes across tables. 
		//We need to iterate over the resulting tuples and enforce the rest of the joins between additional attributes.
		hashTable = applyRestOfJoinedAttributes(hashTable, joins);
		return hashTable;
	}
	public Map<String, MainMemoryRelation> renameWithAliases( Map<String, String> fromAliases, MainMemoryDatabase database ){
		// Gets a mapping aliasName:originalName
				// Creates a new mapping of name:table where name is the alias name and table is the data in the original table, with the table and attributes appropriately renamed
				Map<String, MainMemoryRelation> searchDatabase = new HashMap<String, MainMemoryRelation>();
				for( String newName: Lists.newArrayList(fromAliases.keySet())){
					// Renames the attributes as x0, x1, etc., excluding the Fact attribute (if any)
					List<Attribute> newAttr = rename( database.getTable(fromAliases.get(newName)).getAttributes() );
					Relation newRelate = new DatabaseRelation( newName, newAttr );
					// Instantiates the new table & copies the data in the old one over
					MainMemoryRelation newTable = new MainMemoryRelation(newRelate);
					for( Atom tuple: database.getTable(fromAliases.get(newName)).getTuples() ){
						newTable.addTuple(tuple);
					}
					// Adds the new table to the database
					searchDatabase.put(newName, newTable);
					
				}
				return searchDatabase;
	}
	public MainMemoryRelation renameRelationwithAliases( Map<String, MainMemoryRelation> tableMap ){
		String alias = Lists.newArrayList(tableMap.keySet()).get(0);
		MainMemoryRelation oldTable = Lists.newArrayList(tableMap.values()).get(0);
		List<Attribute> newAttributes = rename(oldTable.getAttributes());
		
		// First create a list of attributes with the names you want
		for( int i = 0; i < newAttributes.size(); i++) {//Attribute oldAttr: oldTable.getAttributes()){
			Attribute newAttr = new Attribute(newAttributes.get(i).getType(), alias+"."+newAttributes.get(i).getName());
			newAttributes.set(i, newAttr);
		}
		// Create the new table
		MainMemoryRelation newTable = new MainMemoryRelation( oldTable.getName(), newAttributes);
		// Populate the new table with the tuples from the old one
		for( Atom tuple : oldTable.getTuples()){
			newTable.addTuple(tuple);
		}
		return newTable;
		
	}
	public MainMemoryRelation applyInequalities( MainMemoryRelation searchTable, Map<String, String> inequalities){
		// Takes a table and a map Att1:Att2. Returns a table where every row meets the criteria Att1 < Att2.
		for( String minorAttr : inequalities.keySet()){
			int index1 = searchTable.getAttributeIndex(minorAttr);
			int index2 = searchTable.getAttributeIndex(inequalities.get(minorAttr));
			for( Atom row: Lists.newArrayList( searchTable.getTuples() )){
				if( row.getTerm(index1).isSkolem() && row.getTerm(index2).isSkolem() ){
					// This is an ugly way to do this but there seems to be no way to return a term's actual value directly, or to check whether it's a TypedConstant
					// So instead I am abusing toString(). 
					if( Integer.parseInt(row.getTerm(index1).toString()) <= Integer.parseInt(row.getTerm(index2).toString())){
						searchTable.dropTuple(row);
					}
				} else {
					throw new UnsupportedOperationException("The value of terms you are trying to compare is not numerical");
				}
			}
		}
		
		return searchTable;
	}
	public Set<Map<Variable, Constant>> finalSelect( LinkedHashMap<String, Variable> selectAttrib, MainMemoryRelation searchTable ){
		// Gets a table containing tuples that we will return in the final result
		// Also gets a string-variable map that represents which attributes map to which variables
		// For each row, we use the attribute name to pick the constant which is in the correct position and map it to the specified variable
		// Maps are then compiled into a set to delete duplicates and returned
		Set<Map<Variable, Constant>> results = new HashSet<Map<Variable,Constant>>();
		for( Atom row: searchTable.getTuples() ){
			Map<Variable, Constant> mapping = new HashMap<Variable, Constant>();
			for( String attrib : selectAttrib.keySet()){	
				mapping.put( selectAttrib.get(attrib), (Constant) row.getTerm( searchTable.getAttributeIndex(attrib)));
			}
			results.add( mapping );
		}
		
		return results;
	}
	
	private MainMemoryRelation hashJoin( MainMemoryRelation table1, int index1, MainMemoryRelation table2, int index2 ){
		Map< Term, List<Atom> > indexOnFirstJoinAttribute = new HashMap<>();
		//When two atoms join they create one "larger" atom
		List< Pair<Atom, Atom> > tuplePairs = new ArrayList<  Pair<Atom, Atom> >();
		
		//Create an index of all atoms of table one, indexing on the join attribute
		for( Atom tuple: table1.getTuples() ){
			List<Atom> v = (indexOnFirstJoinAttribute.get(tuple.getTerm(index1)) != null)?indexOnFirstJoinAttribute.get(tuple.getTerm(index1)): new ArrayList<Atom>();
			v.add(tuple);
			indexOnFirstJoinAttribute.put( tuple.getTerm(index1), v);
		}

		//Using the index on the first table in order to join
		for( Atom tuple : table2.getTuples() ){
			List<Atom> lst = indexOnFirstJoinAttribute.get( tuple.getTerm(index2));
			if( lst != null ){
				for( Atom tuple2 : lst ){
					 Pair<Atom, Atom> tatom = Pair.of(tuple, tuple2);
					tuplePairs.add(tatom);
				}
			}
		}
		
		//Renaming attributes so the tuple which is the join result does not contain duplicate attribute names
		List<Attribute> attributes = new ArrayList<Attribute>();
		//If on one side I'm joining x0 attribute from A0 table, there will be a new column in my output table called A0.x0
		for( Attribute attr : table1.getAttributes()){
			Attribute newAttr = new Attribute( attr.getType(), table1.getName() + "." + attr );
			attributes.add( newAttr );
		}
		//If one side of the join is the hashTable I'll just keep all its columns with their names as they are
		if ( table2.getName() != "hashTable"){
			int pos = 0;
			for( Attribute attr : table2.getAttributes()){
				Attribute newAttr = new Attribute( attr.getType(), table2.getName() + "." + attr );
				attributes.add(pos, newAttr );
				pos++;
			}
		} else {
			attributes.addAll(0, table2.getAttributes());
		}
		
		//Output of the join
		MainMemoryRelation hashTable = new MainMemoryRelation( "hashTable", attributes );
		
		//Adds the joined tuples to the output table
		for( Pair<Atom,Atom> entries : tuplePairs ){
			List<Term> newAtom = new ArrayList<Term>(entries.getLeft().getTerms());
			newAtom.addAll(entries.getRight().getTerms());
			Predicate pred = new Predicate("h", newAtom.size());
			hashTable.addTuple(new Atom(pred, newAtom));
		}
		
		return hashTable;
	}
	private boolean inAlready( String name, MainMemoryRelation table ){
		// Based on attribute names of the table, checks whether the table of name "name" has been hashed into this table yet
		if( table == null ){
			return false;
		}
		for( Attribute attrName : table.getAttributes() ){
			if( attrName.getName().contains(name) ){
				return true;
			}
		}
		
		return false;
	}
	private int whichIndex( Pair<String, String> where, MainMemoryRelation table ){
		// where consists of <tableName, attributeName>. This returns the index of that original table, attribute pair in a joined result
		return table.getAttributeIndex(where.getLeft()+"."+where.getRight());
}
	private MainMemoryRelation applyRestOfJoinedAttributes( MainMemoryRelation searchTable, AttributeEqualities equalAttributes ){
 		// Gets a table and an object that contains the equalities that should be present in the final selection
 		// Returns the table after dropping those tuples that do not satisfy the equalities
 		while(!equalAttributes.isEmpty()){
 			Pair<String, String> index = equalAttributes.getLeftEqualities().get(0);
			int index1 = searchTable.getAttributeIndex(index.getLeft()+"."+index.getRight());
			int index2 = searchTable.getAttributeIndex( equalAttributes.get(index).get(0).getLeft()+"." + equalAttributes.get(index).get(0).getRight());
			equalAttributes.dropEquality(Pair.of(index, equalAttributes.get(index).get(0)));
			for( Atom row : Lists.newArrayList(searchTable.getTuples() )){
				if( !row.getTerm(index1).equals(row.getTerm(index2))){
					searchTable.dropTuple(row);
				}
			}
		}
		
		return searchTable;
	}
	private List<Attribute> rename( List<Attribute> current ){
		// Renames attributes with a unified naming scheme (x0, x1, x2, etc.)
		List<Attribute> future = new ArrayList<Attribute>();
		String prefix = "x";
		int num = 0;
		for( Attribute now : current ){
			// Excludes the Fact attribute, if any
			if(now.getName() != "Fact"){
				future.add(new Attribute(now.getType(), prefix+num ));
				num++;
			} else {
				future.add( now );
			}
		}
		return future;
	}
}


