package uk.ac.ox.cs.pdq.fol;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.ConstantAdapter;

/**
 * 
 * A constant term.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author Gabor
 */
@XmlJavaTypeAdapter(ConstantAdapter.class)
public abstract class Constant extends Term {
	private static final long serialVersionUID = -9179710480240580816L;
}
