package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedVariable;
/**
 * @author Gabor
 *
 */
public class VariableAdapter extends XmlAdapter<AdaptedVariable, Variable>  implements Serializable {
	private static final long serialVersionUID = -9222721018270749836L;

	@Override
	public Variable unmarshal(AdaptedVariable v) throws Exception {
		try {
			return v.toVariable();
		}catch(Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}
		
	}

	@Override
	public AdaptedVariable marshal(Variable v) throws Exception {
		try {
			return new AdaptedVariable(v);
		}catch(Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}
	}
	
}
