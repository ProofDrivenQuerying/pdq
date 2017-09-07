package uk.ac.ox.cs.pdq.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;

/** 
 * Prints a picture of a plan for better readability, and opens it in explorer.
 * Preconditions: GraphViz have to be installed and the bin directory have to be added to the path, so the command "dot.exe -V" will work in any command line window.
 *  
 * @author Gabor
 */
public class PlanPrinter {
		private static int id = 0;
		
		/** Creates a png file in a temp folder, and attempts to open it with the explorer.
		 * @param t
		 * @throws IOException
		 * @throws InterruptedException
		 */
		public static void openPngPlan(RelationalTerm t) throws IOException, InterruptedException {
			File gt = File.createTempFile("GraphViz", ".tmp");
			List<String> command = printPlanToFile(t, gt);
			System.out.println("executng cmd: " + command);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(new File(gt.getParentFile(),gt.getName() + ".png"));
            pb.redirectError(Redirect.INHERIT);
            Process p = pb.start();            
            p.waitFor();
            Thread.sleep(50);
            File out = new File(gt.getParentFile(),gt.getName() + ".png");
			System.out.println("executng cmd: " + out);
            Process p2 = Runtime.getRuntime().exec("explorer " + out.getAbsolutePath());
            p2.waitFor();
            //gt.deleteOnExit();
		}
		private static List<String> printPlanToFile(RelationalTerm t, File out) throws IOException {
			String ret = "digraph G {\n";
			ret += print(t,t.getClass().getSimpleName() + id,null);
			ret += "\n}\n";
			FileWriter fw = new FileWriter(out);
			fw.write(ret);
			fw.close();
			List<String> command = new ArrayList<>();
			command.add("dot.exe");
			command.add(out.getAbsolutePath());
			command.add("-Tpng");
			return  command;
			 
		}
		
		private static  String print(RelationalTerm t, String myId, String parentId) {
			
			String ret = ""; 
			ret += myId + "[" + getLabelFor(t)+ "];\n";
			for (RelationalTerm child:t.getChildren()) {
				id++;
				String childId = child.getClass().getSimpleName() + id;
				ret += myId + "->" + childId + "\n";
				ret += print(child, childId, myId);
			}
			return ret;
		}
		private static String getLabelFor(RelationalTerm t) {
			String ret = "label=\""+t.getClass().getSimpleName() + "\n";
			ret += "In :" + Arrays.asList(t.getInputAttributes()) + "\n";
			ret += "Out:" + Arrays.asList(t.getOutputAttributes()) + "\n";
			if (t instanceof SelectionTerm) {
				ret += "SelectionCondition:" + ((SelectionTerm)t).getSelectionCondition() + "\"\n"; 
				ret += "shape=polygon,sides=4,distortion=-.3\n";
			} else
			if (t instanceof AccessTerm) {
				ret += "Relation:" + ((AccessTerm)t).getRelation() + "\n";
				ret += "AccessMethod:" + ((AccessTerm)t).getAccessMethod() + "\n"; 
				ret += "InputConstants:" + ((AccessTerm)t).getInputConstants() + "\"\n"; 
			} else
			if (t instanceof RenameTerm) {
				ret += "Renamings:" + Arrays.asList(((RenameTerm)t).getRenamings()) + "\"\n";
				ret += "shape=polygon,sides=4\n";
			} else
			if (t instanceof JoinTerm) {
				ret += "Conditions:" + ((JoinTerm)t).getJoinConditions() + "\"\n"; 
				ret += "shape=invtriangle\n";
			} else 
			if (t instanceof DependentJoinTerm) {
				ret += "Conditions:" + ((DependentJoinTerm)t).getFollowupJoinConditions() + "\n"; 
				ret += "LeftRight positions:" + ((DependentJoinTerm)t).getPositionsInLeftChildThatAreInputToRightChild() + "\n"; 
				ret += "\",shape=polygon,sides=5\n";
			} else {
				ret += "\",shape=polygon,sides=7\n";
			}
			return ret;
		}
}
