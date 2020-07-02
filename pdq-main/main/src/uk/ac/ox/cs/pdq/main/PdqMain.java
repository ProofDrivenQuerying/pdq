// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.main;

import uk.ac.ox.cs.pdq.regression.PdqRegression;
import uk.ac.ox.cs.pdq.cost.CostMain;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.reasoning.Reason;
import uk.ac.ox.cs.pdq.runtime.Runtime;
import uk.ac.ox.cs.pdq.ui.PDQApplication;

import java.util.Arrays;

/**
 * Main entry point for PDQ
 */
public class PdqMain {

    public static void main(String[] args) {

        if (args.length == 0) {
            printHelp();
        }
        else {
            String[] new_args = Arrays.copyOfRange(args, 1, args.length);
            switch (args[0]) {
            case "cost":
                CostMain.main(new_args);
                break;
            case "planner":
                Planner.main(new_args);
                break;
            case "runtime":
                Runtime.main(new_args);
                break;
            case "regression":
                PdqRegression.main(new_args);
                break;
            case "reasoning":
                Reason.main(new_args);
                break;
            case "gui":
                PDQApplication.main(new_args);
                break;
            default:
                printHelp();
                break;
            }
        }

    }

    private static void printHelp() {
        String sb = "Usage: PdqMain [module] [args]\n" +
                "\twhere module is one of:\n" +
                "\t\tcost\n" +
                "\t\tplanner\n" +
                "\t\truntime\n" +
                "\t\tregression\n" +
                "\t\treasoning\n" +
                "\t\tgui\n" +
                "\tand args are arguments for the selected module\n";
        System.out.println(sb);
    }

}