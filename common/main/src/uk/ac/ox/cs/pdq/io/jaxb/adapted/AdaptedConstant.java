// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;

/**
 * @author Gabor, Fergus Cooper
 *
 */
public class AdaptedConstant extends AdaptedVariable {

	/** Logger. */
	private static Logger log = Logger.getLogger(AdaptedQuery.class);

	private Type type;
	private String value;

	public AdaptedConstant() {
	}

	public AdaptedConstant(Constant value) {
		if (value instanceof TypedConstant) {
			this.type = ((TypedConstant) value).getType();
			this.value = String.valueOf(((TypedConstant) value).getValue());

		} else if (value instanceof UntypedConstant) {
			this.value = ((UntypedConstant) value).getSymbol();
		} else {
			throw new IllegalArgumentException();
		}
	}

	@XmlAttribute(name = "value")
	public String getValue() {
		return this.value;
	}

	public Constant toConstant() {
		return toConstant(null);
	}

	public Constant toConstant(Type preferedType) {
		if (type == null) {
			type = preferedType;
		}

		// Type may be null because it is unspecified in an xml file such as query.xml. Check here for potentially
		// common mistake of having not specified a numeric type as being a number. Interpret such cases as being either
		// double or int
		if (type == null) {
			try {
				double val = Double.parseDouble(this.value);
				type = Double.class;
				String type_name = "java.lang.Double";

				if (Math.rint(val) == val) {
					type = Integer.class;
					type_name = "java.lang.Integer";
				}

				log.warn("Constant " + this.value + " does not specify a type but is numeric. Should a type have been" +
						" specified in a query.xml file or similar? Interpreting as type: " + type_name);

			} catch (NumberFormatException ignored) {}
		}

		// if type is still null, send a general warning that it has not been possible to infer the type: this may
		// still be an error that would otherwise be hard to track down
		if (type == null) {
			log.warn("Constant " + this.value + " does not specify a type and could not be inferred to be numeric." +
					"Should a type have been specified in a query.xml file or similar?");
		}

		TypedConstant ret = null;
		if (type != null && type == Double.class) {
			ret = TypedConstant.create((Double)(Double.parseDouble(value)));
		} else if (type != null && type == Integer.class) {
			try {
				ret = TypedConstant.create((Integer)(Integer.parseInt(value)));
			}catch (NumberFormatException e) {
				ret = TypedConstant.create(value);
			}
		} else if (type!=null && (type == java.sql.Date.class || type == java.util.Date.class)) {
			SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dDate;
			try {
				dDate = sdfmt1.parse( value );
				if (type == java.sql.Date.class ) {
					ret = TypedConstant.create(new java.sql.Date(dDate.getTime()));
				} else if (type == java.util.Date.class ) {
					ret = TypedConstant.create(new java.util.Date(dDate.getTime()));
				}
			} catch (ParseException e) {
				// ignored, could be user's incorrect xml file.
				e.printStackTrace();
				ret = TypedConstant.create(value);
			}
		} else {
			Constructor<?> constructor=null;
			try {
				if (type != null) constructor = Class.forName(type.getTypeName()).getConstructor(String.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (constructor!=null) {
				try {
					ret = TypedConstant.create(constructor.newInstance(value));
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			if (ret == null)
				ret = TypedConstant.create(value);
		}
		if (type!=null && ret.getType() != type) {
			throw new IllegalArgumentException("Type should match! Expected type:" + type + " generated value's type:" + ret.getType() );
		}
		return ret;
	}

	public void setType(Type type) {
		this.type = type;
	}
	@XmlAttribute(name = "type")
	public void setType(String type) {
		this.type = TypedConstant.convertStringToType(type);
	}
	public String getType() {
		if (type!= null && type != String.class)
			return type.getTypeName();
		return null;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setValue(Integer value) {
		this.value = "" + value;
	}

	public String toString() {
		return value;
	}

}
