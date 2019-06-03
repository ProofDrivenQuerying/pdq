package uk.ac.ox.cs.pdq.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Prints a picture of a plan for better readability, and opens it in explorer.
 * Preconditions: GraphViz (http://www.graphviz.org/Download..php) have to be
 * installed and the bin directory have to be added to the path, so the command
 * "dot.exe -V" will work in any command line window. for linux use the
 * following command: sudo apt-get install graphviz
 * 
 * @author Gabor
 */
public class PlanPrinter {
	private static final String WINDOWS_EXECUTABLE = "dot.exe";
	private static final String LINUX_EXECUTABLE = "dot";
	private static final String LINUX_OPEN_IMG = "xdg-open";
	private static final String WINDOWS_OPEN_IMG = "explorer";
	private static String graphVizExecutable = LINUX_EXECUTABLE;
	private static String imageOpen = LINUX_OPEN_IMG;

	private static int id = 0;

	/**
	 * Creates a png file in a temp folder, and attempts to open it with the
	 * explorer.
	 * 
	 * @param t
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void openPngPlan(RelationalTerm t) throws IOException, InterruptedException {
		initOS();
		File gt = File.createTempFile("GraphViz", ".tmp");
		List<String> command = printPlanToFile(t, gt);
		System.out.println("executng cmd: " + command);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectOutput(new File(gt.getParentFile(), gt.getName() + ".png"));
		pb.redirectError(Redirect.INHERIT);
		Process p = pb.start();
		p.waitFor();
		Thread.sleep(50);
		File out = new File(gt.getParentFile(), gt.getName() + ".png");
		System.out.println("executng cmd: " + imageOpen +" " + out);
		Process p2 = Runtime.getRuntime().exec(imageOpen +" " + out.getAbsolutePath());
		p2.waitFor();
		gt.deleteOnExit();
	}

	private static void initOS() {
		String dataDir = System.getenv("XDG_DATA_DIRS");
		if (dataDir!=null && !dataDir.isEmpty()) {
			graphVizExecutable = LINUX_EXECUTABLE;
			imageOpen = LINUX_OPEN_IMG;
		} else {
			graphVizExecutable = WINDOWS_EXECUTABLE;
			imageOpen = WINDOWS_OPEN_IMG;
		}
	}
	private static List<String> printPlanToFile(RelationalTerm t, File out) throws IOException {
		String ret = "digraph G {\n";
		ret += print(t, t.getClass().getSimpleName() + id, null);
		ret += "\n}\n";
		FileWriter fw = new FileWriter(out);
		fw.write(ret);
		fw.close();
		List<String> command = new ArrayList<>();
		command.add(graphVizExecutable);
		command.add(out.getAbsolutePath());
		command.add("-Tpng");
		return command;

	}

	private static String print(RelationalTerm t, String myId, String parentId) {

		String ret = "";
		ret += myId + "[" + getLabelFor(t) + "];\n";
		for (RelationalTerm child : t.getChildren()) {
			id++;
			String childId = child.getClass().getSimpleName() + id;
			ret += myId + "->" + childId + "\n";
			ret += print(child, childId, myId);
		}
		return ret;
	}

	private static String getLabelFor(RelationalTerm t) {
		String ret = "label=\"" + t.getClass().getSimpleName() + "\n";
//		ret += "In :" + Joiner.on(",\n").join(Arrays.asList(t.getInputAttributes())) + "\n";
//		ret += "Out:" + Joiner.on(",\n").join(Arrays.asList(t.getOutputAttributes())) + "\n";
		if (t instanceof SelectionTerm) {
			ret += "SelectionCondition:" + ((SelectionTerm) t).getSelectionCondition() + "\"\n";
			ret += "shape=polygon,sides=4,distortion=-.3\n";
		} else if (t instanceof AccessTerm) {
			ret += "Relation:" + ((AccessTerm) t).getRelation() + "\n";
			ret += "AccessMethod:" + ((AccessTerm) t).getAccessMethod() + "\n";
			ret += "InputConstants:" + ((AccessTerm) t).getInputConstants() + "\"\n";
		} else if (t instanceof RenameTerm) {
			ret += /*"Renamings:" + Arrays.asList(((RenameTerm) t).getRenamings()) + */ "\"\n";
			ret += "shape=polygon,sides=4\n";
		} else if (t instanceof JoinTerm) {
			ret += "Conditions:" + ((JoinTerm) t).getJoinConditions() + "\"\n";
			ret += "shape=invtriangle\n";
		} else if (t instanceof DependentJoinTerm) {
			ret += "Conditions:" + ((DependentJoinTerm) t).getJoinConditions() + "\n";
			ret += "LeftRight positions:" + ((DependentJoinTerm) t).getPositionsInLeftChildThatAreInputToRightChild()
					+ "\n";
			ret += "\",shape=polygon,sides=5\n";
		} else {
			ret += "\",shape=polygon,sides=7\n";
		}
		return ret;
	}

	public static void printPlanToText(OutputStream s, RelationalTerm p) throws IOException {
		s.write(printPlanToString(p).getBytes());
	}
	public static String printPlanToString(RelationalTerm p) {
		if (p instanceof RenameTerm) return printPlanToString(p.getChild(0));
		if (p instanceof ProjectionTerm) return "Project[" + printAttributeList(((ProjectionTerm)p).getProjections()) + "] {"+printPlanToString(p.getChild(0)) + "}";
		if (p instanceof SelectionTerm) 
			return "Selection[" + ((SelectionTerm)p).getSelectionCondition().toString() + "]{" + printPlanToString(p.getChild(0)) + "}";
		if (p instanceof AccessTerm) 
			return "Access[" + ((AccessTerm)p).getRelation() + "(" + Joiner.on(',').join((((AccessTerm)p).getAccessMethod().getInputs())) + ")]";
		if (p instanceof CartesianProductTerm) {
			StringBuffer ret = new StringBuffer();
			ret.append(p.getClass().getSimpleName()); 
			if (p instanceof JoinTerm) {
				ret.append('[');
				ret.append(((JoinTerm) p).getJoinConditions().toString()); 
				ret.append(']');
			}
			ret.append('{');
			ret.append(printPlanToString(p.getChild(1))); 
			ret.append(',');
			ret.append(printPlanToString(p.getChild(0)));
			ret.append('}');
			return ret.toString();
		}
		return "?"+p.getClass().getSimpleName();
	}
	private static String printAttributeList(Attribute[] attributes) {
		StringBuffer ret = new StringBuffer();
		boolean first = true;
		for (Attribute a:attributes) {
			if (first) {
				first = false;
			} else {
				ret.append(",");
			}
			ret.append(a.getName());
		}
		if (ret.length() > 15) return "...";
		return ret.toString();
	}
	
	
}
