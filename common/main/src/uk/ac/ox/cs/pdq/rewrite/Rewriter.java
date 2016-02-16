package uk.ac.ox.cs.pdq.rewrite;

// TODO: Auto-generated Javadoc
/**
 * Interface common to rewriter classes.
 *
 * @author Julien Leblay
 * @param <I> the generic type
 * @param <O> the generic type
 */
public interface Rewriter<I, O> {

	/**
	 * Rewrite.
	 *
	 * @param input I
	 * @return an object of type O which is a rewriting of the given input.
	 * @throws RewriterException the rewriter exception
	 */
	O rewrite(I input) throws RewriterException;
}
