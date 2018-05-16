package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * An output method that uses a path expression to extract an attributes values
 * from a JSON result.
 *  
 * @author Julien Leblay
 *
 */
public class PathOutputMethod implements OutputMethod {

	/**  Path separator. */
	public static final String PATH_SEPARATOR = "(?<!\\\\)/";

	/**  Path element regexp named group. TOCOMMENT:?*/
	public static final String PATHELEMENT_GROUP = "path";

	/**  Index regexp named group. TOCOMMENT:? */
	public static final String INDEX_GROUP = "index";

	/**  indexed path element regexp. TOCOMMENT:?*/
	public static final Pattern INDEXED_PARAM = Pattern.compile("(?<" + PATHELEMENT_GROUP + ">[ /\\w]+)(\\[(?<" + INDEX_GROUP + ">[=\\w]+)\\])?");

	public static final Pattern VALUE = Pattern.compile("\\(([^)]+)\\)");

	/**  The path as a list of string. */
	private final List<String> path;

	/**
	 * Initialized an output method from the given path, with PATH_SEPARATOR is
	 * is used to break the path down.
	 *
	 * @param path the path
	 */
	public PathOutputMethod(String path) {
		super();
		Preconditions.checkArgument(path != null && !path.isEmpty());
		this.path = new LinkedList<>();
		for (String p: Lists.newArrayList(path.split(PATH_SEPARATOR))) {
			this.path.add(p.replace("\\", ""));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.service.rest.io.OutputMethod#extract(java.util.Map)
	 */
	@Override
	public Object extract(Map<String, Object> wrapper) {
		return this.extract(wrapper, this.path);
	}

	/**
	 *
	 * @param p the p
	 * @return a pair whose left element the input method referred to in p, and
	 * the right is a string representation of its parameter also found in p.
	 */
	private Pair<String, String> indexedPath(String p) {
		String pathElement = null;
		String index = null;
		if(p.contains("-")) {
			return Pair.of(p, index);
		}
		Matcher m = INDEXED_PARAM.matcher(p);
		if (m.find()) {
			pathElement = m.group(PATHELEMENT_GROUP);
			index = m.group(INDEX_GROUP);
		}
		return Pair.of(pathElement, index);
	}

	/**
	 *
	 * @param wrapper the wrapper
	 * @param subPath the sub path
	 * @return recursively extract an object form the given subPath
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object extract(Map<String, Object> wrapper, List<String> subPath) {
		Preconditions.checkArgument(subPath != null && !subPath.isEmpty());
		Pair<String, String> pathElement = this.indexedPath(subPath.get(0));
		Object o = wrapper.get(pathElement.getLeft());
		if (pathElement.getRight() != null) {
			if (o instanceof List) {
				o = this.extract((List) o, pathElement.getRight()) ;
			} else if ((o instanceof Map)) {
				o = this.extract((Map) o, pathElement.getRight());
			}
		}
		if (subPath.size() == 1) {
			if(o instanceof List) {
				if(((List) o).size() > 0) {
					return ((List) o).get(0);
				}
				else {
					return null;
				}
			}
			return o;
		} else if ((o instanceof Map)) {
			return this.extract((Map) o, subPath.subList(1, subPath.size()));
		}
		else if((o instanceof List)) {
			for(Object o2: (List)o) {
				if(o2 instanceof Map) {
					return this.extract((Map) o2, subPath.subList(1, subPath.size()));
				}
				else {
					throw new java.lang.IllegalStateException();
				}
			}
		}
		//		throw new IllegalStateException("No such path in response wrapper: " + subPath);
		return null;
	}

	/**
	 *
	 * @param wrapper the wrapper
	 * @param key String
	 * @return recursively extract an object form the given subPath
	 */
	private Object extract(Map<String, Object> wrapper, String key) {
		Preconditions.checkArgument(wrapper != null);
		return wrapper.get(key);
	}

	/**
	 *
	 * @param wrapper the wrapper
	 * @param index String
	 * @return recursively extract an object form the given subPath
	 */
	@SuppressWarnings("rawtypes")
	private Object extract(List<Object> wrapper, String index) {
		Preconditions.checkArgument(wrapper != null);
		Preconditions.checkArgument(index != null);
		String[] str = index.split("=");
		if (str.length == 2) {
			for (Object o: wrapper) {
				if (o instanceof Map) {
					Object result = ((Map) o).get(str[0]);
					if (result != null && result.equals(str[1])) {
						return o;
					}
				}
			}
			return null;
		}
		int i = Integer.valueOf(index);
		if (i >=0 && i < wrapper.size()) {
			return wrapper.get(i);
		}
		return null;
	}

	/**
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return this.path.toString();
	}
}
