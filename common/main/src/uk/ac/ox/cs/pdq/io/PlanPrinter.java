package uk.ac.ox.cs.pdq.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Options: 
 *    - flat single line text,
 *    - indented text
 *    - indented sequential text for linear plans.
 *    - png file output - optionally opened in browser.
 *    
 * Preconditions for png: GraphViz (http://www.graphviz.org/Download..php) have to be
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
	private static final boolean PNG_SHORT_MODE=true;
	private static int id = 0;
	public static enum TEXT_MODE {flatline, generictext, linearplantext};
	private static final TEXT_MODE defaultTextMode = TEXT_MODE.linearplantext;
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
		if (!PNG_SHORT_MODE) {
			ret += "In :" + Joiner.on(",\n").join(Arrays.asList(t.getInputAttributes())) + "\n";
			ret += "Out:" + Joiner.on(",\n").join(Arrays.asList(t.getOutputAttributes())) + "\n";
		}
		if (t instanceof SelectionTerm) {
			ret += "SelectionCondition:" + ((SelectionTerm) t).getSelectionCondition() + "\"\n";
			ret += "shape=polygon,sides=4,distortion=-.3\n";
		} else if (t instanceof AccessTerm) {
			ret += "Relation:" + ((AccessTerm) t).getRelation() + "\n";
			ret += "AccessMethod:" + ((AccessTerm) t).getAccessMethod() + "\n";
			ret += "InputConstants:" + ((AccessTerm) t).getInputConstants() + "\"\n";
		} else if (t instanceof RenameTerm) {
			if (!PNG_SHORT_MODE) {
				ret += "Renamings:" + Arrays.asList(((RenameTerm) t).getRenamings());
			}
			ret += "\"\nshape=polygon,sides=4\n";
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

	public static void printPlanToText(PrintStream s, RelationalTerm p, int indent) throws IOException {
		printPlanToText(s,p,indent,defaultTextMode);
	}
	public static void printPlanToText(PrintStream s, RelationalTerm p, int indent, TEXT_MODE tm) throws IOException {
		switch (tm) {
		case flatline:
			s.write(printFlatLinePlanToString(p).getBytes());
			break;
		case generictext:
			printGenericPlanToStream(s,p,indent);
			break;
		case linearplantext:
			printLinearPlanToStream(s,p,indent);
			break;
			
		}
		s.write(printFlatLinePlanToString(p).getBytes());
	}
	
	public static String printFlatLinePlanToString(RelationalTerm p) {
		if (p instanceof RenameTerm) return printFlatLinePlanToString(p.getChild(0));
		if (p instanceof ProjectionTerm) return "Project[" + printAttributeList(((ProjectionTerm)p).getProjections()) + "] {"+printFlatLinePlanToString(p.getChild(0)) + "}";
		if (p instanceof SelectionTerm) 
			return "Selection[" + ((SelectionTerm)p).getSelectionCondition().toString() + "]{" + printFlatLinePlanToString(p.getChild(0)) + "}";
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
			ret.append(printFlatLinePlanToString(p.getChild(1))); 
			ret.append(',');
			ret.append(printFlatLinePlanToString(p.getChild(0)));
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
	
	public static void printLinearPlanToStream(PrintStream out, RelationalTerm p, int indent) {
		if (!p.isLinear()) {
			//System.err.println("Linear printing disabled for non linear plan.");
			printGenericPlanToStream(out,p,indent);
			return;
		}
		printGenericPlanToStream(out,p,indent);
		getSubTree(p, indent);
//		if (indent == 0) {
//			out.println("T1 <- 0");
//			RelationalTerm subTree = getSubTree(p, indent);
//			//printLinearSubTree();
//		} else {
//			out.println("T" + (indent + 1) + "' <- T" + indent); 					       // T2' <- T1
//			out.println("T" + (indent + 1) + " = T" + (indent + 1) + "' |><| T" + indent); // T2 = T2' |><| T1   
//		}
		
	}
	
	private static RelationalTerm getSubTree(RelationalTerm p, int indent) {
		
		return null;
	}

	public static void printGenericPlanToStream(PrintStream out, RelationalTerm p, int indent) {
		if(p instanceof AccessTerm)
		{
			ident(out, indent); out.println("Access");
			ident(out, indent); out.println("{");
			ident(out, indent+1); out.println(chop(p.toString()));
			for(int i = 0; i < p.getChildren().length; i++)
			{
				printGenericPlanToStream(out, p.getChild(i), indent+1);
			}
			ident(out, indent); out.println("}");
		}
		if(p instanceof CartesianProductTerm)
		{
			ident(out, indent); out.println("Join");
			ident(out, indent); out.println("{");
			ident(out, indent+1); out.println(chop(p.toString()));
			for(int i = 0; i < p.getChildren().length; i++)
			{
				printGenericPlanToStream(out, p.getChild(i), indent+1);
			}
			ident(out, indent); out.println("}");
		}
		if(p instanceof ProjectionTerm)
		{
			ident(out, indent); out.println("Project");
			ident(out, indent); out.println("{");
			ident(out, indent+1); out.println(chop(p.toString()));
			for(int i = 0; i < p.getChildren().length; i++)
			{
				printGenericPlanToStream(out, p.getChild(i), indent+1);
			}
			ident(out, indent); out.println("}");
		}
		if(p instanceof RenameTerm)
		{
			ident(out, indent); out.println("Rename");
			ident(out, indent); out.println("{");
			ident(out, indent+1); out.println(chop(p.toString()));
			for(int i = 0; i < p.getChildren().length; i++)
			{
				printGenericPlanToStream(out, p.getChild(i), indent+1);
			}
			ident(out, indent); out.println("}");
		}
		if(p instanceof SelectionTerm)
		{
			ident(out, indent); out.println("Select");
			ident(out, indent); out.println("{");
			ident(out, indent+1); out.println(chop(p.toString()));
			for(int i = 0; i < p.getChildren().length; i++)
			{
				printGenericPlanToStream(out, p.getChild(i), indent+1);
			}
			ident(out, indent); out.println("}");
		}
				
	}
	static public void ident(PrintStream out, int indent)
	{
		for(int i = 0; i < indent; i++) out.print("  ");
	}
	static public String chop(String input)
	{
		String output = "";
		boolean print = false;
		for(int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			if(c == '[') print = true;
			if(print) output = output + c;
			if(c == '{') print = true;
			if(c == ']') break;
		}
		return output;
	}
	
}
