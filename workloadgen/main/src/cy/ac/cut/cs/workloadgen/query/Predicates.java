/**
 * 
 */
package cy.ac.cut.cs.workloadgen.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Relation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Represents a conjunction of join and filter predicates.
 * 
 * @author herodotos.herodotou
 */
public class Predicates {

   private List<JoinPredicate> joinPredicates;
   private List<FilterPredicate> filterPredicates;

   // Maintained for fast lookups
   private Set<JoinPredicate> joinPredSet;
   private Set<FilterPredicate> filterPredSet;

   /**
    * Default constructor
    */
   public Predicates() {
      this.joinPredicates = new ArrayList<JoinPredicate>();
      this.filterPredicates = new ArrayList<FilterPredicate>();
      this.joinPredSet = new HashSet<JoinPredicate>();
      this.filterPredSet = new HashSet<FilterPredicate>();
   }
   
   private Predicates(List<JoinPredicate> joinPredicates, List<FilterPredicate> filterPredicates, Set<JoinPredicate> joinPredSet, Set<FilterPredicate> filterPredSet) {
	      this.joinPredicates = joinPredicates;
	      this.filterPredicates = filterPredicates;
	      this.joinPredSet = joinPredSet;
	      this.filterPredSet = filterPredSet;
   }

   /**
    * @return the joinPredicates
    */
   public List<JoinPredicate> getJoinPredicates() {
      return joinPredicates;
   }

   /**
    * @return the filterPredicates
    */
   public List<FilterPredicate> getFilterPredicates() {
      return filterPredicates;
   }

   /**
    * Add a join predicate if it doesn't exist already
    * 
    * @param joinPredicate
    * @return
    */
   public boolean addJoinPredicate(JoinPredicate joinPredicate) {
      if (joinPredSet.contains(joinPredicate))
         return false;

      joinPredSet.add(joinPredicate);
      joinPredicates.add(joinPredicate);
      return true;
   }

   /**
    * Add a filter predicate if it doesn't exist already
    * 
    * @param filterPredicate
    * @return
    */
   public boolean addFilterPredicate(FilterPredicate filterPredicate) {
      if (filterPredSet.contains(filterPredicate))
         return false;

      filterPredSet.add(filterPredicate);
      filterPredicates.add(filterPredicate);
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      int count = 0;

      for (JoinPredicate joinPredicate : joinPredicates) {
         if (count > 0)
            sb.append("\n\t AND ");

         sb.append(joinPredicate.toString());
         ++count;
      }

      for (FilterPredicate filterPredicate : filterPredicates) {
         if (count > 0)
            sb.append("\n\t AND ");

         sb.append(filterPredicate.toString());
         ++count;
      }

      return sb.toString();
   }
   
   @Override
   public Predicates clone() {
	   return new Predicates(Lists.newArrayList(joinPredicates), Lists.newArrayList(filterPredicates), Sets.newHashSet(joinPredSet), Sets.newHashSet(filterPredSet));
   }
   
	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return Predicates.class.isInstance(o)
				&& this.filterPredicates.equals(((Predicates) o).filterPredicates)
				&& this.joinPredicates.equals(((Predicates) o).joinPredicates);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.filterPredicates,this.joinPredicates);
	}

}
