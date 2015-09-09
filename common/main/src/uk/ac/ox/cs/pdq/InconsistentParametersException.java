package uk.ac.ox.cs.pdq;




/**
 * Exception that occurred when an inconsistency is found among parameters.
 *
 * @author Julien Leblay
 */
public class InconsistentParametersException extends ParametersException {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * @param msg exception message.
	 */
	public InconsistentParametersException(String msg) {
		super(msg);
	}

	/**
	 */
	public InconsistentParametersException() {
		super();
	}
}
