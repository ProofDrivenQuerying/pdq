package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;

/**
 * Extension of the Attribute class to list the provenance
 * name of an attribute in a plan
 * @author Brandon Moore
 *
 */
public class AttributeAndProvenance extends Attribute{

    protected String provenance;

    protected AttributeAndProvenance(Type type, String name, String provenance) {
        super(type, name);
        assert provenance != null;
        this.provenance = provenance;
    }

    public AttributeAndProvenance(Attribute attribute) {
        super(attribute);
    }

    public String getProvenance() {
        return provenance;
    }
}
