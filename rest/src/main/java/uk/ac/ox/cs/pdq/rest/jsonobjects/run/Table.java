package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.JsonAttribute;

import java.util.List;

/**
 * Serializable Table object.
 *
 * @author Camilo Ortiz
 */
public class JsonTable {
    public JsonAttribute[] header;
    public JsonTuple[] data;
    public int dataSize;

    JsonTable(Table t){
        //header
        Attribute[] tableHeader = t.getHeader();

        this.header = new JsonAttribute[tableHeader.length];

        for (int i = 0; i < tableHeader.length; i ++){
            header[i] = new JsonAttribute(tableHeader[i].getName(), tableHeader[i].getType().toString());
        }

        //data
        List<Tuple> tableData = t.getData();
        int n = tableData.size();

        this.dataSize = n;

        if(n > 100) n = 100; //only send a sample of the dataset

        data = new JsonTuple[n];

        for(int j = 0; j < n; j++){
            data[j] = new JsonTuple(tableData.get(j));
        }
    }
}
