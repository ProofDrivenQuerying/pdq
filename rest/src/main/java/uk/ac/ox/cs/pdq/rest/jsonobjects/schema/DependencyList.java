package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;

/**
 * Serializable class that contains dependency arrays. Initialized with a Schema.
 *
 * @author Camilo Ortiz
 */
public class JsonDependencyList {
    public JsonEGD[] EGDDependencies;
    public JsonTGD[] TGDDependencies;
    public int id;

    public JsonDependencyList(Schema s, int id){
        this.id = id;

        EGD[] egd = s.getKeyDependencies();
        Dependency[] tgd = s.getNonEgdDependencies();

        this.EGDDependencies = new JsonEGD[egd.length];
        for (int i = 0; i < egd.length; i++){
            String n = egd[i].getName();
            String d = egd[i].toString();

            this.EGDDependencies[i] = new JsonEGD(n, d);
        }

        this.TGDDependencies = new JsonTGD[tgd.length];
        for (int j = 0; j< tgd.length; j++){
            String n = tgd[j].getName();
            String d = tgd[j].toString();

            this.TGDDependencies[j] = new JsonTGD(n, d);
        }
    }

}
