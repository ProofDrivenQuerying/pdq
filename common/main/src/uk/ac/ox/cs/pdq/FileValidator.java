package uk.ac.ox.cs.pdq;

import java.io.File;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * Filters out files that do not exist or are directories.
 * @author Julien LEBLAY
 */
public class FileValidator implements IParameterValidator {
	
	/* (non-Javadoc)
	 * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
	 */
	@Override
	public void validate(String name, String value) throws ParameterException {
		try {
			File f = new File(value);
			if (!f.exists() || f.isDirectory()) {
				throw new ParameterException(name + " must be a valid configuration file.");
			}
		} catch (Exception e) {
			throw new ParameterException(name + " must be a valid configuration file.");
		}
	}
}