package uk.ac.ox.cs.pdq;




/**
 * Exception that occurrs when an inconsistency is found among parameters.
 *
 * @author Julien Leblay
 */
public class InconsistentParametersException extends ParametersException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Instantiates a new inconsistent parameters exception.
	 *
	 * @param msg exception message.
	 */
	public InconsistentParametersException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new inconsistent parameters exception.
	 */
	public InconsistentParametersException() {
		super();
	}
}
