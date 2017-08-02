package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;

/**
 * @author Gabor
 *
 */
@XmlType(propOrder = { "type", "name", "inputs" })
public class AdaptedAccessMethod implements Serializable {
	protected static final long serialVersionUID = -5821292665848480210L;
	private String inputs = null;
	private String name;

	public AdaptedAccessMethod() {
	}

	public AdaptedAccessMethod(String name, Integer[] inputs) throws Exception {
		this.setName(name);
		this.setInputs(createCommaSeparatedString(inputs));
	}

	public AccessMethod toAccessMethod() {
		try {
			if (getInputs() != null)
				return AccessMethod.create(getName(), parseIntArrayFromCommaSeparatedList(getInputs()));
			return AccessMethod.create(getName(), new Integer[] {});
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@XmlAttribute
	public String getInputs() {
		if (inputs != null && inputs.length() == 0)
			return null;
		return this.inputs;
	}

	@XmlAttribute
	public String getType() {
		if (inputs == null || inputs.length() == 0)
			return "FREE";
		return "LIMITED";
	}

	public void setType(String type) {
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setInputs(String inputs) {
		this.inputs = inputs;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * By default Jaxb creates space separated lists, so we need to do manual
	 * converting to get comma separated list of numbers. Mostly used in the
	 * "inputs" attribute.
	 */
	private Integer[] parseIntArrayFromCommaSeparatedList(final String string) {
		try {
			final List<Integer> ints = new ArrayList<Integer>();
			if (string == null || string.isEmpty()) {
				// this case is only possible when we have remote sources declared.
				return ints.toArray(new Integer[] {});
			}
			for (final String s : string.split(",")) {
				final String trimmed = s.trim();

				if (trimmed.length() > 0) {
					ints.add(Integer.parseInt(trimmed));
				}
			}
			return ints.toArray(new Integer[] {});
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

	/**
	 * By default Jaxb creates space separated lists, so we need to do manual
	 * converting to get comma separated list of numbers. Mostly used in the
	 * "inputs" attribute.
	 */
	private String createCommaSeparatedString(Integer[] numbers) throws Exception {
		try {
			final StringBuilder sb = new StringBuilder();
			for (final Integer number : numbers) {
				if (sb.length() > 0) {
					sb.append(",");
				}

				sb.append(number);
			}
			return sb.toString();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

}
