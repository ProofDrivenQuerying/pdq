package uk.ac.ox.cs.pdq.regression.chasebench;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Efi & Gabor
 *
 */
public class CommonToPDQTranslator {

	private static String READ_TABLE_HEADER = "((\\w+)(\\s+)\\{)";
	private static String READ_TABLE_COLUMN = "((\\w+)(\\s+)\\:(\\s+)(STRING|INTEGER|DOUBLE|SYMBOL))";
	private static String READ_TABLE_FOOTER = "(\\})";
	private static String READ_ATOM = "((\\w+)\\(\\S+\\))";
	private static String READ_VARIABLE = "\\?(\\w+)+";
	private static String READ_EQUALITY = "((\\?\\w+) = (\\?\\w+))";

	public static Map<String, Relation> parseTables(String file) {
		Map<String, Relation> relations = Maps.newHashMap();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;
			String tableName = null;
			List<Attribute> attributes = Lists.newArrayList();
			while ((line = input.readLine()) != null) {
				String rN;
				if ((rN = parseTableHeader(line)) != null) {
					tableName = rN;
				}
				Attribute attribute;
				if ((attribute = parseTableColumn(line)) != null) {
					attributes.add(attribute);
				}
				if (parseTableFooter(line)) {
					Relation relation = Relation.create(tableName, attributes.toArray(new Attribute[attributes.size()]));
					relations.put(tableName, relation);
					attributes.clear();
					tableName = null;
				}
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return relations;
	}

	protected static String parseTableHeader(String line) {
		Pattern p = Pattern.compile(READ_TABLE_HEADER);
		Matcher m = p.matcher(line);
		if (m.find()) {
			return m.group(2);
		}
		return null;
	}

	protected static Boolean parseTableFooter(String line) {
		Pattern p = Pattern.compile(READ_TABLE_FOOTER);
		Matcher m = p.matcher(line);
		if (m.find()) {
			return true;
		}
		return false;
	}

	protected static Attribute parseTableColumn(String line) {
		Pattern p = Pattern.compile(READ_TABLE_COLUMN);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String name = m.group(2);
			String type = m.group(5);
			switch (type) {
			case "STRING":
				return Attribute.create(java.lang.String.class, name);
			case "SYMBOL":
				return Attribute.create(java.lang.String.class, name);
			case "INTEGER":
				return Attribute.create(java.lang.Integer.class, name);
			case "DOUBLE":
				return Attribute.create(java.lang.Double.class, name);
			}
		}
		return null;
	}

	protected static Atom parseAtom(Map<String, Relation> relations, String line) {
		Pattern p = Pattern.compile(READ_ATOM);
		Matcher m = p.matcher(line);
		List<Term> terms = Lists.newArrayList();
		if (m.find()) {
			String name = m.group(2);
			Pattern p2 = Pattern.compile(READ_VARIABLE);
			Matcher m2 = p2.matcher(line);
			while (m2.find()) {
				String variable = m2.group(1);
				if (variable.startsWith("?")) {
					terms.add(Variable.create(variable.substring(1, variable.length())));
				} else {
					terms.add(Variable.create(variable.replaceAll("\"", "")));
				}
			}
			return Atom.create(relations.get(name), terms.toArray(new Term[terms.size()]));
		}
		return null;
	}

	protected static List<Atom> parseAtoms(Map<String, Relation> relations, String line) {
		Pattern p = Pattern.compile(READ_ATOM);
		Matcher m = p.matcher(line);
		List<Atom> atoms = Lists.newArrayList();
		while (m.find()) {
			String name = m.group(2);
			int startInd = 0;
			while (m.group(1).indexOf("(",startInd) >0) {
				List<Term> terms = Lists.newArrayList();
				int start = m.group(1).indexOf("(",startInd);
				int end = m.group(1).indexOf(")",start);
				name = m.group(1).substring(startInd, start).trim().replace(",", "").replace(")", "");
				startInd = end;
				String termsList = m.group(1).substring(start + 1, end);
				for (String t : termsList.split(",")) {
					if (t.startsWith("?")) {
						terms.add(Variable.create(t.substring(1, t.length())));
					} else {
						terms.add(TypedConstant.create(t.replaceAll("\"", "")));
					}
				}
				atoms.add(Atom.create(relations.get(name), terms.toArray(new Term[terms.size()])));
			}
		}
		return atoms;
	}

	protected static Atom parseHeadAtom(String line) {
		Pattern p = Pattern.compile(READ_ATOM);
		Matcher m = p.matcher(line);
		while (m.find()) {
			String name = m.group(2);
			List<Term> terms = Lists.newArrayList();
			int start = m.group(1).indexOf("(");
			int end = m.group(1).indexOf(")");
			String termsList = m.group(1).substring(start + 1, end);
			for (String t : termsList.split(",")) {
				if (t.startsWith("?")) {
					terms.add(Variable.create(t.substring(1)));
				} else {
					throw new java.lang.RuntimeException("PDQ format does not support variables in the query head.");
				}
			}
			return Atom.create(Predicate.create(name, terms.size()), terms.toArray(new Term[terms.size()]));
		}
		return null;
	}

