package uk.ac.ox.cs.pdq.io.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.ox.cs.pdq.io.Reader;

// TODO: Auto-generated Javadoc
/**
 * Reads experiment sample elements from XML.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public abstract class AbstractXMLReader<T> extends DefaultHandler implements Reader<T> {

	/** Logger. */
	private static Logger log = Logger.getLogger(AbstractXMLReader.class);

	/**
	 * To int list.
	 *
	 * @param str the str
	 * @return a list of integer, from an String input comma-separated int list.
	 * @throws NullPointerException if the given str is null
	 * @throws NumberFormatException if the input string is not well-formed.
	 */
	protected static List<Integer> toIntList(String str) {
		if (str != null && !str.trim().isEmpty()) {
			String[] vals = str.split(",");
			List<Integer> result = new ArrayList<>(vals.length);
			for (int i = 0, l = vals.length; i < l; i++) {
				result.add(Integer.valueOf(vals[i]));
			}
			return result;
		}
		return new ArrayList<>();
	}

	/**
	 * Gets the value.
	 *
	 * @param att the att
	 * @param qn the qn
	 * @return the attribute value of the given qname
	 */
	protected String getValue(Attributes att, QNames qn) {
		return att.getValue(qn.format());
	}

	/**
	 * Gets the int value.
	 *
	 * @param att the att
	 * @param qn the qn
	 * @return the attribute value of the given qname
	 */
	protected Integer getIntValue(Attributes att, QNames qn) {
		try {
			String s = att.getValue(qn.format());
			if (s != null) {
				return Integer.valueOf(s);
			}
		} catch (NumberFormatException e) {
			log.debug(AbstractXMLReader.class + "", e);
			return null;
		}
		return null;
	}
}
