package uk.ac.ox.cs.pdq.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

public class MainMemoryDatabase {
	
//THIS WHOLE CLASS NEEDS TO CHANGE!!
	protected Map<String, MainMemoryRelation> relations;
		
	public Map<String, MainMemoryRelation> getTables(){
 		// Returns all the tables
		return this.relations;
	}
	public MainMemoryDatabase(){
		// Constructor sets up the database
		// Automatically creates table to store the equalities between constants and labeled nulls / labeled nulls and other labeled nulls
		this.relations = new HashMap<String, MainMemoryRelation>();
		Attribute attribute1 = new Attribute(String.class, "left");
		Attribute attribute2 = new Attribute(String.class, "right");
		Attribute attribute3 = new Attribute(Integer.class, "Fact");
		MainMemoryRelation equaltable = new MainMemoryRelation("EQUALITY", Lists.newArrayList(attribute1, attribute2, attribute3));
		relations.put("EQUALITY", equaltable);
	}

	public void addNewTable( Relation relation ){
		// Also adds a new table
		this.relations.put(relation.getName(), new MainMemoryRelation(relation));
	}

	public void dropTable( String name ){
		relations.remove(name);
	}
	public void editTable( MainMemoryRelation table ){
		// Edits or inserts table
		relations.put(table.getName(), table);
	}
	public MainMemoryRelation getTable( String name ){
		return this.relations.get(name);
	}
	public void addTuples( Collection<Atom> tuples ){
		for(Atom tuple: tuples ){
			MainMemoryRelation temp = this.relations.get(tuple.getName());
			temp.addTuple(tuple);
			this.relations.put(temp.getName(), temp);
		}
	}
	public void dropTuples( Collection<Atom> tuples ){
		
		for( Atom tuple: tuples){
			MainMemoryRelation temp = this.relations.get(tuple.getName());	
			temp.dropTuple(tuple);
			this.relations.put(temp.getName(), temp);
		}
	
	}
	
	public String toString(){
		String temp = new String("Database contents:\n");
		for( String name : this.relations.keySet()){
			temp += "Alias: "+ name + " Original name: "+this.relations.get(name).toString()+"\n";
		}
		return temp;
	}
	


}
