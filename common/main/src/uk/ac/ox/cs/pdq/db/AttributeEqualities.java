package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class AttributeEqualities {
	// Contains a set of the equalities between attributes which will be implemented in a join
	// Example: in a join where A0.x1 = A1.x2 and A2.x0 = A0.x3, entries would be:
	//		<<A0, x1>, <A1, x2>>
	//		<<A2, x0>, <A0, x3>>
	Set<Pair<Pair<String, String>, Pair<String, String>>> equalities;
	
	public AttributeEqualities(){
		equalities = new HashSet<Pair<Pair<String, String>, Pair<String,String>>>();
	}
	
	// Adds a single equality to the list
	public void addEquality( Pair<Pair<String, String>, Pair<String, String>> newEquality ){
		this.equalities.add(newEquality);
	}
	
	// Given two pairs (ie, <A1, x0> and <A2, x2>) creates a new equality and adds it to the list
	public void put(Pair<String, String> left, Pair<String, String> right){
		this.addEquality(Pair.of(left, right));
	}
	
	// Adds multiple equalities to the existing equalities
	public void addEqualities( Collection<Pair<Pair<String, String>, Pair<String, String>>> newEqualities){
		for( Pair<Pair<String, String>, Pair<String, String>> newEquality : newEqualities ){
			this.addEquality( newEquality );
		}
	}
	
	// Gets all the left sides of the equalities (if a <table, attr> pair occurs multiple times, it is returned multiple times.)
	public List<Pair<String, String>> getLeftEqualities(){
		List<Pair<String, String>> lefts = new ArrayList<Pair<String,String>>();
		for( Pair<Pair<String, String>, Pair<String,String>> equality : this.getEqualities()){
			lefts.add( equality.getLeft());
		}
		return lefts;
	}
	
	// Returns a list of all <table, attr> pairs that a given <table, attr> is equal to.
	// Does NOT work transitively! i.e. if left = <A1, x1> and <<A1, x1>, <A0, x0>> and <<A0, x0>, <A2, x2>> exist but <<A1, x1>, <A2, x2>> does not then <A2, x2> will not be an entry of the returned list
	// If the given left side does not correspond to any right sides, an empty list is returned
	// Also does not work if the given left is only stored as a right, i.e. if left=<A0, x0> and the only entry is <<A1, x0>, <A0, x0>>, this returns empty
	public List<Pair<String, String>> get( Pair<String, String> left ){
		List<Pair<String, String>> rights = new ArrayList<Pair<String, String>>();
		for( Pair<Pair<String, String>, Pair<String, String>> equality : this.equalities){
			if( equality.getLeft().equals(left)){
				rights.add(equality.getRight());
			}
		}
		return rights;
	}
	
	// Returns all equalities
	public Set<Pair<Pair<String, String>, Pair<String, String>>> getEqualities(){
		return this.equalities;
	}
	
	// Removes a specific equality
	public void dropEquality( Pair<Pair<String, String>, Pair<String, String>> oldEquality ){
		this.equalities.remove(oldEquality);
	}
	
	// Checks whether any equalities exist
	public boolean isEmpty(){
		return this.equalities.isEmpty();
	}
	
	// Contents of this.equalities
	public String toString(){
		String result = "";
		for( Pair<Pair<String, String>, Pair<String, String>> equality: this.getEqualities()){
			result += equality.getLeft().getLeft() + "." + equality.getLeft().getRight() + "=" + equality.getRight().getLeft()+"."+equality.getRight().getRight() + "\n";
		}
		return result;
	}
	
}
