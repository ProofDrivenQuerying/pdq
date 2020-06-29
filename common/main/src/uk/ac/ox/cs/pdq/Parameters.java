// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import uk.ac.ox.cs.pdq.exceptions.ParametersException;

/**
 * A general parameters utility class, which allows getting and setting typed
 * properties, either through the loose properties methods, or stricter
 * CamelCase getter and setter methods.
 *
 * @author Julien Leblay
 */
public abstract class Parameters extends Properties {

	/**  Generated serial ID. */
	private static final long serialVersionUID = -722499380053104769L;

	/**
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited @Target(ElementType.FIELD)
	public static @interface Parameter {
		
		/**
		 * Description.
		 *
		 * @return the string
		 */
		String description();
		
		/**
		 * Default value.
		 *
		 * @return the string
		 */
		String defaultValue() default ""; 
	}

	/**
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited @Target(ElementType.FIELD)
	public static @interface EnumParameterValue {
		
		/**
		 * 
		 *
		 * @return the string
		 */
		public abstract String description();
	}

	/** */
	private static Logger log = Logger.getLogger(Parameters.class);

	/**   */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/** The getters. */
	private Map<String, Method> getters = new LinkedHashMap<>();
	
	/** The setters. */
	private Map<String, Method> setters = new LinkedHashMap<>();

	/**  If true, throws exception when parameter-related problems occur. */
	private final boolean strict;

	/**  If true, reports when parameter-related problems occur. */
	private final boolean verbose;

	/**
	 * Forces the loading of an alternate filename. Note: the properties
	 * attribute is not reinitialize, allowing to use entries defined in the
	 * default location as default variables.
	 *
	 * @param configFile the config file
	 * @param verbose the verbose
	 * @param strict if true, param loading problem will throw an exception
	 */
	protected Parameters(File configFile, boolean verbose, boolean strict) {
		this.strict = strict;
		this.verbose = verbose;
		if (configFile!=null) {
			this.load(configFile, verbose, strict);
		}
		this.updateAccessors();
		if (configFile!=null) {
			// you have to load twice!
			this.load(configFile, verbose, strict);
		}
	}

	/**
	 * Forces the loading of an alternate filename. Note: the properties
	 * attribute is not reinitialize, allowing to use entries defined in the
	 * default location as default variables.
	 */
	protected Parameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
	 * loads the properties from the specified file.
	 *
	 * @param configFile File
	 * @param verbose the verbose
	 * @param strict boolean
	 */
	protected void load(File configFile, boolean verbose, boolean strict) {
		if (configFile.exists()) {
			log.debug("Reading initialConfig file " + configFile + "...");
			// load the properties from the given file
			try (FileInputStream fis = new FileInputStream(configFile);) {
				this.load(fis);
				for (String k: this.getters.keySet()) {
					Object v = super.get(k);
					if (v != null) {
						this.set(k, v, verbose, strict);
					} else if (verbose) {
						log.info("Value of '" + k + "' is null. Ignored.");
					} else {
						log.debug("Value of '" + k + "' is null. Ignored.");
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		} else if (strict) {
			throw new ParametersException("Unable to load " + configFile.getAbsolutePath());
		}
	}

	/**
	 * Stores and update mapping between field canonical names and accessors;.
	 */
	private void updateAccessors() {
		for (Field f : this.getAllFields()) {
			Annotation a = f.getAnnotation(Parameter.class);
			if (a != null) {
				String canonicalName = toCanonicalName(f.getName());
				String getterName = "get" + toUpperCamelName(canonicalName);
				String setterName = "set" + toUpperCamelName(canonicalName);
				try {
					this.getters.put(canonicalName,
							this.getClass().getMethod(
									getterName, new Class<?>[] {}));
					if (Number.class.isAssignableFrom(f.getType())) {
						this.setters.put(canonicalName,
								this.getClass().getMethod(
										setterName, new Class<?>[] { Number.class }));
					} else if (f.getType().isEnum()) {
						this.setters.put(canonicalName,
								this.getClass().getMethod(
										setterName, new Class<?>[] { String.class }));
					} else {
						this.setters.put(canonicalName,
								this.getClass().getMethod(
										setterName, new Class<?>[] { f.getType() }));
					}
				} catch (IllegalArgumentException e) {
					log.warn("Could not invoke target on field " + f.getName() + ".", e);
				} catch (NoSuchMethodException e) {
					log.warn("Unable to access parameters " + canonicalName + ".", e);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		assert this.getters.size() == this.setters.size();
	}

	/**
	 * Type-checks the given objects against Boolean, Number and String
	 * in this order, and return a cast of the first compatible type.
	 *
	 * @param o the o
	 * @return a cast of the given o into the first match type among Boolean,
	 * Number and String
	 */
	private Object narrowType(Object o) {
		if (o == null) {
			return null;
		}
		String s = String.valueOf(o).trim();
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		if (!s.isEmpty() && "truefalse".contains(s.toLowerCase())) {
			return Boolean.parseBoolean(s);
		}
		try {
			return NumberFormat.getNumberInstance().parse(s);
		} catch (ParseException e) {
			log.debug(e);
			// Not a number
		}
		return s;
	}

	/**
	 * 
	 *
	 * @param camelName String
	 * @return String
	 */
	private static String toCanonicalName(String camelName) {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelName);
	}

	/**
	 * To loweramel name.
	 *
	 * @param canonicalName String
	 * @return String
	 */
	private static String toLoweramelName(String canonicalName) {
		return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, canonicalName);
	}

	/**
	 * To upper camel name.
	 *
	 * @param canonicalName String
	 * @return String
	 */
	private static String toUpperCamelName(String canonicalName) {
		return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, canonicalName);
	}

	/**
	 * Checks whether the given property is part of these parameters.
	 *
	 * @param k the k
	 * @return true, if the Parameters have property k
	 */
	protected boolean hasProperty(Object k) {
		try {
			this.getClass().getDeclaredField(toLoweramelName(String.valueOf(k)));
			return true;
		} catch (NoSuchFieldException e) {
			log.debug(e);
			return false;
		}
	}

	/**
	 * Sets the member of a initialConfig helper with the value specified in the
	 * underlying Properties.
	 *
	 * @param k the k
	 * @param v the v
	 */
	public void set(Object k, Object v) {
		Preconditions.checkArgument(v != null);
		this.set(k, v, this.verbose, this.strict);
	}

	/**
	 * Sets the member of a initialConfig helper with the value specified in the
	 * underlying Properties.
	 *
	 *
	 */
	public void set(Object k, Object v, boolean verbose, boolean strict) {
		Preconditions.checkArgument(v != null);
		try {
			Method m = this.setters.get(k);
			Object arg = this.narrowType(v);
			if (m != null
					&& m.getParameterTypes().length == 1
					&& m.getParameterTypes()[0].isAssignableFrom(arg.getClass())) {
				m.invoke(this, arg);
			} else {
				if (verbose) {
					log.warn("Unable to set " + k + " to " + arg);
				}
				if (strict) {
					throw new IllegalStateException("Unable to set " + k + " to " + arg);
				}
			}
		} catch (ReflectiveOperationException e) {
			if (strict) {
				throw new IllegalStateException(e);
			}
		} catch (IllegalArgumentException e) {
			if (verbose) {
				log.warn("Unable to set " + k + ". Cause: " + e.getMessage());
			}
			if (strict) {
				throw e;
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Gets the member of a initialConfig helper whose value is given as
	 * parameter.
	 *
	 * 
	 */
	@Override
	public synchronized Object get(Object k) {
		try {
			Method m = this.getters.get(k);
			if (m != null) {
				return m.invoke(this);
			} else if (this.strict) {
				throw new IllegalStateException("No getter for " + k);
			}
			return null;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 *
	 *
	 * @return Collection<Field>
	 */
	protected Collection<Field> getAllFields() {
		LinkedList<Field> result = new LinkedList<>();
		Class<?> current = this.getClass();
		do {
			for (Field f : current.getDeclaredFields()) {
				result.add(f);
			}
			current = current.getSuperclass();
		} while (current.getSuperclass() != null);

		return result;
	}

	/**
	 * 
	 *
	 * @return a string containing of the parameters and the current values,
	 * as they would appear in the file, i.e. the returned String can be used
	 * for serialization purposes.
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * 
	 *
	 * 
	 * @return a string containing of the parameters and the current values,
	 * as they would appear in the file, i.e. the returned String can be used
	 * for serialization purposes.
	 */
	public synchronized String toString(boolean verbose) {
		StringBuilder result = new StringBuilder();
		for (Field f : this.getAllFields()) {
			Annotation a = f.getAnnotation(Parameter.class);
			if (a != null) {
				try {
					String canonicalName = toCanonicalName(f.getName());
					Object value = this.get(canonicalName);
					Parameter p = (Parameter) a;
					if (value == null && !p.defaultValue().isEmpty()) {
						value = p.defaultValue();
					}
					// Don't output params that are optional and whose value
					// is currently unassigned.
					if (value != null && !"".equals(value)) {
						if (verbose) {
							result.append("#\n# ")
									.append(p.description().replace("\n", "\n# ")).append("\n# Type: ")
									.append(f.getType().getSimpleName());
							if (!p.defaultValue().isEmpty()) {
								result.append("\n# Default: ").append(p.defaultValue());
							} else {
								result.append("\n# (Optional)");
							}
							result.append("\n");
							if (f.getType().isEnum()) {
								result.append("# Possible value:\n");
								for (Object o : f.getType().getEnumConstants()) {
									assert o instanceof Enum;
									Enum<?> e = (Enum<?>) o;
									try {
										Annotation v = e
												.getClass()
												.getField(e.name())
												.getAnnotation(
														EnumParameterValue.class);
										EnumParameterValue pv = (EnumParameterValue) v;
										result.append("#\t- ")
												.append(e.name())
												.append(": ")
												.append(pv.description()
														.replace("\n",
																"\n#\t  "))
												.append("\n");
									} catch (NoSuchFieldException
											| SecurityException e1) {
										e1.printStackTrace();
										log.error(e1.getMessage(),e1);
									}
								}
							}
						}
						result.append(canonicalName + " = ");
						result.append(value).append('\n');
					}
				} catch (IllegalArgumentException e) {
					log.warn("Could not invoke target on field " + f.getName() + ".", e);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage(),e);
				}
			}
		}
		return result.toString();
	}

	/**
	 * C
	 *
	 * @param params Parameters[]
	 * @return Parameters
	 */
	public static Parameters combine(Parameters... params) {
		return new CombinedParameters(params);
	}

	/**
	 * An aggregation of several parameters objects.
	 *
	 * @author Julien Leblay
	 */
	public static class CombinedParameters extends Parameters {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 5262156763918477392L;

		/** The sub params. */
		public final Set<Parameters> subParams;

		/**
		 * Constructor for CombinedParameters.
		 * @param params Parameters[]
		 */
		public CombinedParameters(Parameters... params) {
			this.subParams = ImmutableSet.copyOf(params);
			Preconditions.checkState(!hasNameClashes(params));
		}

		/**
		 * Checks for name clashes.
		 *
		 * @param params Parameters[]
		 * @return boolean
		 */
		private static boolean hasNameClashes(Parameters... params) {
			return false;
		}

		/**
		 * 
		 *
		 * @param k Object
		 * @param v Object
		 */
		@Override
		public void set(Object k, Object v) {
			for (Parameters param: this.subParams) {
				if (param.hasProperty(k)) {
					param.set(k, v);
					return;
				}
			}
			throw new IllegalStateException("Unable to set property '" +
					k + "' to value '" + v + "'");
		}

		/**
		 * 
		 *
		 * @param k Object
		 * @return Object
		 * @see java.util.Map#get(Object)
		 */
		@Override
		public synchronized Object get(Object k) {
			for (Parameters param: this.subParams) {
				if (param.hasProperty(k)) {
					return param.get(k);
				}
			}
			throw new IllegalStateException("Unable to get property '" +
					k + "'");
		}
	}

	/** The seed. */
	@Parameter(
			description="Randomizer seed shared by all randomizer across the PDQ libraries.",
			defaultValue = "0"
	)
	protected Integer seed = 0;

	@Parameter(
			description = "Time limit (in ms).",
			defaultValue = "Infinity")
	protected Double timeout = 2*60*1000d;

	public Double getTimeout() {
		return this.timeout;
	}
	
	public void setTimeout(Number timeout) {
		this.timeout = timeout.doubleValue();
	}

	/**
	 * 
	 *
	 * @return Integer
	 */
	public Integer getSeed() {
		return this.seed;
	}

	/**
	 * 
	 *
	 * @param seed Number
	 */
	public void setSeed(Number seed) {
		this.seed = seed.intValue();
	}

	/**
	 * 
	 *
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
		String path = "/common.version";
		try (InputStream stream = Parameters.class.getResourceAsStream(path)) {
			if (stream == null) {
				return "UNKNOWN";
			}
			Properties props = new Properties();
			props.load(stream);
			stream.close();
			return (String) props.get("version");
		} catch (IOException e) {
			log.warn(e);
			return "UNKNOWN";
		}
	}
}
