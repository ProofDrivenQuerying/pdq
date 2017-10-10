package uk.ac.ox.cs.pdq.algebra;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.ConditionAdapter;

/**
 * Represents a condition, that can be used to create -for example- a SelectionTerm, or a dependent join term. 
 *  
 * @author Unknown
 * @author Gabor
 */
@XmlJavaTypeAdapter(ConditionAdapter.class)
public abstract class Condition implements Serializable {
	private static final long serialVersionUID = -2227912493390798172L;
	
}
