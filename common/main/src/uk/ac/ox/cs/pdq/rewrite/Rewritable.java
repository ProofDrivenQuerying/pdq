package uk.ac.ox.cs.pdq.rewrite;

/**
 * Common interface to all rewritable objects.
 * Generally only call the given rewritten onto the current object.
 * 
 * @author Julien Leblay
 */
public interface Rewritable {
	<I extends Rewritable, O> O rewrite(Rewriter<I, O> rewriter) throws RewriterException;
}