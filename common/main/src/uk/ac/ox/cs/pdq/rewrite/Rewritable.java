package uk.ac.ox.cs.pdq.rewrite;


/**
 * Common interface to all rewritable objects.
 * 
 * TOCOMMENT I don't get the line below
 * 
 * Generally only call the given rewritten onto the current object.
 * 
 * @author Julien Leblay
 */
public interface Rewritable {
	
	/**
	 * Rewrite.
	 *
	 * @param <I> the generic type
	 * @param <O> the generic type
	 * @param rewriter the rewriter
	 * @return the o
	 * @throws RewriterException the rewriter exception
	 */
	<I extends Rewritable, O> O rewrite(Rewriter<I, O> rewriter) throws RewriterException;
}