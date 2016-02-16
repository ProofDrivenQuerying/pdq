package uk.ac.ox.cs.pdq.fol;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.rewrite.Rewritable;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;

// TODO: Auto-generated Javadoc
/**
 * A first order formula.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class AbstractFormula implements Formula {

	/** The log. */
	protected static Logger log = Logger.getLogger(Formula.class);
	
	/** The global id. */
	protected static int globalId = 0;

	/**  Formula's identifier. */
	protected final int id;

	/**
	 * Instantiates a new abstract formula.
	 */
	public AbstractFormula() {
		this.id = globalId++;
	}

	/**
	 * Gets the id.
	 *
	 * @return int
	 */
	public int getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.rewrite.Rewritable#rewrite(uk.ac.ox.cs.pdq.rewrite.Rewriter)
	 */
	@Override
	public <I extends Rewritable, O> O rewrite(Rewriter<I, O> rewriter) throws RewriterException {
		return rewriter.rewrite((I) this);
	}
}
