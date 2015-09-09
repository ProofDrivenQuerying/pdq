package uk.ac.ox.cs.pdq.rewrite;

/**
 * Interface common to rewriter classes.
 *
 * @author Julien Leblay
 *
 * @param <I>
 * @param <O>
 */
public interface Rewriter<I, O> {

	/**
	 * @param input I
	 * @return an object of type O which is a rewriting of the given input.
	 */
	O rewrite(I input) throws RewriterException;
}
