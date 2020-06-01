package uk.ac.ox.cs.pdq;

import uk.ac.ox.cs.pdq.regression.PdqRegression;
import uk.ac.ox.cs.pdq.ui.PDQApplication;
import uk.ac.ox.cs.pdq.cost.CostMain;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.reasoning.Reason;
import uk.ac.ox.cs.pdq.runtime.Runtime;

import java.util.Arrays;

/**
 * FullMain
 */
public class FullMain {

    public static void main(String[] args) {

        if (args.length == 0)
            PDQApplication.main(args);
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
            default:
            	PDQApplication.main(args);
                break;
            }

        }

    }

}