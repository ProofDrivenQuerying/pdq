* `uk.ac.ox.cs.pdq.algebra`
  [ ] `Predicate` was renamed to `Atom` in `staging`: 
  we need to converge on which term to use. I think it is good to use Atom as there is another `Predicate` class in common, and it also conflict with `java.util.Predicate`. The `getPredicateName()` method can stay the same though.
  [ ] In `Access` and `Access`, remove all references to "outputTerms", which is redundant with "columns". 
  We can consider renaming columns to outputTerms if this is too misleading.
* `uk.ac.ox.cs.pdq.algebra.predicate`:
  [ ] The `flatten()` method signature in `Predicate` interface should be removed, unless it is clear (and documented) what flattening something else than a `ConjunctivePredicate` actually mean. Maybe a more general and sound approach to would be to use rewriters (see the Rewriter interface)
  [ ] The `public static` method was made `private` non-`static` in `ConjunctivePredicate`. It should be `static`, because the method is stateless, and I don't see why not make it `public` since it can be used by other classes elsewhere (in particular, but not only, in this package).
  [ ] `ExtendedAttributeEqualityPredicate` vs. `ExtendedAttributeInEqualityPredicate`
  I don't see why two classes are needed here neither what exactly what the latter is supposed to do. The `satisfies()` method seems to do the opposite of the parent class, which at least ought to be explained.
* `uk.ac.ox.cs.pdq.db`:
  [ ] Maybe it is time for the `Schema.isCyclic()` method to retire, and be replaced with a smarter method, for example, that checks if the schema is weakly acyclic.
	Also, the whole dependency on the jgrapht library is solely for this method, although, it could be done directly.
  [ ] The `Relation` class now has keys as part of the EGD-related changes, but:
    - You might want to allow multiple keys for a given relation (conversely, what happens if a `Schema` defines multiply EGDs on the same relation ?)
    - The way the key is currently handled breaks the immutability of the Relation class.
    Two possible alternatives: (i) make the key part of the main constructor, (ii) externalize key management to the schema, i.e. makes keys a separate class, which will also have the advantage of being more flexible about single vs. multiple keys
  [ ] `TGD` (and `EGD`):
    Potential bug (also present in master, staging): `getFree()` returns the universal variables. It should return the empty list. But then, we should make sure every place where it is used actually use getUniversal instead.
* `uk.ac.ox.cs.pdq.fol`
  [ ] `CanonicalNameGenerator` was moved inside the `Skolem` constant class.
	I am not strongly opposed to this in general, but since we have needs for a name-generator in various part of the program (see bug-#6), it would make sense to make the class more generic, give the name `NameGenerator` and possibly move it to `uk.ac.ox.cs.pdq.util`.
  [ ] `Equality`: I would suggest to be the class `final`, unless there is a plan to later have "specialized" types of `Equality` (although I don't what would that look like).
  [ ] `ConjunctiveQuery`: 
  I have changed this class quite a few times, by rearranging the way canonical mappings are handled, to ensure the class remains immutable, but the old way always comes back.
  There should be no `setGrounding()` in this class: *this is a magnet for bugs!*.
  It is OK to have a method there generate a grounding from scratch, but ideally, the mapping should be kept outside, and applied to the query when necessary. (In fact, the comment applies to all formulas.)
  [ ] Same class. 
  A method was rename to `inferGround`, but this is misleading because the grounding to generated rather than inferred. I am not a big fan on method names with numbers in them (`getFree2Canonical`). They look like indices (alternative to some other `getFreeCanonical` method).
* uk.ac.ox.cs.plan
  [ ] It is not clear to me why all the new classes in this package, which seems resurrected from the very first version of PDQ. Maybe that will become clearer when I look at the planner code, but anyway I would suggest, to added some explain about this (for instance, in the package-info.java file)
  Overall, this change seems to be entirely Linear-planner-specific, so if it needs to be, it should be done there.
  [ ] Regarding this above item, the `Table` class now has a name field like in the old days.
  `Table` is really just a collection of `Tuple`s, so allowing to have a name is a bit strange. Most importantly, this change also seems to be there to accommodate linear-planner-specific things, so please do this in a sub-class in the linear package instead.
* `uk.ac.ox.cs.pdq.util`
	[ ] `IndexedDirectpGraph` should be moved to planner util.
