package cy.ac.cut.cs.workloadgen.translator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cy.ac.cut.cs.workloadgen.query.FilterPredicate;
import cy.ac.cut.cs.workloadgen.query.JoinPredicate;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;
import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.Table;

/**
 * Translates SQL queries to the internal PDQ conjunctive queries  
 * @author Efthymia Tsamoura
 *
 */
public class QueryTranslator {

	public static uk.ac.ox.cs.pdq.fol.ConjunctiveQuery translate(Query query) {
		return new ConjunctiveQuery(createHead(query), Conjunction.of(createBody(query)));		
	}
	
	public static uk.ac.ox.cs.pdq.db.LinearGuarded translate(View view) {
		return new LinearGuarded(createHead(view), Conjunction.of(createBody(view)));		
	}
	
	private static Atom createHead(Query query) {
		//Create the Atom of the query
		List<Term> headTerms = Lists.newArrayList();
		for(Attribute attribute:query.getSelectClause()) {
			headTerms.add(new Variable(attribute.getName()));
		}
		return new Atom(new Predicate(query.getName(), headTerms.size()), headTerms);
	}
	
	private static Collection<Atom> createBody(Query query) {
		//Get the query's tables
		Set<Table> tables = query.getFromClause();
		Map<Table,List<Term>> termsMap = Maps.newLinkedHashMap();
		for(Table table:tables) {
			//Generate one predicate per table
			List<Term> terms = Lists.newArrayList();
			for(Attribute attribute:table.getAttributesList()) {
				terms.add(new Variable(attribute.getName()));
			}
			termsMap.put(table, terms);
		}
		
		//Create equijoin predicates
		for(JoinPredicate joinPredicate:query.getWhereClause().getJoinPredicates()) {
			Attribute left = joinPredicate.getAttr1();
			Attribute right = joinPredicate.getAttr2();
			
			//The query must not project both join attributes
			Preconditions.checkArgument(!(query.getSelectClause().contains(left) && query.getSelectClause().contains(right)));
			if(query.getSelectClause().contains(right)) {
				left = joinPredicate.getAttr2();
				right = joinPredicate.getAttr1();
			}
			
			Table leftTable = left.getTable();
			Table rightTable = right.getTable();
			int leftIndex = leftTable.getAttributesList().indexOf(left);
			int rightIndex = rightTable.getAttributesList().indexOf(right);
			Preconditions.checkArgument(leftIndex >=0 && rightIndex >= 0);
			Term leftTerm = termsMap.get(leftTable).get(leftIndex);
			termsMap.get(rightTable).set(rightIndex, leftTerm);
		}
		
		//Create filter predicates
		for(FilterPredicate filterPredicate:query.getWhereClause().getFilterPredicates()) {
			Attribute left = filterPredicate.getAttribute();
			Object value = filterPredicate.getValue();
			Table leftTable = left.getTable();
			int leftIndex = leftTable.getAttributesList().indexOf(left);
			Preconditions.checkArgument(leftIndex >=0);
			termsMap.get(leftTable).set(leftIndex, new TypedConstant(value));
		}
		
		//Create the body of the query
		Collection<Atom> body = Lists.newArrayList();
		for(Entry<Table, List<Term>> entry:termsMap.entrySet()) {
			Table table = entry.getKey();
			List<Term> terms = entry.getValue();
			body.add(new Atom(new Predicate(table.getName(), terms.size()), terms));
		}
		return body;
	}

	
}
