package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

import javafx.scene.control.TableColumn;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.Attribute;
import uk.ac.ox.cs.pdq.ui.RuntimeController;

import java.util.List;

/**
 * Serializable Table object.
 *
 * @author Camilo Ortiz
 */
public class Table {
    public Attribute[] header;
    public Tuple[] data;
    public int dataSize;
    public String[] columns;

    Table(uk.ac.ox.cs.pdq.datasources.tuple.Table t, ConjunctiveQuery cq){
        //header
        uk.ac.ox.cs.pdq.db.Attribute[] tableHeader = t.getHeader();

        this.header = new Attribute[tableHeader.length];

        for (int i = 0; i < tableHeader.length; i ++){
            header[i] = new Attribute(tableHeader[i].getName(), tableHeader[i].getType().toString());
        }

        this.columns = new String[cq.getFreeVariables().length];
        for (int i = 0, l = cq.getFreeVariables().length; i < l; i++) {
            columns[i] = cq.getFreeVariables()[i].toString();
        }

        //data
        List<uk.ac.ox.cs.pdq.db.tuple.Tuple> tableData = t.getData();
        int n = tableData.size();

        this.dataSize = n;

        if(n > 100) n = 100; //only send a sample of the dataset

        data = new Tuple[n];

        for(int j = 0; j < n; j++){
            data[j] = new Tuple(tableData.get(j));
        }
    }
}