	protected static Atom parseEquality(String line) {
		Pattern p = Pattern.compile(READ_EQUALITY);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String left = m.group(2).replace("?","").replaceAll("\"","").trim();
			String right = m.group(3).replace("?","").replaceAll("\"","").trim();
			return Atom.create(Predicate.create("EQUALITY".toLowerCase(), 2, true), Variable.create(left), Variable.create(right));
		}
		return null;
	}

	protected static Dependency parseDependency(Map<String, Relation> relations, String line) {
		if (line.contains("->") && line.contains(".")) {
			String[] sides = line.split("->");
			List<Atom> bodyAtoms = parseAtoms(relations, sides[0]);
			List<Atom> headAtoms = parseAtoms(relations, sides[1]);
			Atom equality = parseEquality(sides[1]);
			if (bodyAtoms.isEmpty() || (headAtoms.isEmpty() && equality == null)) {
				throw new java.lang.IllegalStateException("Illegal rule shape in line: " + line);
			} else {
				if (equality == null) {
					return TGD.create(bodyAtoms.toArray(new Atom[bodyAtoms.size()]), headAtoms.toArray(new Atom[headAtoms.size()]));
				} else {
					return EGD.create(new Atom[] {bodyAtoms.get(0), bodyAtoms.get(1)}, new Atom[] {Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),
							equality.getVariables()[0],equality.getVariables()[1])});
				}
			}
		}
		return null;
	}

	public static List<Dependency> parseDependencies(Map<String, Relation> relations, String file) {
		List<Dependency> dependencies = Lists.newArrayList();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;

			boolean body = true;
			List<Atom> bodyAtoms = Lists.newArrayList();
			List<Atom> headAtoms = Lists.newArrayList();
			List<Atom> headEqualities = Lists.newArrayList();

			while ((line = input.readLine()) != null) {
				Dependency dependency = parseDependency(relations, line);
				if (dependency != null) {
					dependencies.add(dependency);
				} else {
					List<Atom> atoms = parseAtoms(relations, line);
					if (body) {
						bodyAtoms.addAll(atoms);
					} else {
						headAtoms.addAll(atoms);
					}

					Atom equality;
					if ((equality = parseEquality(line)) != null) {
						headEqualities.add(equality);
					}
					if (line.contains("->")) {
						body = false;
					}
					if (line.contains(".")) {
						if (!headAtoms.isEmpty()) {
							dependencies.add(TGD.create(bodyAtoms.toArray(new Atom[bodyAtoms.size()]), headAtoms.toArray(new Atom[headAtoms.size()])));
						} else if (!headEqualities.isEmpty()) {
							dependencies.add(EGD.create(bodyAtoms.toArray(new Atom[bodyAtoms.size()]), headEqualities.toArray(new Atom[headEqualities.size()])));
						}
						body = true;
						bodyAtoms.clear();
						headAtoms.clear();
						headEqualities.clear();
					}
				}
			}
			input.close();
			return dependencies;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ConjunctiveQuery parseQuery(Map<String, Relation> relations, String file) throws IOException {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;

			boolean head = true;
			List<Atom> bodyAtoms = Lists.newArrayList();
			Atom headAtom = null;

			while ((line = input.readLine()) != null) {
				if (head) {
					headAtom = parseHeadAtom(line);
				} else {
					List<Atom> atoms = parseAtoms(relations, line);
					bodyAtoms.addAll(atoms);
				}

				if (line.contains("<-")) {
					head = false;
				}
				if (line.contains(".")) {
					if (headAtom != null) { 
						List<Variable> v = Utility.getVariables(bodyAtoms);
						return ConjunctiveQuery.create(v.toArray(new Variable[v.size()]), bodyAtoms.toArray(new Atom[bodyAtoms.size()]));
					}
				}
			}
		} finally {
			input.close();
		}
		return null;
	}

	public static Collection<Atom> importFacts(Schema schema, String table, String csvFile) {
		Collection<Atom> facts = Sets.newHashSet();
		BufferedReader reader = null;
		try {
			// Open the csv file for reading
			reader = new BufferedReader(new FileReader(csvFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tuple = line.split(",");
				List<Term> constants = Lists.newArrayList();
				for (int i = 0; i < tuple.length; ++i) {
					constants.add(TypedConstant.create(tuple[i].replace("\"", "")));
				}
				facts.add(Atom.create(schema.getRelation(table), constants.toArray(new Term[constants.size()])));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return facts;
	}

}
