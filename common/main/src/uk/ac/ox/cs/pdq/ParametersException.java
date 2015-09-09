package uk.ac.ox.cs.pdq;




/**
 * Exception that occurred when parameters cannot be loaded or set properly.
 *
 * @author Julien Leblay
 */
public class ParametersException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * @param msg exception message.
	 */
	public ParametersException(String msg) {
		super(msg);
	}

	/**
	 */
	public ParametersException() {
		super();
	}
}
